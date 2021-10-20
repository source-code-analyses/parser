package org.codeontology.extraction.support

import org.apache.jena.rdf.model.Property
import org.codeontology.Ontology
import org.codeontology.extraction.RDFLogger
import org.codeontology.extraction.expression.ExpressionEntity

public class ExpressionTagger(val entity: ExpressionHolderEntity<*>) {
    public fun tagExpression() {
        tagExpression(Ontology.EXPRESSION_PROPERTY)
    }

    public fun tagExpression(property: Property) {
        val expression: ExpressionEntity<*> = entity.getExpression()!!

        RDFLogger.getInstance().addTriple(entity, property, expression)
        expression.extract()
    }
}