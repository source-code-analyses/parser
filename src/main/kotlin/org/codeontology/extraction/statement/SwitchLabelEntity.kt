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

import org.codeontology.Ontology
import org.codeontology.extraction.CodeElementEntity
import org.codeontology.extraction.support.LineTagger
import org.codeontology.extraction.support.StatementsHolderEntity
import org.codeontology.extraction.support.StatementsTagger
import spoon.reflect.code.CtCase
import spoon.reflect.code.CtStatement

abstract class SwitchLabelEntity(label: CtCase<*>): CodeElementEntity<CtCase<*>>(label), StatementsHolderEntity<CtCase<*>> {
    var next: SwitchLabelEntity? = null

    override fun extract() {
        tagType()
        tagStatements()
        tagLine()
        tagEndLine()
        tagNext()
    }

    fun tagNext() {
        if (next != null) {
            getLogger().addTriple(this, Ontology.NEXT_PROPERTY, next!!)
        }
    }

    override fun getStatements(): List<StatementEntity<*>> {
        val statements: List<CtStatement> = element?.statements ?: ArrayList()
        return StatementsTagger(this).asEntities(statements)
    }

    override fun tagStatements() {
        StatementsTagger(this).tagStatements()
    }

    fun tagLine() {
        LineTagger(this).tagLine()
    }

    fun tagEndLine() {
        LineTagger(this).tagEndLine()
    }
}