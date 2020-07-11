package de.unia.se.plantestic

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.google.common.io.Resources
import de.unia.se.plantestic.Main.runTransformationPipeline
import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.joor.Reflect
import java.io.File

val wireMockServer = WireMockServer(8080)

class End2EndTest : StringSpec({

    "End2End test with extracted tester" {
        wireMockServer.stubFor(
            get(urlEqualTo("/testB/hello"))
                .willReturn(WireMock.aResponse().withStatus(200))
        )

        wireMockServer.stubFor(
            get(urlEqualTo("/testC/bye"))
                .willReturn(WireMock.aResponse().withStatus(200))
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

    "End2End test receives request on mock server for the minimal hello" {
        wireMockServer.stubFor(
            get(urlEqualTo("/testB/hello"))
                .willReturn(WireMock.aResponse().withStatus(200))
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
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        wireMockServer.allServeEvents.filter { serveEvent ->
            serveEvent.request.url == "/testB/hello"
        }.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 200
    }

    // This this fails because the receiver "B" is somehow not set, which results in things like "${.path}".
    "End2End test receives request on mock server for complex hello".config(enabled = true) {
        val body = """{
              "itemA": 1,
              "itemB": 1,
            }"""
        wireMockServer.stubFor(
            post(urlPathMatching("/testB/hello/123"))
                .willReturn(aResponse().withStatus(200).withBody(body))
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
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        wireMockServer.allServeEvents.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 200
    }

    // Test is bullshit because it never sets voiceEstablished to anything.
    "End2End test receives request on mock server for rerouting - voiceEstablished == true".config(enabled = false) {
        val body_CCC_CRS = """{
              "uiswitch" : "/UISWITCH",
              "reroute" : "/REROUTE",
              "warmhandover" : "/WARMHANDOVER",
            }"""
        val body_CCC_Voicemanager_voiceenabled = """{
              "eventid1" : "/VoiceStatus/eventId1",
              "agent1" : "/VoiceStatus/agent1/connectionstatus",
              "agent2" : "/VoiceStatus/agent2/connectionstatus",
            }"""
        wireMockServer.stubFor(
            WireMock
                .get(WireMock.urlPathMatching("/CRS/ccc/rerouteOptions"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(body_CCC_CRS)
                )
        )
        wireMockServer.stubFor(
            WireMock
                .get(WireMock.urlPathMatching("/Voicemanager/ccc/events/123/isconnected"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(body_CCC_Voicemanager_voiceenabled)
                )
        )

        runTransformationPipeline(REROUTE_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testrerouting_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(REROUTE_CONFIG_FILE.path)
        try {
            compiledTest.call("test")
        } catch (e: Exception) {
        }

        // Check if we received a correct request
        // TODO: more assertions
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        wireMockServer.allServeEvents.size shouldBe 2
        wireMockServer.allServeEvents[0].response.status shouldBe 200
        wireMockServer.allServeEvents[1].response.status shouldBe 200
    }

    // Test is bullshit because it never sets voiceEstablished to anything.
    "End2End test receives request on mock server for rerouting - voiceEstablished == false, return 400".config(enabled = false) {
        val body_CCC_CRS = """{
              "uiswitch" : "UISWITCH",
              "reroute" : "REROUTE",
              "warmhandover" : "WARMHANDOVER",
            }"""
        wireMockServer.stubFor(
            WireMock
                .get(WireMock.urlPathMatching("/CRS/ccc/rerouteOptions"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(body_CCC_CRS)
                )
        )
        wireMockServer.stubFor(
            WireMock
                .get(WireMock.urlPathMatching("/Voicemanager/ccc/events/123/isconnected"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(400)
                )
        )
        wireMockServer.stubFor(
            WireMock
                .get(WireMock.anyUrl())
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(400)
                )
        )

        runTransformationPipeline(REROUTE_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testrerouting_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(REROUTE_CONFIG_FILE.path)
        compiledTest.call("test")

        // Check if we received a correct request
        // TODO: more assertions
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        wireMockServer.allServeEvents.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 400
    }

    // Test is bullshit because it never sets voiceEstablished to anything.
    "End2End test receives request on mock server for rerouting - voiceEstablished == false, return 404".config(enabled = false) {
        val body_CCC_CRS = """{
              "uiswitch" : "UISWITCH",
              "reroute" : "REROUTE",
              "warmhandover" : "WARMHANDOVER",
            }"""
        wireMockServer.stubFor(
            WireMock
                .get(WireMock.urlPathMatching("/CRS/ccc/rerouteOptions"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(body_CCC_CRS)
                )
        )
        wireMockServer.stubFor(
            WireMock
                .get(WireMock.urlPathMatching("/Voicemanager/ccc/events/123/isconnected"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(404)
                )
        )

        runTransformationPipeline(REROUTE_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testrerouting_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(REROUTE_CONFIG_FILE.path)
        compiledTest.call("test")

        // Check if we received a correct request
        // TODO: more assertions
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        wireMockServer.allServeEvents.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 404
    }

    // Test is bullshit because it never sets voiceEstablished to anything.
    "End2End test receives request on mock server for rerouting - voiceEstablished == false, return 500".config(enabled = false) {
        val body_CCC_CRS = """{
              "uiswitch" : "UISWITCH",
              "reroute" : "REROUTE",
              "warmhandover" : "WARMHANDOVER",
            }""".trimMargin()
        wireMockServer.stubFor(
            WireMock
                .get(WireMock.urlPathMatching("/CRS/ccc/rerouteOptions"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(body_CCC_CRS)
                )
        )
        wireMockServer.stubFor(
            WireMock
                .get(WireMock.urlPathMatching("/Voicemanager/ccc/events/123/isconnected"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(500)
                )
        )
        wireMockServer.stubFor(
            WireMock
                .get(WireMock.anyUrl())
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(500)
                )
        )

        runTransformationPipeline(REROUTE_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testrerouting_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(REROUTE_CONFIG_FILE.path)
        compiledTest.call("test")

        // Check if we received a correct request
        // TODO: more assertions
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        wireMockServer.allServeEvents.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 500
    }

    // This test is bullshit because the mock server setup has nothing to do with the actual scenario
    "End2End test receives request on mock server for the xcall example".config(enabled = false) {
        wireMockServer.stubFor(get(urlEqualTo("/hello/123")).willReturn(aResponse().withBody("test")))

        runTransformationPipeline(XCALL_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testxcall_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(XCALL_CONFIG_FILE.path)
        compiledTest.call("test")

        // Check if we received a correct request
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        wireMockServer.allServeEvents.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 200
    }
}) {
    companion object {
        private val TESTER_OUT_INPUT_FILE = File(Resources.getResource("tester_out.puml").path)
        private val TESTER_OUT_CONFIG_FILE = File(Resources.getResource("tester_out_config.toml").path)

        private val MINIMAL_EXAMPLE_INPUT_FILE = File(Resources.getResource("minimal_hello.puml").path)
        private val MINIMAL_EXAMPLE_CONFIG_FILE = File(Resources.getResource("minimal_hello_config.toml").path)

        private val COMPLEX_HELLO_INPUT_FILE = File(Resources.getResource("complex_hello.puml").path)
        private val COMPLEX_HELLO_CONFIG_FILE = File(Resources.getResource("complex_hello_config.toml").path)

        private val REROUTE_INPUT_FILE = File(Resources.getResource("rerouting.puml").path)
        private val REROUTE_CONFIG_FILE = File(Resources.getResource("rerouting_config.toml").path)

        private val XCALL_INPUT_FILE = File(Resources.getResource("xcall.puml").path)
        private val XCALL_CONFIG_FILE = File(Resources.getResource("xcall_config.toml").path)

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
