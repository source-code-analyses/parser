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

package org.codeontology.extraction.declaration

import org.apache.jena.rdf.model.RDFNode
import org.codeontology.CodeOntology
import org.codeontology.Ontology
import org.codeontology.extraction.support.FormalTypeParametersTagger
import org.codeontology.extraction.support.GenericDeclarationEntity
import spoon.reflect.declaration.CtInterface
import spoon.reflect.reference.CtTypeReference

class InterfaceEntity(interfaceReference: CtTypeReference<*>): TypeEntity<CtInterface<*>>(interfaceReference), GenericDeclarationEntity<CtInterface<*>> {
    override fun getType(): RDFNode {
        return Ontology.INTERFACE_ENTITY
    }

    override fun extract() {
        tagType()
        tagName()
        tagLabel()
        tagSuperInterfaces()
        tagModifiers()
        if (isDeclarationAvailable() || CodeOntology.isJarExplorationEnabled()) {
            tagFields()
            tagMethods()
        }
        if (isDeclarationAvailable()) {
            tagAnnotations()
            tagSourceCode()
            tagComment()
            tagFormalTypeParameters()
        }
    }

    private fun tagSuperInterfaces() {
        tagSuperInterfaces(Ontology.EXTENDS_PROPERTY)
    }

    override fun getFormalTypeParameters(): List<TypeVariableEntity> {
        return FormalTypeParametersTagger.formalTypeParametersOf(this)
    }

    override fun tagFormalTypeParameters() {
        FormalTypeParametersTagger(this).tagFormalTypeParameters()
    }
}