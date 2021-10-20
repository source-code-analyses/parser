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

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.RDFNode
import org.codeontology.Ontology
import org.codeontology.extraction.AbstractEntity
import org.codeontology.extraction.Entity
import org.codeontology.extraction.RDFLogger

public class StatementExpressionListEntity public constructor(list: List<Entity<*>>):
    AbstractEntity<List<Entity<*>>>(list) {
    public var position: Int = 0

    companion object {
        @JvmStatic private val TAG = "statement-expression-list"
    }

    init {
        for(current: Entity<*> in list) {
            current.parent = this
        }
    }

    protected override fun buildRelativeURI(): String {
        return "${parent?.getRelativeURI()}$SEPARATOR$TAG$SEPARATOR$position"
    }

    protected override fun getType(): RDFNode {
        return Ontology.STATEMENT_EXPRESSION_LIST_ENTITY
    }

    public override fun extract() {
        tagType()
        tagSourceCode()
    }

    public override fun getSourceCode(): String {
        val list: List<Entity<*>> = element ?: ArrayList()
        val size: Int = list.size
        val builder: StringBuilder = StringBuilder()

        if (size > 0) {
            builder.append(list[0].element)
        }

        for(i in 1 until size) {
            builder.append(", ")
            builder.append(list[i].element)
        }

        return builder.toString()
    }
}