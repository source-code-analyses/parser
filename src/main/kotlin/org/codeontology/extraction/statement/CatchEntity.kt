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
import org.codeontology.extraction.CodeElementEntity
import org.codeontology.extraction.Entity
import org.codeontology.extraction.declaration.ExecutableEntity
import org.codeontology.extraction.declaration.TypeEntity
import org.codeontology.extraction.support.LineTagger
import org.codeontology.extraction.support.StatementsHolderEntity
import org.codeontology.extraction.support.StatementsTagger
import spoon.reflect.code.CtCatch
import spoon.reflect.reference.CtTypeReference

public class CatchEntity(catcher: CtCatch): CodeElementEntity<CtCatch>(catcher), StatementsHolderEntity<CtCatch> {
    public var position: Int = 0

    protected override fun getType(): RDFNode {
        return Ontology.CATCH_ENTITY
    }

    public override fun extract() {
        tagType()
        tagSourceCode()
        tagLine()
        tagStatements()
        tagEndLine()
        tagCatchFormalParameters()
    }

    public fun tagCatchFormalParameters() {
        val formalParameters: List<TypeEntity<*>> = getCatchFormalParameters()
        for(catchFormalParameter: TypeEntity<*> in formalParameters) {
            getLogger().addTriple(this, Ontology.CATCH_FORMAL_PARAMETER_PROPERTY, catchFormalParameter)
            catchFormalParameter.follow()
        }
    }

    public fun getCatchFormalParameters(): List<TypeEntity<*>> {
        val references: List<CtTypeReference<*>> = element?.parameter?.multiTypes ?: ArrayList()
        val parameters: ArrayList<TypeEntity<*>> = ArrayList()

        for(reference: CtTypeReference<*> in references) {
            val parameter: TypeEntity<*> = getFactory().wrap(reference)!!
            parameter.parent = getParent(ExecutableEntity::class.java)
            parameters.add(parameter)
        }

        return parameters
    }

    public fun tagLine() {
        LineTagger(this).tagLine()
    }

    public override fun getStatements(): List<StatementEntity<*>> {
        return StatementsTagger(this).asEntities(element?.body?.statements ?: ArrayList())
    }


    public override fun tagStatements() {
        StatementsTagger(this).tagStatements()
    }

    public fun tagEndLine() {
        LineTagger(this).tagEndLine()
    }
}