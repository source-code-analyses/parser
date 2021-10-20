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
import org.codeontology.extraction.Entity
import org.codeontology.extraction.support.ExpressionHolderEntity
import org.codeontology.extraction.support.ExpressionTagger
import spoon.reflect.code.CtAssignment

public class AssignmentExpressionEntity(expression: CtAssignment<*, *>): ExpressionEntity<CtAssignment<*, *>>(expression), ExpressionHolderEntity<CtAssignment<*, *>> {
    public override fun extract() {
        super.extract()
        tagLeftHandSideExpression()
        tagExpression()
    }

    public fun tagLeftHandSideExpression() {
        val expression: ExpressionEntity<*> = getLeftHandSideExpression()
        getLogger().addTriple(this, Ontology.LEFT_HAND_SIDE_PROPERTY, expression)
        expression.extract()
    }

    public fun getLeftHandSideExpression(): ExpressionEntity<*> {
        val leftHandExpression: ExpressionEntity<*> = getFactory().wrap(element!!.assigned)
        leftHandExpression.parent = this
        return leftHandExpression
    }

    protected override fun getType(): RDFNode {
        return Ontology.ASSIGNMENT_EXPRESSION_ENTITY
    }

    public override fun getExpression(): ExpressionEntity<*> {
        val expression: ExpressionEntity<*> = getFactory().wrap(element!!.assignment)
        expression.parent = this
        return expression
    }

    public override fun tagExpression() {
        ExpressionTagger(this).tagExpression()
    }
}