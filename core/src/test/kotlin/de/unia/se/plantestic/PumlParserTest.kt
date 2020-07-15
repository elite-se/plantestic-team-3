package de.unia.se.plantestic

import com.google.common.io.Resources
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import plantuml.puml.Activate
import plantuml.puml.Alternative
import plantuml.puml.Message
import plantuml.puml.Participant

class PumlParserTest : StringSpec({

    "Parsing works for the minimal example" {
        MetaModelSetup.doSetup()

        val sequenceDiagram = PumlParser.parse(MINIMAL_HELLO_INPUT_PATH)
        printModel(sequenceDiagram)

        sequenceDiagram.umlElements.size shouldBe 4
        (sequenceDiagram.umlElements[0] as Participant).name shouldBe "A"
        (sequenceDiagram.umlElements[1] as Participant).name shouldBe "B"
        (sequenceDiagram.umlElements[2] as Message).source shouldBe sequenceDiagram.umlElements[0]
        (sequenceDiagram.umlElements[2] as Message).sink shouldBe sequenceDiagram.umlElements[1]
        ((sequenceDiagram.umlElements[3] as Activate).umlElements[0] as Message).source shouldBe
                sequenceDiagram.umlElements[1]
        ((sequenceDiagram.umlElements[3] as Activate).umlElements[0] as Message).sink shouldBe
                sequenceDiagram.umlElements[0]
    }

    "Parsing works for the complex hello example" {
        MetaModelSetup.doSetup()

        val sequenceDiagram = PumlParser.parse(COMPLEX_HELLO_INPUT_PATH)
        printModel(sequenceDiagram)

        sequenceDiagram.umlElements.size shouldBe 3
        (sequenceDiagram.umlElements[0] as Participant).name shouldBe "A"
        (sequenceDiagram.umlElements[1] as Participant).name shouldBe "B"

        val alternative = (sequenceDiagram.umlElements[2] as Alternative)

        alternative.text shouldBe "\${testCondition} == 'SomeValue'"
        (alternative.umlElements[0] as Message).source shouldBe sequenceDiagram.umlElements[0]
        (alternative.umlElements[0] as Message).sink shouldBe sequenceDiagram.umlElements[1]
        ((alternative.umlElements[1] as Activate).umlElements[0] as Message).source shouldBe
                sequenceDiagram.umlElements[1]
        ((alternative.umlElements[1] as Activate).umlElements[0] as Message).sink shouldBe
                sequenceDiagram.umlElements[0]
    }

    "Parsing works for the rerouting example" {
        MetaModelSetup.doSetup()

        val sequenceDiagram = PumlParser.parse(REROUTE_INPUT_PATH)
        printModel(sequenceDiagram)

        sequenceDiagram.umlElements.filterIsInstance<Participant>().size shouldBe 3
        val alternative = ((sequenceDiagram.umlElements[5] as Activate)
            .umlElements[2] as Activate).umlElements[0] as Alternative

        alternative.text shouldBe "\${voiceEstablished} == true"
        (alternative.umlElements[0] as Message).source.name shouldBe "VM"
    }

    "Parsing works for the xcall example" {
        MetaModelSetup.doSetup()

        val sequenceDiagram = PumlParser.parse(XCALL_INPUT_PATH)
        printModel(sequenceDiagram)

        sequenceDiagram.umlElements.filterIsInstance<Participant>().size shouldBe 7
        val alternative = (sequenceDiagram.umlElements[7] as Activate).umlElements[4] as Alternative

        alternative.text shouldBe "\${xcsServiceType} == 'ACall'"
        (alternative.umlElements[0] as Message).source.name shouldBe "XCS"
        ((alternative.umlElements[1] as Activate).umlElements[0] as Message).source.name shouldBe "EventNotifier"
    }

    "Parsing works for the parameter_pass example" {
        MetaModelSetup.doSetup()

        val sequenceDiagram = PumlParser.parse(PARAMETER_PASS_INPUT_PATH)
        printModel(sequenceDiagram)

        sequenceDiagram.umlElements.filterIsInstance<Participant>().size shouldBe 2
        val messages = sequenceDiagram.umlElements.filterIsInstance<Message>()
        messages.size shouldBe 6

        messages[0].source.name shouldBe messages[1].sink.name
        messages[1].source.name shouldBe messages[0].sink.name

    }
}) {
    companion object {
        private val MINIMAL_HELLO_INPUT_PATH = Resources.getResource("minimal_hello.puml").path
        private val COMPLEX_HELLO_INPUT_PATH = Resources.getResource("complex_hello.puml").path
        private val REROUTE_INPUT_PATH = Resources.getResource("rerouting.puml").path
        private val XCALL_INPUT_PATH = Resources.getResource("xcall.puml").path
        private val PARAMETER_PASS_INPUT_PATH = Resources.getResource("parameter_pass.puml").path

        fun printModel(model: EObject) {
            val resource = ResourceSetImpl().createResource(URI.createURI("dummy:/test.ecore"))
            resource.contents.add(model)

            resource.save(System.out, null)
        }
    }
}
