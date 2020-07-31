package de.unia.se.plantestic

import edu.uoc.som.openapi2.API
import edu.uoc.som.openapi2.Parameter
import edu.uoc.som.openapi2.ParameterLocation
import edu.uoc.som.openapi2.Path
import edu.uoc.som.openapi2.io.OpenAPI2Importer
import edu.uoc.som.openapi2.io.model.SerializationFormat
import org.eclipse.emf.ecore.EObject
import plantuml.puml.Message
import plantuml.puml.Request
import plantuml.puml.impl.PumlFactoryImpl
import java.io.FileNotFoundException

object SwaggerAttributeExtractor {

    fun addSwaggerAttributes(inputModel : EObject, sink2SwaggerPathMap : Map<String, Any>) : EObject {
        val messagesList = inputModel.eContents().filter { obj -> obj.eClass().name == "Message"
                && obj.eContents().filter { message -> message.eClass().name == "Request" }.any()}
        val sink2SwaggerMap = mutableMapOf<String, API>()
        messagesList.forEach { message ->
            val message = message as Message
            val sinkName = message.sink.name + ".swagger_json_path"
            if (!sink2SwaggerMap.containsKey(sinkName)) {
                val path = sink2SwaggerPathMap[sinkName] as String
                sink2SwaggerMap[sinkName] = OpenAPI2Importer().createOpenAPI2ModelFromURL(path, SerializationFormat.YAML)
                if (sink2SwaggerMap[sinkName] == null) {
                    throw FileNotFoundException();
                }
            }
            addSwaggerAttributeToRequest(message.content as Request, sink2SwaggerMap[sinkName]!!)
        }
        return inputModel
    }

    fun addSwaggerAttributeToRequest(request : Request, openAPI : API) {
        val method = request.method.toLowerCase()
        val matchedPath = openAPI.paths.first { path -> pathUrlMatcher(path, request.url) } //TODO: What if multiple matches? Better throw exception?
        val params = mutableListOf<String>()

        when (method) {
            "get" -> {
                matchedPath.get.parameters
                    .filter { param -> param.location != ParameterLocation.PATH}
                    .forEach { param -> addAttributes(params, param) }
            }
            "post" -> {
                matchedPath.post.parameters
                    .filter { param -> param.location != ParameterLocation.PATH}
                    .forEach { param -> addAttributes(params, param) }
            }
            //TODO: other methods e.g. put, delete, etc
        }
        params.forEach { param ->
            val newRequestParam  = PumlFactoryImpl.init().createRequestParam()
            newRequestParam.name = param
            newRequestParam.value = "STUB"
            if (request.requestParam.all { predicate -> predicate.name != param }) {
                request.requestParam.add(newRequestParam)
            }
        }
    }

    fun pathUrlMatcher(path : Path, url : String) : Boolean {
        val componentsPath = path.relativePath.trim { c -> c == '/' }.split("/")
        val componentsUrl = url.trim { c -> c == '/' }.split("/")
        return componentsPath.size == componentsUrl.size && componentsPath.zip(componentsUrl)
            .all { pair -> pair.first == pair.second || pair.first.matches("\\{.*}".toRegex())  }
    }

    fun addAttributes(targetList : MutableList<String>, parameter: Parameter) {
        if(parameter.location == ParameterLocation.BODY) {
            parameter.schema.properties.forEach { property -> if(property.required) targetList.add(property.name) }
        } else {
            targetList.add(parameter.name)
        }
    }
}