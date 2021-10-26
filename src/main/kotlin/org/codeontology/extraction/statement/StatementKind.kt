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

import spoon.reflect.code.*
import spoon.reflect.declaration.CtClass

enum class StatementKind {
    BLOCK,
    IF_THEN_ELSE,
    SWITCH,
    WHILE,
    DO,
    FOR,
    FOREACH,
    TRY,
    RETURN,
    THROW,
    BREAK,
    CONTINUE,
    ASSERT,
    SYNCHRONIZED,
    EXPRESSION_STATEMENT,
    LOCAL_VARIABLE_DECLARATION,
    CLASS_DECLARATION,
    STATEMENT;

    companion object {
        @JvmStatic
        fun getKindOf(statement: CtStatement): StatementKind {
            return when (statement) {
                is CtBlock<*> -> BLOCK
                is CtIf -> IF_THEN_ELSE
                is CtSwitch<*> -> SWITCH
                is CtWhile -> WHILE
                is CtDo -> DO
                is CtFor -> FOR
                is CtForEach -> FOREACH
                is CtTry -> TRY
                is CtReturn<*> -> RETURN
                is CtThrow -> THROW
                is CtBreak -> BREAK
                is CtContinue -> CONTINUE
                is CtAssert<*> -> ASSERT
                is CtSynchronized -> SYNCHRONIZED
                is CtLocalVariable<*> -> LOCAL_VARIABLE_DECLARATION
                is CtClass<*> -> CLASS_DECLARATION
                is CtExpression<*> -> EXPRESSION_STATEMENT
                else -> STATEMENT
            }
        }
    }
}