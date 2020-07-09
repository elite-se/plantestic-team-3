package de.unia.se.plantestic.server

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.default
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

object ServerMain {

    @JvmStatic
    fun main(args: Array<String>) {
        val server = embeddedServer(Netty, port = 9090) {
            routing {
                static("/") {
                    resources("vueappbuild")
                    defaultResource("vueappbuild/index.html")
                }
            }
        }

        println("Plantestic Server listening at port 9090")
        server.start(wait = true)
    }
}
