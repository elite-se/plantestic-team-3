package de.unia.se.plantestic

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.EcoreUtil
import plantuml.puml.SequenceDiagram

object PumlParser {

    /**
     * Parses a resource specified by an URI and returns the resulting object tree root element.
     * @param inputUri URI of resource to be parsed as String
     * @return Root model object
     */
    fun parse(inputUri: String): Resource {
        require(EPackage.Registry.INSTANCE["http://www.eclipse.plantuml/Puml"] != null) {
            "Please run MetaModelSetup.doSetup() first!"
        }

        val uri = URI.createFileURI(inputUri)
        val resource = ResourceSetImpl().getResource(uri, true)

        // Resolve cross references
        EcoreUtil.resolveAll(resource)

        require(resource.contents.size > 0) { "File should contain something meaningful." }
        require(resource.contents[0] is SequenceDiagram) { "File should contain a diagram." }
        return resource
    }
}
