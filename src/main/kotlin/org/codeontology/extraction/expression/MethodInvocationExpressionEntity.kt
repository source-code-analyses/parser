/*
Copyright 2021 Mattia Atzeni, Maurizio Atzori, Oliver Schmidtke
This file is part of CodeOntology.
CodeOntology is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
CodeOntology is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with CodeOntology.  If not, see <http://www.gnu.org/licenses/>
*/

package org.codeontology.extraction.expression

import org.apache.jena.rdf.model.RDFNode
import org.codeontology.Ontology
import org.codeontology.extraction.declaration.TypeEntity
import org.codeontology.extraction.support.ExpressionHolderEntity
import org.codeontology.extraction.support.ExpressionTagger
import spoon.reflect.code.CtExpression
import spoon.reflect.code.CtInvocation
import spoon.reflect.code.CtTypeAccess
import spoon.reflect.reference.CtTypeReference

class MethodInvocationExpressionEntity(expression: CtInvocation<*>): AbstractInvocationExpressionEntity<CtInvocation<*>>(expression), ExpressionHolderEntity<CtInvocation<*>> {
    override fun getType(): RDFNode {
        return Ontology.METHOD_INVOCATION_EXPRESSION_ENTITY
    }

    override fun extract() {
        super.extract()
        tagTarget()
        tagArguments()
        tagExecutable()
    }

    private fun tagTarget() {
        val target: CtExpression<*>? = element?.target

        if (target !is CtTypeAccess<*>) {
            tagExpression()
            return
        }

        val reference: CtTypeReference<*> = target.type
        val type: TypeEntity<*>? = getFactory().wrap(reference)
        if (type != null) {
            getLogger().addTriple(this, Ontology.TARGET_PROPERTY, type)
            type.follow()
        }
    }

    override fun getExpression(): ExpressionEntity<*>? {
        val target: CtExpression<*>? = element?.target
        if (target != null) {
            val expression: ExpressionEntity<*> = getFactory().wrap(target)
            expression.parent = this
            return expression
        }
        return null
    }

    override fun tagExpression() {
        ExpressionTagger(this).tagExpression(Ontology.TARGET_PROPERTY)
    }
}