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
import org.codeontology.extraction.expression.ExpressionEntity
import org.codeontology.extraction.support.LineTagger
import org.codeontology.extraction.support.VariableDeclarationEntity
import org.codeontology.extraction.support.VariableDeclarationTagger
import spoon.reflect.declaration.CtField

public class FieldDeclaration(element: CtField<*>): CodeElementEntity<CtField<*>>(element), VariableDeclarationEntity<CtField<*>> {
    protected override fun getType(): RDFNode? {
        return null
    }

    public override fun extract() {
        tagType()
        tagLine()
        tagVariable()
        tagInitializer()
        tagSourceCode()
    }

    public override fun getInitializer(): ExpressionEntity<*>? {
        return VariableDeclarationTagger.initializerOf(this)
    }

    public override fun tagInitializer() {
        VariableDeclarationTagger(this).tagInitializer()
    }

    public override fun getVariable(): Entity<*>? {
        return VariableDeclarationTagger.declaredVariableOf(this)
    }

    public override fun tagVariable() {
        getLogger().addTriple(parent!!, Ontology.DECLARATION_PROPERTY, this)
    }

    public fun tagLine() {
        LineTagger(this).tagLine()
    }
}