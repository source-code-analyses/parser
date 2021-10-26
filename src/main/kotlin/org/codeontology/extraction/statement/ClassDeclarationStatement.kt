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
import org.codeontology.extraction.declaration.ExecutableEntity
import org.codeontology.extraction.declaration.TypeEntity
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtType

class ClassDeclarationStatement(element: CtClass<*>): StatementEntity<CtClass<*>>(element) {
    override fun getType(): RDFNode {
        return Ontology.CLASS_DECLARATION_ENTITY
    }

    override fun extract() {
        super.extract()
        tagDeclaredClass()
    }

    fun getDeclaredClass(): TypeEntity<*> {
        val type: TypeEntity<*> = getFactory().wrap(element as CtType<*>)!!
        type.parent = getParent(ExecutableEntity::class.java, TypeEntity::class.java)!!
        return type
    }

    private fun tagDeclaredClass() {
        val declaredClass: TypeEntity<*> = getDeclaredClass()
        getLogger().addTriple(declaredClass, Ontology.DECLARATION_PROPERTY, this)
        declaredClass.extract()
    }
}