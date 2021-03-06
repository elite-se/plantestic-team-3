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
import java.io.File
import java.io.FileNotFoundException

object SwaggerAttributeExtractor {

    fun addSwaggerAttributes(
        inputModel: EObject,
        tomlMap: Map<String, Any>,
        loadAPIModelFromFile: Boolean = false
    ): EObject {

        val messagesList = getRequestMessages(inputModel.eContents())
        val sink2SwaggerMap = mutableMapOf<String, API>()
        messagesList.forEach { message ->
            val message = message as Message
            val sinkBasePath = message.sink.name + ".path"
            val sinkName = message.sink.name + ".swagger_json_path"
            if (!sink2SwaggerMap.containsKey(sinkName)) {
                val path = tomlMap[sinkName] as String
                sink2SwaggerMap[sinkName] = if (loadAPIModelFromFile) {
                    OpenAPI2Importer().createOpenAPI2ModelFromFile(File(path), SerializationFormat.YAML)
                } else {
                    OpenAPI2Importer().createOpenAPI2ModelFromURL(path, SerializationFormat.YAML)
                }
                if (sink2SwaggerMap[sinkName] == null) {
                    throw FileNotFoundException();
                }
            }
            addSwaggerAttributeToRequest(
                message.content as Request,
                tomlMap[sinkBasePath] as String,
                sink2SwaggerMap[sinkName]!!
            )
        }
        return inputModel
    }

    fun getRequestMessages(elements: List<EObject>): List<EObject> {
        val messagesList = elements.filter { obj ->
            obj.eClass().name == "Message" && obj.eContents().filterIsInstance<Request>().any()
        }.toMutableList()
        for (elem in elements) {
            messagesList.addAll(getRequestMessages(elem.eContents()))
        }
        return messagesList
    }

    fun addSwaggerAttributeToRequest(request: Request, requestSinkBasePath: String, openAPI: API) {
        val method = request.method.toLowerCase()
        val filteredPathsList = openAPI.paths.filter { path ->
            pathUrlMatcher(
                path,
                requestSinkBasePath + request.url
            )
        }
        if (filteredPathsList.size != 1) {
            return
        }
        val matchedPath = filteredPathsList[0]
        val params = mutableListOf<String>()

        when (method) {
            "get" -> {
                matchedPath.get.parameters
                    .filter { param -> param.location != ParameterLocation.PATH }
                    .forEach { param -> addAttributes(params, param) }
            }
            "post" -> {
                matchedPath.post.parameters
                    .filter { param -> param.location != ParameterLocation.PATH }
                    .forEach { param -> addAttributes(params, param) }
            }
            "put" -> {
                matchedPath.put.parameters
                    .filter { param -> param.location != ParameterLocation.PATH }
                    .forEach { param -> addAttributes(params, param) }
            }
            "patch" -> {
                matchedPath.patch.parameters
                    .filter { param -> param.location != ParameterLocation.PATH }
                    .forEach { param -> addAttributes(params, param) }
            }
            else -> return
        }
        params.forEach { param ->
            val newRequestParam = PumlFactoryImpl.init().createRequestParam()
            newRequestParam.name = param
            newRequestParam.value = "STUB"
            if (request.requestParam.all { predicate -> predicate.name != param }) {
                request.requestParam.add(newRequestParam)
            }
        }
    }

    fun pathUrlMatcher(path: Path, url: String): Boolean {
        println("Matching " + url + " against " + path.relativePath)
        val componentsPath = path.relativePath.trim { c -> c == '/' }.split("/")
        val componentsUrl = url.trim { c -> c == '/' }.split("/")
        return componentsPath.size == componentsUrl.size && componentsPath.zip(componentsUrl)
            .all { pair -> pair.first == pair.second || pair.first.matches("\\{.*}".toRegex()) }
    }

    fun addAttributes(targetList: MutableList<String>, parameter: Parameter) {
        if (parameter.location == ParameterLocation.BODY) {
            parameter.schema.properties.forEach { property -> if (property.required) targetList.add(property.name) }
        } else {
            targetList.add(parameter.name)
        }
    }
}
