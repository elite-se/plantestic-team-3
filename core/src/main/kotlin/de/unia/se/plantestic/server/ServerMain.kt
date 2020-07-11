package de.unia.se.plantestic.server

import com.fasterxml.jackson.databind.SerializationFeature
import de.unia.se.plantestic.Main.runTransformationPipeline
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
import java.util.concurrent.TimeUnit

data class PUMLDiagram(val name: String, val diagram: String)

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
        "docker run -d -p 4000:8080 plantuml/plantuml-server:jetty".runCommand(File("./"))
        io.ktor.server.netty.EngineMain.main(arrayOf<String>())
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

                runTransformationPipeline(inputFile, outputFolder)

                call.respond(mapOf("notok" to false))
                println("Pipeline Done")
            }
        }
    }
}


