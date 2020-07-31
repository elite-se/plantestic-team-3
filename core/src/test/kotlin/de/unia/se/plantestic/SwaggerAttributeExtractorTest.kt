package de.unia.se.plantestic

import com.google.common.io.Resources
import edu.uoc.som.openapi2.ParameterLocation
import edu.uoc.som.openapi2.impl.ExtendedPathImpl
import edu.uoc.som.openapi2.io.OpenAPI2Importer
import edu.uoc.som.openapi2.io.model.SerializationFormat
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldExist
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import plantuml.puml.Alternative
import plantuml.puml.impl.PumlFactoryImpl
import java.io.File

class SwaggerAttributeExtractorTest : StringSpec({
    val apiFile = OpenAPI2Importer().createOpenAPI2ModelFromFile(
        File(Resources.getResource("tests_swagger.yaml").path), SerializationFormat.YAML
    )

    val testUrl = "/testB/hello/{id}"

    "Test pathUrlMatcher" {
        val testPath = ExtendedPathImpl()
        testPath.relativePath = "this/{is}/a/{test}/"
        SwaggerAttributeExtractor.pathUrlMatcher(testPath, "this/test_should_be/a/success").shouldBeTrue()
    }

    "Test addAttributes" {
        val testPath = apiFile.getPathByRelativePath(testUrl)
        val testList = mutableListOf<String>()
        testPath.post.parameters.filter { param -> param.location != ParameterLocation.PATH }
            .forEach { param -> SwaggerAttributeExtractor.addAttributes(testList, param) }
        testList.size.shouldBe(3)
        testList.contains("testQuery").shouldBeTrue()
        testList.contains("varA").shouldBeTrue()
        testList.contains("varB").shouldBeTrue()
    }

    "Test addSwaggerAttributeToRequest" {
        val requestTest = PumlFactoryImpl.init().createRequest()
        requestTest.method = "POST"
        requestTest.url = testUrl

        SwaggerAttributeExtractor.addSwaggerAttributeToRequest(requestTest, "", apiFile)
        requestTest.requestParam.size.shouldBe(3)
        requestTest.requestParam.shouldExist { p -> p.name == "testQuery" }
        requestTest.requestParam.shouldExist { p -> p.name == "varA" }
        requestTest.requestParam.shouldExist { p -> p.name == "varB" }
    }

    "Test addSwaggerAttributes with Alternatives" {
        val pumlFactory = PumlFactoryImpl.init()
        val request = pumlFactory.createRequest()
        request.method = "POST"
        request.url = testUrl
        val sink = pumlFactory.createParticipant()
        sink.name = "SINK"
        val message = pumlFactory.createMessage()
        message.content = request
        message.sink = sink
        val alternative = pumlFactory.createAlternative()
        alternative.umlElements.add(message)

        SwaggerAttributeExtractor.addSwaggerAttributes(
            alternative,
            mapOf(sink.name + ".swagger_json_path" to Resources.getResource("tests_swagger.yaml").path),
            loadAPIModelFromFile = true
        )
        request.requestParam.size.shouldBe(3)
        request.requestParam.shouldExist { p -> p.name == "testQuery" }
        request.requestParam.shouldExist { p -> p.name == "varA" }
        request.requestParam.shouldExist { p -> p.name == "varB" }
    }

    "Test getRequestMessages" {
        val pumlFactory = PumlFactoryImpl.init()

        fun addAlternative(alternative: Alternative, count: Int): Alternative {
            if (count <= 0) {
                return alternative
            }
            val request = pumlFactory.createRequest()
            request.method = "POST"
            request.url = testUrl
            val sink = pumlFactory.createParticipant()
            sink.name = "SINK"
            val message = pumlFactory.createMessage()
            message.content = request
            message.sink = sink
            alternative.umlElements.add(message)

            val newAlt = pumlFactory.createAlternative()
            alternative.umlElements.add(newAlt)
            addAlternative(newAlt, count - 1)

            return alternative
        }

        val requestCount = 5
        val alternative = addAlternative(pumlFactory.createAlternative(), count = requestCount)

        val extractedAlternatives = SwaggerAttributeExtractor.getRequestMessages(alternative.umlElements)
        extractedAlternatives.shouldHaveSize(requestCount)
    }
})
