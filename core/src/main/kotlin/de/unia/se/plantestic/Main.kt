package de.unia.se.plantestic

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import de.unia.se.plantestic.server.ServerMain
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import java.io.File

object Main {

    class Cli : CliktCommand(
        printHelpOnEmptyArgs = true,
        help = "Plantestic is a tool that transforms UML sequence diagrams of REST APIs into Java unit tests.",
        epilog = """
        |The UML sequence diagrams must be specified with PlantUML.
        |Arrows between actors are considered as requests and responses.
        |Actors are considered services. The actor with the first outgoing arrow is considered the client.
        |Test cases emulate requests of the client against other actors.
        |
        |DIAGRAM FORMAT
        |==============
        |
        |The text on arrows need to follow a certain format to be recognized as requests or responses.
        |For requests, this is:
        |
        |   <METHOD> "<PATH>" (<PARAM_NAME> : "<PARAM_VALUE>"[, ...])
        |
        |Where <METHOD> is one of 'GET', 'POST', 'PUT', 'DELETE', 'PATCH'
        |<PATH> is the relative URL.
        |Params are parameter pairs. Depending on the type of request, they will either be used as query or form params.
        |
        |All values that are in quotes can contain arbitrary variable substitutions of the form "$\{VARIABLE_NAME\}".
        |Variables can either be imported via the configuration file or from previous responses.
        |
        |Similarly, responses need to follow the following schema:
        |
        |<CODE>
        |  or
        |<CODE> - (<VARIABLE_NAME> : "<XPATH_TO_VARIABLE>"[, ...])
        |
        |Where <CODE> is one or a comma-separated list of allowed HTTP response codes.
        |<XPATH_TO_VARIABLE> is a path to a certain value in a JSON or XML body following the XPATH scheme.
        |The value of <XPATH_TO_VARIABLE> will be checked for being present and will then be assigned to
        |<VARIABLE_NAME> for later use.
        |
        |Plantestic also allows conditional requests. They should be put in UML "alt" blocks within PlantUML.
        |The condition might be any valid JavaScript code with any arbitrary templating variables.
        |Before the code will be evaluated, the templating engine will replace all variables in "$\{VAR\}".
        |The condition should return true or false.
        |
        |CONFIG FILE
        |===========
        |
        |Because your test might contain sensitive data or data you quickly want to change between tests,
        |Plantestic also supports config files in .toml format.
        |You can define arbitrary variables and their values in the [Templating] section.
        |Every service might additionally get values for "path", "username" and "password".
        |
        |Example:
        |--------
        |
        |[Templating]
        |id = "asdf"
        |important_value = 5
        |
        |[ServiceA]
        |path = "www.example.com"
        |username = "admin"
        |password = "admin"
        |
        |LEGAL
        |=====
        |
        |This software is licensed under Apache 2.0 license and was developed by 
        |Andreas Zimmerer, Stefan Grafberger, Fiona Guerin, Daniela Neupert and Michelle Martin.
        """.trimMargin()
    ) {

        private val input: String? by option(help = "Path to the PlantUML file containing the API specification.")
        private val output: String by option(help = "Output folder where the test cases should be written to. Default is './plantestic-test'")
            .default("./plantestic-test")
        private val preprocessflag by option(
            "--preprocess",
            "-p",
            help = "Trigger preprocessing of the input PlantUML instead of transforming to Java unit tests. Generates a new PlantUML file into the output folder with imported variables from Swagger and a tester actor (if present)."
        )
            .flag(default = false)
        private val config: String? by option(help = "Path to a .toml file containing configuration information. Required for swagger preprocessing.")
        private val tester: String? by option(help = "Actor in the input PlantUML whose requests should be extracted into a separate actor during preprocessing. If not supplied preprocessing will not generate such a new test actor. If supplied while running the normal pipeline, it will implicitly perform the preprocessing step.")
        private val serverflag by option(
            "--server",
            "-s",
            help = "Starts the server providing the workflow's web GUI at localhost:9090. Can be used independently of or in conjunction with the normal pipeline."
        )
            .flag(default = false)

        override fun run() {
            if (serverflag) {
                MetaModelSetup.doSetup()
                ServerMain.startServer()
            }

            if (input == null) {
                if (!serverflag) echo("Input file has to be provided, when not running in server mode!")
            } else {
                val inputFile = File(input!!).normalize()
                val outputFolder = File(output).normalize()

                if (!inputFile.exists() && !serverflag) {
                    echo("Input file ${inputFile.absolutePath} does not exist.")
                    return
                }

                if (preprocessflag) {
                    if (tester == null && config == null) {
                        echo("During preprocessing you need to specify at least a tester or supply a config .toml file!")
                        return
                    }

                    println("Preprocessing PlantUML '${inputFile.name}'. ${if (tester != null) "Tester: '$tester';" else ""} ${if (config != null) "Config '$config'" else ""}" )

                    MetaModelSetup.doSetup()

                    val inputFileName = (input!!).trim { c -> c == '/' }.split("/").last()
                    val outputFile = File(outputFolder, "preprocessed_$inputFileName").normalize()
                    outputFile.writeText(inputFile.readText())

                    //swagger
                    if (config != null) {
                        val configFile = File(config!!).normalize()
                        val pumlDiagramModel = PumlParser.parse(outputFile.absolutePath)
                        val pumlDiagramWithActor =
                            SwaggerAttributeExtractor.addSwaggerAttributes(pumlDiagramModel.contents[0], parseToml(configFile.readText()))

                        pumlDiagramModel.contents[0] = pumlDiagramWithActor
                        val serialised = PumlSerializer.parse(pumlDiagramModel)
                        outputFile.writeText(serialised)
                    }

                    if (tester != null) {
                        val pumlDiagramModel = PumlParser.parse(outputFile.absolutePath)
                        val pumlDiagramWithActor =
                            M2MTransformer.transformPuml2Puml(pumlDiagramModel.contents[0], tester!!)

                        pumlDiagramModel.contents[0] = pumlDiagramWithActor
                        val serialised = PumlSerializer.parse(pumlDiagramModel)
                        outputFile.writeText(serialised)
                    }

                    println("Preprocessed PlantUML can be found in the file ${outputFile.absolutePath}")
                } else {
                    if (tester != null) {
                        runTransformationPipeline(inputFile, outputFolder, tester!!)
                    } else {
                        runTransformationPipeline(inputFile, outputFolder)
                    }
                }
            }
        }
    }

    fun runTransformationPipeline(inputFile: File, outputFolder: File) {
        MetaModelSetup.doSetup()

        val pumlDiagramModel = PumlParser.parse(inputFile.absolutePath)
        val requestResponsePairsModel = M2MTransformer.transformPuml2ReqRes(pumlDiagramModel.contents[0])
        val restAssuredModel = M2MTransformer.transformReqRes2RestAssured(requestResponsePairsModel)

        println("Generating code into $outputFolder")
        AcceleoCodeGenerator.generateCode(restAssuredModel, outputFolder)
    }

    fun runTransformationPipeline(inputFile: File, outputFolder: File, tester: String) {
        MetaModelSetup.doSetup()

        val pumlDiagramModel = PumlParser.parse(inputFile.absolutePath)
        val pumlDiagramWithActor = M2MTransformer.transformPuml2Puml(pumlDiagramModel.contents[0], tester)
        val requestResponsePairsModel = M2MTransformer.transformPuml2ReqRes(pumlDiagramWithActor)
        val restAssuredModel = M2MTransformer.transformReqRes2RestAssured(requestResponsePairsModel)

        addTestScenarioNameIfNull(restAssuredModel, inputFile)
        println("Generating code into $outputFolder")
        AcceleoCodeGenerator.generateCode(restAssuredModel, outputFolder)
    }

    private fun addTestScenarioNameIfNull(restAssuredModel: EObject, pumlFile: File) {
        val feature: EStructuralFeature = restAssuredModel.eClass().getEStructuralFeature("testScenarioName")
        val curr_val = restAssuredModel.eGet(feature)
        if (curr_val == null) {
            val shorter = pumlFile.name.replace('.', '_')
            restAssuredModel.eSet(feature, shorter)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = Cli().main(args)
}
