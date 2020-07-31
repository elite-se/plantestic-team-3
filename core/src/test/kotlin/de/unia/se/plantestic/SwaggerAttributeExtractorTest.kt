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
import plantuml.puml.Activate
import plantuml.puml.Alternative
import plantuml.puml.Message
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
        testList.size.shouldBe(2)
        testList.contains("varA").shouldBeTrue()
        testList.contains("varB").shouldBeTrue()
    }

    "Test addSwaggerAttributeToRequest" {
        val requestTest = PumlFactoryImpl.init().createRequest()
        requestTest.method = "POST"
        requestTest.url = testUrl

        SwaggerAttributeExtractor.addSwaggerAttributeToRequest(requestTest, "", apiFile)
        requestTest.requestParam.size.shouldBe(2)
        requestTest.requestParam.shouldExist { p -> p.name == "varA" }
        requestTest.requestParam.shouldExist { p -> p.name == "varB" }
    }

    "Test addSwaggerAttributes with Alternatives" {
        val pumlFactory = PumlFactoryImpl.init()

        fun createMessage(): Message {
            val pumlFactory = PumlFactoryImpl.init()
            val request = pumlFactory.createRequest()
            request.method = "POST"
            request.url = testUrl
            val sink = pumlFactory.createParticipant()
            sink.name = "SINK"
            val message = pumlFactory.createMessage()
            message.content = request
            message.sink = sink
            return message
        }

        val alternative = pumlFactory.createAlternative()
        alternative.umlElements.add(createMessage())
        val elseBlock = pumlFactory.createElse()
        elseBlock.umlElements.add(createMessage())
        alternative.elseBlocks.add(elseBlock)
        val sequenceDiagram = pumlFactory.createSequenceDiagram()
        sequenceDiagram.umlElements.add(alternative)

        val extractedRequests = SwaggerAttributeExtractor.getRequestMessages(sequenceDiagram.umlElements)
        extractedRequests.shouldHaveSize(2)
    }

    "Test getRequestMessages nested Alternatives" {
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

    "Test getRequestMessages nested Activate" {
        val pumlFactory = PumlFactoryImpl.init()

        fun addActivate(activate: Activate, count: Int): Activate {
            if (count <= 0) {
                return activate
            }
            val request = pumlFactory.createRequest()
            request.method = "POST"
            request.url = testUrl
            val sink = pumlFactory.createParticipant()
            sink.name = "SINK"
            val message = pumlFactory.createMessage()
            message.content = request
            message.sink = sink
            activate.umlElements.add(message)

            val newActivate = pumlFactory.createActivate()
            newActivate.activate = pumlFactory.createParticipant().apply { name = "P${count - 1}" }
            newActivate.deactivate = pumlFactory.createParticipant().apply { name = "P${count - 1}" }
            activate.umlElements.add(newActivate)
            addActivate(newActivate, count - 1)

            return activate
        }

        val requestCount = 5
        val startActivate = pumlFactory.createActivate()
        startActivate.activate = pumlFactory.createParticipant().apply { name = "P$requestCount" }
        startActivate.deactivate = pumlFactory.createParticipant().apply { name = "P$requestCount" }
        val alternative = addActivate(startActivate, count = requestCount)

        val extractedRequests = SwaggerAttributeExtractor.getRequestMessages(alternative.umlElements)
        extractedRequests.shouldHaveSize(requestCount)
    }
})
