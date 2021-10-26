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
import org.codeontology.extraction.support.ExpressionHolderEntity
import org.codeontology.extraction.support.ExpressionTagger
import spoon.reflect.code.CtCase
import spoon.reflect.code.CtExpression

class CaseLabelEntity(label: CtCase<*>): SwitchLabelEntity(label), ExpressionHolderEntity<CtCase<*>> {
    override fun getType(): RDFNode {
        return Ontology.CASE_ENTITY
    }

    override fun extract() {
        super.extract()
        tagExpression()
    }

    override fun getExpression(): ExpressionEntity<*>? {
        val caseExpression: CtExpression<*>? = element!!.caseExpression
        if (caseExpression != null) {
            val expression: ExpressionEntity<*> = getFactory().wrap(caseExpression)
            expression.parent = this
            return expression
        }

        return null
    }

    override fun tagExpression() {
        ExpressionTagger(this).tagExpression()
    }
}