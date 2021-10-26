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

package org.codeontology.extraction.statement

import org.apache.jena.rdf.model.RDFNode
import org.codeontology.Ontology
import org.codeontology.extraction.expression.ExpressionEntity
import org.codeontology.extraction.support.BodyHolderEntity
import org.codeontology.extraction.support.BodyTagger
import org.codeontology.extraction.support.ExpressionHolderEntity
import org.codeontology.extraction.support.ExpressionTagger
import spoon.reflect.code.CtExpression
import spoon.reflect.code.CtSynchronized

class SynchronizedEntity(element: CtSynchronized): StatementEntity<CtSynchronized>(element), BodyHolderEntity<CtSynchronized>, ExpressionHolderEntity<CtSynchronized> {
    override fun getType(): RDFNode {
        return Ontology.SYNCHRONIZED_ENTITY
    }

    override fun extract() {
        super.extract()
        tagBody()
        tagExpression()
    }

    override fun getBody(): StatementEntity<*> {
        val body: StatementEntity<*> = getFactory().wrap(element!!.block)
        body.parent = this
        return body
    }

    override fun tagBody() {
        BodyTagger(this).tagBody()
    }

    override fun getExpression(): ExpressionEntity<*>? {
        val expression: CtExpression<*>? = element?.expression
        if (expression != null) {
            val entity: ExpressionEntity<*> = getFactory().wrap(expression)
            entity.parent = this
            return entity
        }

        return null
    }

    override fun tagExpression() {
        ExpressionTagger(this).tagExpression()
    }
}