package de.unia.se.plantestic

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.google.common.io.Resources
import java.io.File


fun main() {
    val SWAGGER_YAML = File(Resources.getResource("tests_swagger.yaml").path)

    val wireMockServer = WireMockServer(8080)
    wireMockServer.start()
    wireMockServer.stubFor(
        get(urlPathMatching("/swagger/tests.yaml"))
            .willReturn(aResponse().withStatus(200).withBody(SWAGGER_YAML.readText()))
    )
}
