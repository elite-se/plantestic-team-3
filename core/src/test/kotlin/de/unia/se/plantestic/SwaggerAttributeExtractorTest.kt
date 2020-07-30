package de.unia.se.plantestic

import com.google.common.io.Resources
import edu.uoc.som.openapi2.ParameterLocation
import edu.uoc.som.openapi2.impl.ExtendedPathImpl
import edu.uoc.som.openapi2.io.OpenAPI2Importer
import edu.uoc.som.openapi2.io.model.SerializationFormat
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldExist
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import plantuml.puml.impl.PumlFactoryImpl
import java.io.File

class SwaggerAttributeExtractorTest : StringSpec ({
    val apiFile = OpenAPI2Importer().createOpenAPI2ModelFromFile(
        File(Resources.getResource("tests_swagger.yaml").path), SerializationFormat.YAML)

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

        SwaggerAttributeExtractor.addSwaggerAttributeToRequest(requestTest, apiFile)
        requestTest.requestParam.size.shouldBe(3)
        requestTest.requestParam.shouldExist { p -> p.name == "testQuery"}
        requestTest.requestParam.shouldExist { p -> p.name == "varA"}
        requestTest.requestParam.shouldExist { p -> p.name == "varB"}
    }
})