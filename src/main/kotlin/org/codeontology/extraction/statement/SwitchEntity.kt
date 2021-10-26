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
import spoon.reflect.code.CtSwitch

class SwitchEntity(element: CtSwitch<*>):
    StatementEntity<CtSwitch<*>>(element), ExpressionHolderEntity<CtSwitch<*>> {
    override fun getType(): RDFNode {
        return Ontology.SWITCH_ENTITY
    }

    override fun extract() {
        super.extract()
        tagExpression()
        tagSwitchLabels()
    }

    fun tagSwitchLabels() {
        val labels: List<SwitchLabelEntity> = getSwitchLabels()
        for (label: SwitchLabelEntity in labels) {
            getLogger().addTriple(this, Ontology.SWITCH_LABEL_PROPERTY, label)
            label.extract()
        }
    }

    fun getSwitchLabels(): List<SwitchLabelEntity> {
        val labels: ArrayList<CtCase<*>> = ArrayList()

        for(case: CtCase<*> in element?.cases!!) {
            labels.add(case)
        }

        val result: ArrayList<SwitchLabelEntity> = ArrayList()

        val iterator: Iterator<CtCase<*>> = labels.iterator()
        if (iterator.hasNext()) {
            val previous: SwitchLabelEntity = getFactory().wrap(iterator.next())
            previous.parent = this
            result.add(previous)

            while (iterator.hasNext()) {
                val current: SwitchLabelEntity = getFactory().wrap(iterator.next())
                current.parent = this
                result.add(current)
                previous.next = current
            }
        }

        return result
    }


    override fun getExpression(): ExpressionEntity<*>? {
        val selector: CtExpression<*> = element?.selector ?: return null

        val expression: ExpressionEntity<*> = getFactory().wrap(selector)
        expression.parent = this
        return expression
    }

    override fun tagExpression() {
        ExpressionTagger(this).tagExpression()
    }
}