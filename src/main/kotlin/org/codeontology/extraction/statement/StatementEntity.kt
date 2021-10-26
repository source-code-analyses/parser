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

import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.RDFNode
import org.codeontology.Ontology
import org.codeontology.extraction.CodeElementEntity
import org.codeontology.extraction.RDFLogger
import org.codeontology.extraction.support.LineTagger
import spoon.reflect.code.CtStatement

open class StatementEntity<E: CtStatement>(element: E): CodeElementEntity<E>(element) {
    var position: Int = 0

    override fun buildRelativeURI(): String {
        return super.buildRelativeURI("statement")
    }

    override fun getType(): RDFNode {
        return Ontology.STATEMENT_ENTITY
    }

    override fun extract() {
        tagType()
        tagPosition()
        tagLine()
        tagSourceCode()
        tagLabel()
    }

    fun tagLabel() {
        val labelString: String? = element?.label
        if (labelString != null) {
            val label: Literal = model.createTypedLiteral(labelString)
            getLogger().addTriple(this, Ontology.WOC_LABEL_PROPERTY, label)
        }
    }

    fun tagLine() {
        LineTagger(this).tagLine()
    }

    fun tagPosition() {
        val position: Literal = model.createTypedLiteral(this.position)
        RDFLogger.getInstance().addTriple(this, Ontology.POSITION_PROPERTY, position)
    }
}