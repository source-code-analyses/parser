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
import org.codeontology.extraction.support.ConditionHolderEntity
import org.codeontology.extraction.support.ConditionTagger
import spoon.reflect.code.CtIf
import spoon.reflect.code.CtStatement

class IfThenElseEntity(element: CtIf):
    StatementEntity<CtIf>(element), ConditionHolderEntity<CtIf> {
    override fun getType(): RDFNode {
        return Ontology.IF_THEN_ELSE_ENTITY
    }

    override fun extract() {
        super.extract()
        tagCondition()
        tagThenStatement()
        tagElseStatement()
    }

    override fun getCondition(): ExpressionEntity<*> {
        val condition: ExpressionEntity<*> = getFactory().wrap(element!!.condition)
        condition.parent = this
        return condition
    }

    override fun tagCondition() {
        ConditionTagger(this).tagCondition()
    }

    fun tagThenStatement() {
        val thenStatement: CtStatement = element!!.getThenStatement()
        val statement: StatementEntity<*> = getFactory().wrap(thenStatement)
        statement.parent = this
        statement.position = 0
        getLogger().addTriple(this, Ontology.THEN_PROPERTY, statement)
        statement.extract()
    }

    fun tagElseStatement() {
        val elseStatement: CtStatement? = element!!.getElseStatement()
        if (elseStatement != null) {
            val statement: StatementEntity<*> = getFactory().wrap(elseStatement)
            statement.parent = this
            statement.position = 1
            getLogger().addTriple(this, Ontology.ELSE_PROPERTY, statement)
            statement.extract()
        }
    }

}