package org.codeontology.extraction.support

import org.codeontology.Ontology
import org.codeontology.extraction.RDFLogger
import org.codeontology.extraction.declaration.TypeEntity

public class JavaTypeTagger(private val typedElement: TypedElementEntity<*>) {
    public fun tagJavaType() {
        val type: TypeEntity<*> = typedElement.getJavaType()!!
        RDFLogger.getInstance().addTriple(typedElement, Ontology.JAVA_TYPE_PROPERTY, type)
        type.follow()
    }
}