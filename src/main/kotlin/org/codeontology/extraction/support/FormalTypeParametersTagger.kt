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

package org.codeontology.extraction.support

import org.codeontology.CodeOntology
import org.codeontology.Ontology
import org.codeontology.extraction.Entity
import org.codeontology.extraction.EntityFactory
import org.codeontology.extraction.RDFLogger
import org.codeontology.extraction.declaration.TypeVariableEntity
import spoon.reflect.declaration.CtFormalTypeDeclarer
import spoon.reflect.reference.CtTypeReference

public class FormalTypeParametersTagger(private val genericDeclaration: GenericDeclarationEntity<*>) {
    public fun tagFormalTypeParameters() {
        if(!CodeOntology.processGenerics()) {
            return
        }
        val parameters: List<TypeVariableEntity> = genericDeclaration.getFormalTypeParameters()

        parameters.forEachIndexed { i, typeVariable ->
            typeVariable.parent = genericDeclaration
            typeVariable.position = i
            RDFLogger.getInstance().addTriple(genericDeclaration, Ontology.FORMAL_TYPE_PARAMETER_PROPERTY, typeVariable)
            typeVariable.extract()
        }
    }

    companion object {
        @JvmStatic public fun formalTypeParametersOf(genericDeclaration: GenericDeclarationEntity<*>): List<TypeVariableEntity> {
            val typeVariables = mutableListOf<TypeVariableEntity>()

            if(genericDeclaration.element != null && CodeOntology.processGenerics()) {
                val parameters: List<CtTypeReference<*>> = (genericDeclaration.element as CtFormalTypeDeclarer).formalCtTypeParameters as List<CtTypeReference<*>>
                for(parameter in parameters) {
                    val entity: Entity<*>? = EntityFactory.getInstance().wrap(parameter)

                    if(entity is TypeVariableEntity) {
                        typeVariables.add(entity)
                    }
                }
            }

            return typeVariables
        }
    }
}