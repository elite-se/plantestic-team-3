package de.unia.se.plantestic.server

import com.fasterxml.jackson.databind.SerializationFeature
import de.unia.se.plantestic.*
import de.unia.se.plantestic.Main.runTransformationPipeline
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import java.io.File
import java.util.concurrent.TimeUnit

data class PUMLDiagram(val name: String, val diagram: String, val toml: String?)

data class PreprocessTesterRequest(val pumlString: String, val tester: String)
data class PreprocessSwaggerRequest(val pumlString: String, val tomlString: String)

fun String.runCommand(workingDir: File) {
    ProcessBuilder(*split(" ").toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor(60, TimeUnit.MINUTES)
}

object ServerMain {

    fun startServer(): Unit {
        //start the docker container of the plantuml image generating server
        //if this produces a failure message during runtime, don't worry - it usually means that the container is already running
        //therefore we let it fail silently
        "docker run -d -p 4000:8080 plantuml/plantuml-server:jetty".runCommand(File("./"))

        //start our plantestic server
        io.ktor.server.netty.EngineMain.main(arrayOf<String>())
    }

    //ktor magic, the serverModule function is added to ktor in the application.conf file (located in resources)
    fun Application.plantesticServerModule() {
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT) // Pretty Prints the JSON
            }
        }
        install(StatusPages) {
            exception<Throwable> { e ->
                call.respondText(e.localizedMessage, ContentType.Text.Plain, HttpStatusCode.InternalServerError)
            }
        }
        routing {
            static("/") {
                resources("vueappbuild")
                defaultResource("vueappbuild/index.html")
            }
            get("/test") {
                call.respond(mapOf("OK" to true))
            }
            post("/preprocessTester") {
                val reqParam = call.receive(PreprocessTesterRequest::class)
                println("PreprocessTester: ${reqParam.pumlString}")

                val tmpFile = File("temp.puml")
                tmpFile.writeText(reqParam.pumlString)

                MetaModelSetup.doSetup()
                val pumlDiagramModel = PumlParser.parse(tmpFile.absolutePath)
                val pumlDiagramWithActor =
                    M2MTransformer.transformPuml2Puml(pumlDiagramModel.contents[0], reqParam.tester)

                pumlDiagramModel.contents[0] = pumlDiagramWithActor
                val serialised = PumlSerializer.parse(pumlDiagramModel)

                call.respond(mapOf("processedPuml" to serialised))
                tmpFile.delete()
            }
            post("/preprocessSwagger") {
                val reqParam = call.receive(PreprocessSwaggerRequest::class)
                println("PreprocessSwagger: ${reqParam.pumlString}")

                val tmpFile = File("temp.puml")
                val tmpTomlFile = File("temp.toml")
                tmpFile.writeText(reqParam.pumlString)
                tmpTomlFile.writeText(reqParam.tomlString)

                MetaModelSetup.doSetup()
                val pumlDiagramModel = PumlParser.parse(tmpFile.absolutePath)
                val pumlDiagramWithActor =
                    SwaggerAttributeExtractor.addSwaggerAttributes(pumlDiagramModel.contents[0], parseToml(reqParam.tomlString))

                pumlDiagramModel.contents[0] = pumlDiagramWithActor
                val serialised = PumlSerializer.parse(pumlDiagramModel)

                call.respond(mapOf("processedPuml" to serialised, "processedToml" to tmpTomlFile.readText()))
                tmpFile.delete()
                tmpTomlFile.delete()
            }
            post("runPipeline") {
                val post = call.receive(PUMLDiagram::class)
                println(post.diagram)

                val inputFile = File(post.name + ".puml").normalize()
                inputFile.writeText(post.diagram)
                val outputFolder = File("./testServer").normalize()

                if (post.toml != null) {
                    val tomlFile = File(outputFolder, "Test${post.name}.toml")
                    tomlFile.writeText(post.toml)
                }

                runTransformationPipeline(inputFile, outputFolder)

                call.respond(
                    mapOf(
                        "success" to true,
                        "filePath" to "${outputFolder.absolutePath}/Test${post.name}_puml.java"
                    )
                )
                println("Pipeline Done")
            }
        }
    }
}


