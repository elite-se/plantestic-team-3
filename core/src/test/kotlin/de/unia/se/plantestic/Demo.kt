package de.unia.se.plantestic

import com.google.common.io.Resources
import org.joor.Reflect
import java.io.File


fun main() {
    val OUTPUT_FOLDER = File("/home/tobias/workspace/master/mdd/core/plantestic-test")

    val generatedSourceFile =
        OUTPUT_FOLDER.listFiles().filter { f -> f.name == "TestdemoTest_puml.java" }.first()

    val tomlFile =
        OUTPUT_FOLDER.listFiles().filter { f -> f.name == "TestdemoTest_puml.toml" }.first()

    val compiledTest = Reflect.compile(
        "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
        generatedSourceFile.readText()
    ).create(tomlFile.absolutePath)
    compiledTest.call("test")
}
