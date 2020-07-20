package plantuml.formatting2

import org.eclipse.xtext.formatting2.AbstractFormatter2
import org.eclipse.xtext.formatting2.IFormattableDocument

import plantuml.puml.SequenceDiagram
import plantuml.puml.SequenceElement
import plantuml.puml.UmlElementsContainer

class PumlFormatter extends AbstractFormatter2 {

    def dispatch void format(SequenceDiagram diagram, extension IFormattableDocument document) {
        diagram.regionFor.keyword("@startuml").surround[highPriority; setNewLines(2)]
        for (SequenceElement element : diagram.getUmlElements) {
            element.format
        }
        diagram.regionFor.keyword("@enduml").append[newLine].prepend[highPriority; setNewLines(2)]
    }

    def dispatch void format(SequenceElement element, extension IFormattableDocument document) {
        element.surround[newLine]
    }

    def dispatch void format(UmlElementsContainer element, extension IFormattableDocument document) {
        element.surround[newLine]
        for (SequenceElement child : element.getUmlElements) {
            child.format.surround[indent]
        }
    }
}