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

import spoon.reflect.declaration.CtAnnotationType
import spoon.reflect.declaration.CtEnum
import spoon.reflect.declaration.CtInterface
import spoon.reflect.declaration.CtType
import spoon.reflect.reference.CtArrayTypeReference
import spoon.reflect.reference.CtTypeParameterReference
import spoon.reflect.reference.CtTypeReference
import spoon.support.SpoonClassNotFoundException

public enum class TypeKind {
    CLASS,
    INTERFACE,
    ANNOTATION,
    ENUM,
    PRIMITIVE,
    ARRAY,
    TYPE_VARIABLE,
    PARAMETERIZED_TYPE;

    companion object {
         @JvmStatic public fun getKindOf(reference: CtTypeReference<*>): TypeKind? {
            if (reference is CtArrayTypeReference<*>) {
                return ARRAY
            }

            if (reference is CtTypeParameterReference) {
                return TYPE_VARIABLE
            }

            if (reference.actualTypeArguments.isNotEmpty()) {
                return PARAMETERIZED_TYPE
            }

            val type: CtType<*>? = reference.declaration
            if (type != null) {
                return getKindOf(type)
            }

            if (reference.isPrimitive) {
                return PRIMITIVE
            }
            return try {
                val actualClass: Class<*> = reference.javaClass
                if (actualClass.isAnnotation) {
                    ANNOTATION
                } else if (actualClass.isEnum) {
                    ENUM
                } else if (actualClass.isInterface) {
                    INTERFACE
                } else {
                    CLASS
                }
            } catch (e: SpoonClassNotFoundException) {
                null
            }
        }

        @JvmStatic public fun getKindOf(type: CtType<*>): TypeKind {
            return if (type.reference is CtArrayTypeReference) {
                ARRAY
            } else if (type.reference.actualTypeArguments.size > 0) {
                PARAMETERIZED_TYPE
            } else if (type.isPrimitive) {
                PRIMITIVE
            }  else if (type is CtAnnotationType<*>) {
                ANNOTATION
            } else if (type is CtEnum<*>) {
                ENUM
            } else if (type is CtInterface<*>) {
                INTERFACE
            } else {
                CLASS
            }
        }
    }
}