package de.unia.se.plantestic

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.google.common.io.Resources
import de.unia.se.plantestic.Main.runTransformationPipeline
import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import org.joor.Reflect
import java.io.File

val wireMockServer = WireMockServer(8080)

class End2EndTest : StringSpec({

    "End2End test with extracted tester" {
        wireMockServer.stubFor(
            get(urlEqualTo("/testB/hello"))
                .willReturn(aResponse().withStatus(200))
        )

        wireMockServer.stubFor(
            get(urlEqualTo("/testC/bye"))
                .willReturn(aResponse().withStatus(200))
        )

        wireMockServer.stubFor(
            get(urlPathMatching("/swagger/tests.yaml"))
                .willReturn(aResponse().withStatus(200).withBody(SWAGGER_YAML.readText()))
        )

        runTransformationPipeline(TESTER_OUT_INPUT_FILE, OUTPUT_FOLDER, TESTER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile =
            OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testtester_out_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(TESTER_OUT_CONFIG_FILE.path)
        compiledTest.call("test")

        // Check if we received a correct request
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        wireMockServer.allServeEvents.filter { serveEvent ->
            serveEvent.request.url == "/testB/hello"
        }.size shouldBe 1
        wireMockServer.allServeEvents.filter { serveEvent ->
            serveEvent.request.url == "/testC/bye"
        }.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 200
    }

    "End2End test with extracted tester - Complex" {
        wireMockServer.stubFor(
            post(urlEqualTo("/testB/hello"))
                .willReturn(aResponse().withStatus(200))
        )

        wireMockServer.stubFor(
            get(urlEqualTo("/testC/bye"))
                .willReturn(aResponse().withStatus(200))
        )

        wireMockServer.stubFor(
            get(urlEqualTo("/testA"))
                .willReturn(aResponse().withStatus(400))
        )

        wireMockServer.stubFor(
            get(urlPathMatching("/swagger/tests.yaml"))
                .willReturn(aResponse().withStatus(200).withBody(SWAGGER_YAML.readText()))
        )

        runTransformationPipeline(COMPLEX_TESTER_OUT_INPUT_FILE, OUTPUT_FOLDER, TESTER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile =
            OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testcomplex_tester_out_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(COMPLEX_TESTER_OUT_CONFIG_FILE.path)
        compiledTest.call("test")

        // Check if we received a correct request
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        wireMockServer.allServeEvents.filter { serveEvent ->
            serveEvent.request.url == "/testB/hello"
        }.size shouldBe 1
        wireMockServer.allServeEvents.filter { serveEvent ->
            serveEvent.request.url == "/testC/bye"
        }.size shouldBe 1
        wireMockServer.allServeEvents.filter { serveEvent ->
            serveEvent.request.url == "/testA"
        }.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 400
    }

    "End2End test receives request on mock server for the minimal hello" {
        wireMockServer.stubFor(
            get(urlEqualTo("/testB/hello"))
                .willReturn(aResponse().withStatus(200))
        )
        wireMockServer.stubFor(
            get(urlPathMatching("/swagger/tests.yaml"))
                .willReturn(aResponse().withStatus(200).withBody(SWAGGER_YAML.readText()))
        )

        runTransformationPipeline(MINIMAL_EXAMPLE_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile =
            OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testminimal_hello_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(MINIMAL_EXAMPLE_CONFIG_FILE.path)
        compiledTest.call("test")

        // Check if we received a correct request
        val serveEvents = wireMockServer.allServeEvents.filterNot { serveEvent ->
            serveEvent.request.url.startsWith("/swagger")
        }
        serveEvents.forEach { serveEvent -> println(serveEvent.request) }
        serveEvents.size shouldBe 1
        serveEvents[0].response.status shouldBe 200
    }

    // This this fails because the receiver "B" is somehow not set, which results in things like "${.path}".
    "End2End test receives request on mock server for complex hello".config(enabled = true) {
        val responseHello = """{
              "itemA": 1,
              "itemB": 1
            }"""
        wireMockServer.stubFor(
            post(urlPathMatching("/testB/hello/([0-9]+)"))
                .willReturn(aResponse().withStatus(200).withBody(responseHello))
        )
        wireMockServer.stubFor(
            get(urlPathMatching("/swagger/tests.yaml"))
                .willReturn(aResponse().withStatus(200).withBody(SWAGGER_YAML.readText()))
        )

        runTransformationPipeline(COMPLEX_HELLO_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile =
            OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testcomplex_hello_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(COMPLEX_HELLO_CONFIG_FILE.path)
        compiledTest.call("test")

        // Check if we received a correct request
        val serveEvents = wireMockServer.allServeEvents.filterNot { serveEvent ->
            serveEvent.request.url.startsWith("/swagger")
        }
        serveEvents.forEach { serveEvent -> println(serveEvent.request) }
        serveEvents.size shouldBe 1
        serveEvents[0].response.status shouldBe 200
    }

    "End2End test rerouting - voiceEstablished==true" {
        val responseRerouteOptions = """{
              "uiswitch" : "UISWITCH_VALUE",
              "reroute" : "REROUTE_VALUE",
              "warmhandover" : "WARMHANDOVER_VALUE"
            }"""
        val responseIsConnected =
            """{
                "VoiceStatus": {
                    "eventId1" : "123",
                    "agent1" : {
                        "connectionStatus" : "connected"
                    },
                    "agent2" : {
                        "connectionStatus" : "connected"
                    }
                }
            }"""
        wireMockServer.stubFor(
            post(urlEqualTo("/CRS/ccc/rerouteOptions"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(responseRerouteOptions)
                )
        )
        wireMockServer.stubFor(
            get(urlPathMatching("/Voicemanager/ccc/events/([0-9]+)/isconnected"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(responseIsConnected)
                )
        )
        wireMockServer.stubFor(
            get(urlPathMatching("/swagger/tests.yaml"))
                .willReturn(aResponse().withStatus(200).withBody(SWAGGER_YAML.readText()))
        )

        runTransformationPipeline(REROUTE_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().first { f -> f.name == "Testrerouting_puml.java" }
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(REROUTE_CONFIG_FILE.path)
        compiledTest.call("test")

        val serveEvents = wireMockServer.allServeEvents.filterNot { serveEvent ->
            serveEvent.request.url.startsWith("/swagger")
        }
        serveEvents.forEach { serveEvent -> println(serveEvent.request) }
        serveEvents.size shouldBe 2
        serveEvents.forEach { serveEvent -> serveEvent.response.status shouldBe 200 }
    }

    "End2End test receives request on mock server for the xcall example".config(enabled = true) {
        val responseVehicleInternal = """{
              "homeCountry" : "HOME_COUNTRY",
              "positionCountry" : "POS_COUNTRY",
              "brand" : "BRAND"
            }"""
        wireMockServer.stubFor(
            get(urlEqualTo("/DataService/vehicle/internal/987")).willReturn(
                aResponse().withBody(
                    responseVehicleInternal
                )
            )
        )
        val responseRoutingTargetsFind =
            """{
                "voiceTargets" : "VOICE_TARGETS"
            }"""
        wireMockServer.stubFor(
            post(urlEqualTo("/CRS/routingTargets/find")).willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(responseRoutingTargetsFind)
            )
        )
        wireMockServer.stubFor(
            put(urlPathMatching("/([A-Za-z]+)/xcs/notify/([0-9]+)")).willReturn(
                aResponse()
                    .withStatus(200)
            )
        )
        wireMockServer.stubFor(
            post(urlEqualTo("/CCC/xcs/eventReceived")).willReturn(
                aResponse().withStatus(200)
            )
        )
        wireMockServer.stubFor(
            post(urlEqualTo("/Voicemanager/setupCall")).willReturn(
                aResponse().withStatus(200)
            )
        )
        wireMockServer.stubFor(
            get(urlPathMatching("/swagger/tests.yaml"))
                .willReturn(aResponse().withStatus(200).withBody(SWAGGER_YAML.readText()))
        )

        runTransformationPipeline(XCALL_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testxcall_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(XCALL_CONFIG_FILE.path)
        compiledTest.call("test")

        // Check if we received a correct request
        val serveEvents = wireMockServer.allServeEvents.filterNot { serveEvent ->
            serveEvent.request.url.startsWith("/swagger")
        }
        serveEvents.forEach { serveEvent -> println(serveEvent.request) }
        serveEvents.size shouldBe 5
        serveEvents.forEach { serveEvent -> serveEvent.response.status shouldBe 200 }
    }

    "End2End test for parameter_pass" {
        val responseAge = """{"years":22}"""
        wireMockServer.stubFor(
            get(urlEqualTo("/Person/getAge"))
                .willReturn(aResponse().withStatus(200).withBody(responseAge))
        )
        wireMockServer.stubFor(
            post(urlEqualTo("/Person/checkAge"))
                .willReturn(aResponse().withStatus(200))
        )
        wireMockServer.stubFor(
            get(urlPathMatching("/swagger/tests.yaml"))
                .willReturn(aResponse().withStatus(200).withBody(SWAGGER_YAML.readText()))
        )

        runTransformationPipeline(PARAMETER_PASS_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile =
            OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testparameter_pass_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(PARAMETER_PASS_CONFIG_FILE.path)
        compiledTest.call("test")

        // Check if we received a correct request
        val serveEvents = wireMockServer.allServeEvents.filterNot { serveEvent ->
            serveEvent.request.url.startsWith("/swagger")
        }
        serveEvents.forEach { serveEvent -> println(serveEvent.request) }
        serveEvents.size shouldBe 3
        serveEvents.forEach { serveEvent -> serveEvent.response.status shouldBe 200 }

        // original age isn't overwritten
        val ageA1 = serveEvents[2].request.bodyAsString.split("=").last()
        val ageA2 = serveEvents[0].request.bodyAsString.split("=").last()
        ageA1 shouldNotBe ageA2
        ageA2 shouldBe """{"age":22}"""
    }

}) {
    companion object {
        private val TESTER_OUT_INPUT_FILE = File(Resources.getResource("tester_out.puml").path)
        private val TESTER_OUT_CONFIG_FILE = File(Resources.getResource("tester_out_config.toml").path)

        private val COMPLEX_TESTER_OUT_INPUT_FILE = File(Resources.getResource("complex_tester_out.puml").path)
        private val COMPLEX_TESTER_OUT_CONFIG_FILE = File(Resources.getResource("complex_tester_out_config.toml").path)

        private val MINIMAL_EXAMPLE_INPUT_FILE = File(Resources.getResource("minimal_hello.puml").path)
        private val MINIMAL_EXAMPLE_CONFIG_FILE = File(Resources.getResource("minimal_hello_config.toml").path)

        private val COMPLEX_HELLO_INPUT_FILE = File(Resources.getResource("complex_hello.puml").path)
        private val COMPLEX_HELLO_CONFIG_FILE = File(Resources.getResource("complex_hello_config.toml").path)

        private val REROUTE_INPUT_FILE = File(Resources.getResource("rerouting.puml").path)
        private val REROUTE_CONFIG_FILE = File(Resources.getResource("rerouting_config.toml").path)

        private val XCALL_INPUT_FILE = File(Resources.getResource("xcall.puml").path)
        private val XCALL_CONFIG_FILE = File(Resources.getResource("xcall_config.toml").path)

        private val PARAMETER_PASS_INPUT_FILE = File(Resources.getResource("parameter_pass.puml").path)
        private val PARAMETER_PASS_CONFIG_FILE = File(Resources.getResource("parameter_pass_config.toml").path)

        private val SWAGGER_YAML = File(Resources.getResource("tests_swagger.yaml").path)

        private val OUTPUT_FOLDER = File(Resources.getResource("code-generation").path + "/End2EndTests/GeneratedCode")

        private val TESTER = "A"

        fun printCode(folder: File) {
            folder.listFiles().forEach { file ->
                val lines = file.readLines()
                lines.forEach { line -> println(line) }
            }
        }
    }

    override fun beforeTest(description: Description) {
        wireMockServer.start()
    }

    override fun afterTest(description: Description, result: TestResult) {
        wireMockServer.stop()
        wireMockServer.resetAll()
    }
}
