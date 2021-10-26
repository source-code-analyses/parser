package org.codeontology.extraction.support

import org.apache.jena.rdf.model.Property
import org.codeontology.Ontology
import org.codeontology.extraction.RDFLogger
import org.codeontology.extraction.expression.ExpressionEntity

class ExpressionTagger(val entity: ExpressionHolderEntity<*>) {
    fun tagExpression() {
        tagExpression(Ontology.EXPRESSION_PROPERTY)
    }

    fun tagExpression(property: Property) {
        val expression: ExpressionEntity<*> = entity.getExpression()!!

        RDFLogger.getInstance().addTriple(entity, property, expression)
        expression.extract()
    }
}