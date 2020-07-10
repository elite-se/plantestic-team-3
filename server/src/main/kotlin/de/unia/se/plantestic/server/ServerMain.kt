package de.unia.se.plantestic.server

import com.fasterxml.jackson.databind.SerializationFeature
import de.unia.se.plantestic.AcceleoCodeGenerator
import de.unia.se.plantestic.M2MTransformer
import de.unia.se.plantestic.Main.runTransformationPipeline
import de.unia.se.plantestic.MetaModelSetup
import de.unia.se.plantestic.PumlParser
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.jackson.*
import io.ktor.request.receive
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import java.io.File

data class PUMLDiagram(val name: String, val diagram: String)

object ServerMain {
    //fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

    @JvmStatic
    fun main(args: Array<String>): Unit {
        val inputFile = File("src/main/resources/complex_hello.puml").normalize()
        val outputFolder = File("./testServer").normalize()

        runTransformationPipeline(inputFile, outputFolder)
    }



    fun Application.serverModule() {
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
            post("runPipeline") {
                val post = call.receive(PUMLDiagram::class)
                println(post.diagram)

                val inputFile = File(post.name + ".puml").normalize()
                inputFile.writeText(post.diagram)
                val outputFolder = File("./testServer").normalize()

                MetaModelSetup.doSetup()

                val pumlDiagramModel = PumlParser.parse(inputFile.absolutePath)

                val requestResponsePairsModel = M2MTransformer.transformPuml2ReqRes(pumlDiagramModel)
                val restAssuredModel = M2MTransformer.transformReqRes2RestAssured(requestResponsePairsModel)

                println("Generating code into $outputFolder")
                AcceleoCodeGenerator.generateCode(restAssuredModel, outputFolder)

                call.respond(mapOf("notok" to false))
            }
        }
    }
}


