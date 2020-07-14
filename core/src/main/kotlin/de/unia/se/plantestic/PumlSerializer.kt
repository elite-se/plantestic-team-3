package de.unia.se.plantestic

import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.resource.SaveOptions
import java.io.ByteArrayOutputStream


object PumlSerializer {

    /**
     * Serialize an existing SequenceDiagram a resource specified by an URI and returns the resulting object tree root element.
     * @param inputUri URI of resource to be parsed as String
     * @return Root model object
     */
    fun parse(resource: Resource): String {
        require(EPackage.Registry.INSTANCE["http://www.eclipse.plantuml/Puml"] != null) {
            "Please run MetaModelSetup.doSetup() first!"
        }

        val buffer = ByteArrayOutputStream();
        val options = SaveOptions.newBuilder().format().options.toOptionsMap()

        resource.save(buffer, options);

        return buffer.toString()
    }
}