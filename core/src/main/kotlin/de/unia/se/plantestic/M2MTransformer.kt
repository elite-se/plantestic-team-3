package de.unia.se.plantestic

import com.google.common.io.Resources
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.parser.core.models.ParseOptions
import org.eclipse.emf.common.util.Diagnostic
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.m2m.qvt.oml.BasicModelExtent
import org.eclipse.m2m.qvt.oml.ExecutionContext
import org.eclipse.m2m.qvt.oml.ExecutionContextImpl
import org.eclipse.m2m.qvt.oml.TransformationExecutor
import org.eclipse.m2m.qvt.oml.util.WriterLog
import plantuml.puml.Request
import plantuml.puml.SequenceDiagram
import plantuml.puml.impl.PumlFactoryImpl
import java.io.OutputStreamWriter

object M2MTransformer {

    private val QVT_PUML2PUML_TRANSFORMATION_URI =
        URI.createURI(Resources.getResource("qvt/puml2puml.qvto").toExternalForm())

    private val QVT_PUML2REQRES_TRANSFORMATION_URI =
        URI.createURI(Resources.getResource("qvt/puml2reqres.qvto").toExternalForm())

    private val QVT_REQRES2RESTASSURED_TRANSFORMATION_URI =
        URI.createURI(Resources.getResource("qvt/reqres2restassured.qvto").toExternalForm())

    /**
     * Transforms a UmlDiagram EObject to a another UmlDiagram  EObject with extracted actor.
     * @param inputModel The UmlDiagram to transform
     * @return UmlDiagram with extracted actor
     */
    fun transformPuml2Puml(inputModel: EObject, tester : String): EObject {
        require(inputModel is SequenceDiagram) { "Puml transformation input wasn't a puml object!" }
        val context = setContext(inputModel, Pair("tester", tester))
        val outputModel = doQvtoTransformation(inputModel, QVT_PUML2PUML_TRANSFORMATION_URI, context)
        val test = addSwaggerAttributes(outputModel)
        return outputModel
    }

    fun addSwaggerAttributes(inputModel : EObject) : List<EObject> {
        val requestsList = inputModel.eContents().filter { obj -> obj.eClass().name == "Message"
                && obj.eContents().filter { message -> message.eClass().name == "Request" }.any()}
            .map { message -> message.eGet(message.eClass().getEStructuralFeature("content")) as EObject }
        val swaggerContents = Resources.getResource("tests_swagger.yaml").readText()
        val options = ParseOptions();
        options.setResolve(true);
        options.setFlatten(true);
        val result = OpenAPIParser().readContents(swaggerContents, null, options);
        val openApi = result.getOpenAPI();
        requestsList.forEach { request -> addSwaggerAttributeToRequest(request as Request, openApi) }
        return requestsList
    }

    fun addSwaggerAttributeToRequest(request : Request, openAPI : OpenAPI) {
        val method = request.method.toLowerCase()
        val item = openAPI.paths.get(request.url)
        var params = mutableListOf<String>()

        if (item != null) {
            when (method) {
                "get" -> {
                    params =  item.get.parameters
                        .filter { param -> param.`in` != "path"}.map { param -> param.name }.toMutableList()
                    addBodyAttributes(item.get, openAPI, params)
                }
                "post" -> {
                    params =  item.post.parameters
                        .filter { param -> param.`in` != "path"}.map { param -> param.name }.toMutableList()
                    addBodyAttributes(item.post, openAPI, params)
                }
                //TODO: other methods e.g. put, delete, etc
                else -> {
                }//TODO: error handling
            }
        }
        params.forEach { param ->
                val newRequestParam  = PumlFactoryImpl.init().createRequestParam()
                newRequestParam.name = param
                newRequestParam.value = "STUB"
                request.requestParam.add(newRequestParam)
            }
    }

    fun addBodyAttributes(operation : Operation, openAPI : OpenAPI, params : MutableList<String> ){
        val mediaType = operation.requestBody.content.get("application/json")
        if (mediaType != null) {
            val reference = mediaType.schema.`$ref`
            if (reference.startsWith("#/components/schemas/")){
                val contentName = reference.split('/').last()
                val test = openAPI.components.schemas[contentName]!!.properties!!.keys
                params.addAll(test)
            } else {
                //TODO: Error handling malformed reference
            }

        } else {
            //TODO: Error handling no application/json
        }
    }


    /**
     * Transforms a UmlDiagram EObject to a Request Response Pair EObject.
     * @param inputModel The UmlDiagram to transform
     * @return Request Response Pair
     */
    fun transformPuml2ReqRes(inputModel: EObject): EObject {
        require(inputModel is SequenceDiagram) { "Puml transformation input wasn't a puml object!" }
        val context = setContext(inputModel)
        return doQvtoTransformation(inputModel, QVT_PUML2REQRES_TRANSFORMATION_URI, context)
    }

    /**
     * Transforms a Request Response Pair EObject to a Rest Assured EObject.
     * @param inputModel The Request Response Pair EObject to transform
     * @returnRest Assured EObject
     */
    fun transformReqRes2RestAssured(inputModel: EObject): EObject {
        val context = setContext(inputModel)
        return doQvtoTransformation(inputModel, QVT_REQRES2RESTASSURED_TRANSFORMATION_URI, context)
    }

    private fun setContext(inputModel: EObject, vararg pairs: Pair<String, Any>) : ExecutionContext {
        val context = ExecutionContextImpl()
        context.setConfigProperty("keepModeling", true)
        context.setConfigProperty("diagramName", EcoreUtil.getURI(inputModel).trimFileExtension().lastSegment())
        for (pair in pairs) {
            context.setConfigProperty(pair.first, pair.second)
        }
        require(System.out != null) { "System.out was null!" }
        val outStream = OutputStreamWriter(System.out!!)
        val log = WriterLog(outStream)
        context.log = log
        return context;
    }

    private fun doQvtoTransformation(inputModel: EObject, transformationUri: URI, context : ExecutionContext): EObject {
        // Sources:
        // - https://github.com/mrcalvin/qvto-cli/blob/master/qvto-app/src/main/java/at/ac/wu/nm/qvto/App.java
        // - https://wiki.eclipse.org/QVTOML/Examples/InvokeInJava

        val executor = TransformationExecutor(transformationUri)
        val validationDiagnostic = executor.loadTransformation()
        require(validationDiagnostic.message == "OK") {
            validationDiagnostic.children.fold(StringBuilder("\n"), { sb, child -> sb.appendln(child) })
        }

        val input = BasicModelExtent(listOf(inputModel))
        val output = BasicModelExtent()


        val result = executor.execute(context, input, output)

        if (result.severity == Diagnostic.OK) {
            require(!output.contents.isNullOrEmpty()) { "No transformation result!" }
            return output.contents[0]
        } else {
            throw IllegalArgumentException(result.toString())
        }
    }
}
