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
import org.codeontology.extraction.RDFLogger
import org.codeontology.extraction.declaration.LocalVariableEntity
import org.codeontology.extraction.expression.ExpressionEntity
import org.codeontology.extraction.support.ExpressionHolderEntity
import org.codeontology.extraction.support.ExpressionTagger
import spoon.reflect.code.CtForEach

public class ForEachEntity(element: CtForEach):
    LoopEntity<CtForEach>(element), ExpressionHolderEntity<CtForEach> {
    protected override fun getType(): RDFNode {
        return Ontology.FOR_EACH_ENTITY
    }

    public override fun extract() {
        super.extract()
        tagExpression()
        tagVariable()
    }

    public override fun getExpression(): ExpressionEntity<*> {
        val expression: ExpressionEntity<*> = getFactory().wrap(element!!.expression)
        expression.parent = this
        return expression
    }

    public override fun tagExpression() {
        ExpressionTagger(this).tagExpression()
    }

    private fun getVariable(): LocalVariableEntity {
        val variable: LocalVariableEntity = getFactory().wrap(element!!.variable)
        variable.parent = this
        return variable
    }

    public fun tagVariable() {
        val variable: LocalVariableEntity = getVariable()
        getLogger().addTriple(this, Ontology.VARIABLE_PROPERTY, variable)
        variable.extract()
    }
}