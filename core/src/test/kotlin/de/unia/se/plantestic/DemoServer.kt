package de.unia.se.plantestic

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.google.common.io.Resources
import java.io.File


fun main() {
    val SWAGGER_YAML = File(Resources.getResource("demo_swagger.yaml").path)

    val wireMockServer = WireMockServer(8080)
    wireMockServer.start()
    wireMockServer.stubFor(
        get(urlPathMatching("/swagger/tests.yaml"))
            .willReturn(aResponse().withStatus(200).withBody(SWAGGER_YAML.readText()))
    )
    wireMockServer.stubFor(
        get(urlPathMatching("/testB/asynccall/([0-9]+)")).willReturn(
            aResponse().withStatus(200).withHeader("content-type", "application/json")
                .withBody("""{"a": "Hello World!"}""")
        )
    )
    wireMockServer.stubFor(
        get(urlPathMatching("/testA/STUB")).willReturn(
            aResponse().withStatus(200).withHeader("content-type", "application/json")
        )
    )
}
