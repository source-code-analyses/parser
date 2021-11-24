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
import org.codeontology.extraction.CodeElementEntity
import org.codeontology.extraction.declaration.TypeEntity
import org.codeontology.extraction.support.GenericDeclarationEntity
import org.codeontology.extraction.support.JavaTypeTagger
import org.codeontology.extraction.support.LineTagger
import org.codeontology.extraction.support.TypedElementEntity
import spoon.reflect.code.CtExpression

open class ExpressionEntity<E: CtExpression<*>>(expression: E):
    CodeElementEntity<E>(expression), TypedElementEntity<E> {
    override fun getType(): RDFNode {
        return Ontology.EXPRESSION_ENTITY
    }

    override fun buildRelativeURI(): String {
        return super.buildRelativeURI("expression")
    }

    override fun extract() {
        tagType()
        tagJavaType()
        tagSourceCode()
        tagLine()
    }

    private fun tagLine() {
        LineTagger(this).tagLine()
    }

    override fun getJavaType(): TypeEntity<*>? {
        val type: TypeEntity<*>? = getFactory().wrap(element!!.type)
        if (type != null) {
            type.parent = getParent(GenericDeclarationEntity::class.java)!!
            return type
        }

        return null
    }

    override fun tagJavaType() {
        JavaTypeTagger(this).tagJavaType()
    }
}