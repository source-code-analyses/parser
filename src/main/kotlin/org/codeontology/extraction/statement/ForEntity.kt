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
import org.codeontology.extraction.Entity
import org.codeontology.extraction.expression.ExpressionEntity
import org.codeontology.extraction.support.ConditionHolderEntity
import org.codeontology.extraction.support.ConditionTagger
import spoon.reflect.code.CtExpression
import spoon.reflect.code.CtFor

class ForEntity(element: CtFor):
    LoopEntity<CtFor>(element), ConditionHolderEntity<CtFor> {
    override fun getType(): RDFNode {
        return Ontology.FOR_ENTITY
    }

    override fun extract() {
        super.extract()
        tagForInit()
        tagCondition()
        tagForUpdate()
    }

    override fun getCondition(): ExpressionEntity<*>? {
        val expression: CtExpression<*>? = element!!.expression
        if (expression != null) {
            val condition: ExpressionEntity<*> = getFactory().wrap(expression)
            condition.parent = this
            return condition
        }

        return null
    }

    override fun tagCondition() {
        ConditionTagger(this).tagCondition()
    }

    private fun getForInit(): StatementExpressionListEntity {
        val forInit: StatementExpressionListEntity = getFactory().wrap(element!!.forInit)
        forInit.parent = this
        forInit.position = 0
        return forInit
    }

    fun tagForInit() {
        val forInit: List<Entity<*>> = getForInit().element!!
        for (init: Entity<*> in forInit) {
            getLogger().addTriple(this, Ontology.FOR_INIT_PROPERTY, init)
            init.extract()
        }
    }

    private fun getForUpdate(): StatementExpressionListEntity {
        val forUpdate: StatementExpressionListEntity = getFactory().wrap(element!!.forUpdate)
        forUpdate.position = 2
        forUpdate.parent = this
        return forUpdate
    }

    fun tagForUpdate() {
        val forUpdate: List<Entity<*>> = getForUpdate().element!!
        for (update: Entity<*> in forUpdate) {
            getLogger().addTriple(this, Ontology.FOR_UPDATE_PROPERTY, update)
            update.extract()
        }
    }
}