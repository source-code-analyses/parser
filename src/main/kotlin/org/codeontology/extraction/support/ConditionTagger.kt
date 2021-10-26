package org.codeontology.extraction.support

import org.codeontology.Ontology
import org.codeontology.extraction.RDFLogger
import org.codeontology.extraction.expression.ExpressionEntity

class ConditionTagger(val entity: ConditionHolderEntity<*>) {
    fun tagCondition() {
        val condition: ExpressionEntity<*> = entity.getCondition()!!

        RDFLogger.getInstance().addTriple(entity, Ontology.CONDITION_PROPERTY, condition)
        condition.extract()
    }
}