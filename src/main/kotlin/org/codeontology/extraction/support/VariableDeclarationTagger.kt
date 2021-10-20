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

import org.codeontology.Ontology
import org.codeontology.extraction.Entity
import org.codeontology.extraction.EntityFactory
import org.codeontology.extraction.RDFLogger
import org.codeontology.extraction.expression.ExpressionEntity
import spoon.reflect.code.CtExpression
import spoon.reflect.declaration.CtVariable

public class VariableDeclarationTagger(private val declaration: VariableDeclarationEntity<*>) {
    public fun tagVariable() {
        val variable = declaration.getVariable()!!

        RDFLogger.getInstance().addTriple(variable, Ontology.DECLARATION_PROPERTY, declaration)
        variable.extract()
    }

    public fun tagInitializer() {
        val expression = declaration.getInitializer()!!

        RDFLogger.getInstance().addTriple(declaration, Ontology.INITIALIZER_PROPERTY, expression)
        expression.extract()
    }

    companion object {
        @JvmStatic public fun initializerOf(declaration: VariableDeclarationEntity<out CtVariable<*>>): ExpressionEntity<*>? {
            val defaultExpression: CtExpression<*>? = declaration.element!!.defaultExpression
            if(defaultExpression != null) {
                val initializer: ExpressionEntity<*> = EntityFactory.getInstance().wrap(declaration.element!!) as ExpressionEntity<*>
                initializer.parent = declaration
                return initializer
            }

            return null
        }

        @JvmStatic public fun declaredVariableOf(declaration: VariableDeclarationEntity<out CtVariable<*>>): Entity<*>? {
            val declaredVariable: Entity<*>? = EntityFactory.getInstance().wrap(declaration.element!!)

            if(declaredVariable != null) {
                declaredVariable.parent = declaration
                return declaredVariable
            }

            return null
        }
    }
}