package de.unia.se.plantestic

import com.google.common.io.Resources
import edu.uoc.som.openapi2.*
import edu.uoc.som.openapi2.io.OpenAPI2Importer
import edu.uoc.som.openapi2.io.model.SerializationFormat
import org.eclipse.emf.common.util.Diagnostic
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.m2m.qvt.oml.BasicModelExtent
import org.eclipse.m2m.qvt.oml.ExecutionContext
import org.eclipse.m2m.qvt.oml.ExecutionContextImpl
import org.eclipse.m2m.qvt.oml.TransformationExecutor
import org.eclipse.m2m.qvt.oml.util.WriterLog
import plantuml.puml.Message
import plantuml.puml.Request
import plantuml.puml.SequenceDiagram
import plantuml.puml.impl.PumlFactoryImpl
import java.io.FileNotFoundException
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
        return outputModel
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
