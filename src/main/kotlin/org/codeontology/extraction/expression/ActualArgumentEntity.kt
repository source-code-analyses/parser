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

import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.RDFNode
import org.codeontology.Ontology
import org.codeontology.extraction.AbstractEntity
import org.codeontology.extraction.Entity
import org.codeontology.extraction.support.ExpressionHolderEntity
import org.codeontology.extraction.support.ExpressionTagger

public class ActualArgumentEntity(expression: ExpressionEntity<*>): AbstractEntity<ExpressionEntity<*>>(expression), ExpressionHolderEntity<ExpressionEntity<*>> {
    public var position: Int = 0

    companion object {
        @JvmStatic private val TAG: String = "argument"
    }

    public override fun buildRelativeURI(): String {
        return element!!.getRelativeURI() + SEPARATOR + TAG + SEPARATOR + position
    }

    public override fun extract() {
        tagType()
        tagPosition()
        tagExpression()
    }

    protected override fun getType(): RDFNode {
        return Ontology.ACTUAL_ARGUMENT_ENTITY
    }

    public override fun getExpression(): ExpressionEntity<*>? {
        return element
    }

    public override fun tagExpression() {
        ExpressionTagger(this).tagExpression()
    }

    public fun tagPosition() {
        val position: Literal = model.createTypedLiteral(position)
        getLogger().addTriple(this, Ontology.POSITION_PROPERTY, position)
    }
}