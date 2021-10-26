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

import org.codeontology.Ontology
import org.codeontology.extraction.declaration.ExecutableEntity
import org.codeontology.extraction.declaration.TypeEntity
import spoon.reflect.code.CtAbstractInvocation
import spoon.reflect.code.CtExpression
import spoon.reflect.reference.CtExecutableReference

abstract class AbstractInvocationExpressionEntity<T>(expression: T): ExpressionEntity<T>(expression) where T: CtAbstractInvocation<*>, T: CtExpression<*> {
    override fun extract() {
        super.extract()
        tagExecutable()
        tagArguments()
    }

    fun tagExecutable() {
        val executable: ExecutableEntity<*>? = getExecutable()
        if (executable != null) {
            getLogger().addTriple(this, Ontology.INVOKES_PROPERTY, executable)
            executable.follow()
        }
    }

    fun tagArguments() {
        val arguments: List<ActualArgumentEntity> = getArguments()
        for(argument: ActualArgumentEntity in arguments) {
            getLogger().addTriple(this, Ontology.ARGUMENT_PROPERTY, argument)
            argument.extract()
        }
    }

    fun getExecutable(): ExecutableEntity<*>? {
        val reference: CtExecutableReference<*>? = element?.executable
        if (reference != null) {
            val executable: ExecutableEntity<*> = getFactory().wrap(reference)
            val declaringType: TypeEntity<*> = getFactory().wrap(reference.declaringType)!!
            executable.parent = declaringType
            return executable
        }

        return null
    }

    fun getArguments(): List<ActualArgumentEntity> {
        val expressions: List<CtExpression<*>>? = element?.arguments
        val arguments: ArrayList<ActualArgumentEntity> = ArrayList()

        if (expressions == null) {
            return arguments
        }

        for (i in expressions.indices) {
            val expression: ExpressionEntity<*> = getFactory().wrap(expressions[i])
            val argument = ActualArgumentEntity(expression)
            expression.parent = argument
            argument.parent = this
            argument.position = i
            arguments.add(argument)
        }

        return arguments
    }
}