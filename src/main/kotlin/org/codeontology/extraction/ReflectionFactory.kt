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

package org.codeontology.extraction

import spoon.reflect.declaration.CtElement
import spoon.reflect.factory.Factory
import spoon.reflect.reference.*

import java.lang.reflect.*

public class ReflectionFactory {
    private val previousVariables: HashSet<TypeVariable<*>> = hashSetOf()
    public lateinit var parent: Factory

    companion object {
        @JvmStatic private var instance: ReflectionFactory? = null
        @JvmStatic public fun getInstance(): ReflectionFactory {
            if(instance == null) {
                instance = ReflectionFactory()
            }

            return instance as ReflectionFactory
        }
    }

    public fun createParameterizedTypeReference(parameterizedType: ParameterizedType): CtTypeReference<*> {
        val actualTypeArguments = parameterizedType.actualTypeArguments
        val rawType = parameterizedType.rawType

        val reference: CtTypeReference<*> = if(rawType is Class<*>) {
            parent.Type().createReference(rawType)
        } else {
            parent.Type().createReference<CtTypeReference<*>>(rawType.typeName)
        }

        for (actualArgument: Type in actualTypeArguments) {
            reference.addActualTypeArgument<CtTypeReference<*>>(createTypeReference(actualArgument))
        }

        return reference
    }

    public fun createTypeReference(type: Type): CtTypeReference<*> {
        val reference: CtTypeReference<*> = when(type) {
            is ParameterizedType -> createParameterizedTypeReference(type)
            is TypeVariable<*> -> createTypeVariableReference(type)
            is GenericArrayType -> createGenericArrayReference(type)
            is Class<*> -> parent.Type().createReference(type)
            is WildcardType -> createWildcardReference(type)
            else -> parent.Type().createReference<CtTypeReference<*>>(type.typeName)
        }

        return reference
    }

    public fun createGenericArrayReference(array: GenericArrayType): CtTypeReference<*> {
        var type: Type = array

        var i = 0
        do {
            i++
            type = (type as GenericArrayType).genericComponentType
        } while (type is GenericArrayType)

        val componentType: CtTypeReference<*> = if (type is TypeVariable<*>) {
            createTypeVariableReference(type)
        } else {
            createParameterizedTypeReference(type as ParameterizedType)
        }
        return parent.Type().createArrayReference(componentType, i)
    }

    public fun createTypeVariableReference(typeVariable: TypeVariable<*>): CtTypeParameterReference {
        if (previousVariables.contains(typeVariable)) {
            return parent.Type().createTypeParameterReference(typeVariable.name)
        }

        previousVariables.add(typeVariable)

        val name: String = typeVariable.name
        val bounds: Array<Type> = typeVariable.bounds

        val boundsList: ArrayList<CtTypeReference<*>> = ArrayList()

        for(bound: Type in bounds) {
            boundsList.add(createTypeReference(bound))
        }

        previousVariables.remove(typeVariable)

        val typeParameter = parent.Type().createTypeParameterReference(name)
        typeParameter.setActualTypeArguments<CtTypeReference<*>>(boundsList)

        return typeParameter
    }

    public fun createWildcardReference(wildcard: WildcardType): CtTypeParameterReference {
        val name = "?"
        val upperBounds: Array<Type> = wildcard.upperBounds
        val lowerBounds: Array<Type> = wildcard.lowerBounds

        val boundsList: ArrayList<CtTypeReference<*>> = ArrayList()

        for(bound: Type in upperBounds) {
            boundsList.add(createTypeReference(bound))
        }

        for(bound: Type in lowerBounds) {
            boundsList.add(createTypeReference(bound))
        }

        val wildcardReference: CtWildcardReference = parent.Type().createTypeParameterReference(name) as CtWildcardReference
        wildcardReference.setActualTypeArguments<CtTypeReference<*>>(boundsList)
        wildcardReference.setUpper<CtWildcardReference>(upperBounds.isNotEmpty())

        return wildcardReference
    }

    public fun createActualExecutable(executableReference: CtExecutableReference<*>): Executable? {
        var executable: Executable? = null
        var declaringClass: Class<*>? = null

        try {
            executable = executableReference.actualMethod

            if (executable == null) {
                executable = executableReference.actualConstructor
            }

            try {
                if (executable != null) {
                    declaringClass = executable.declaringClass
                }
            } catch (e: Exception) {
                declaringClass = Class.forName(executableReference.declaringType.qualifiedName)
            } catch (e: Error) {
                declaringClass = Class.forName(executableReference.declaringType.qualifiedName)
            }

        } catch (e: Exception) {
            declaringClass = null
        } catch (e: Error) {
            declaringClass = null
        }

        if (executable == null && declaringClass != null) {
            var executables: Array<out Executable> = declaringClass.declaredMethods
            if (executableReference.isConstructor) {
                executables = declaringClass.declaredConstructors
            }

            for(current: Executable in executables) {
                if (current.name.equals(executableReference.simpleName) || current is Constructor<*>) {
                    if (current.parameterCount == executableReference.parameters.size) {
                        val parameters: List<CtTypeReference<*>> = executableReference.parameters
                        val classes: Array<Class<*>?> = Array(parameters.size) { null }
                        for(i in parameters.indices) {
                            classes[i] = parameters[i].typeDeclaration as Class<*>
                        }

                        var acc = true

                        val parameterTypes: Array<Class<*>> = current.parameterTypes
                        for(i in classes.indices) {
                            if(!acc) {
                                break
                            }

                            acc = classes[i]?.isAssignableFrom(parameterTypes[i]) ?: false
                        }

                        if (acc) {
                            executable = current
                            break
                        }
                    }
                }
            }
        }

        return executable
    }

    public fun createTypeReference(clazz: Class<*>): CtTypeReference<*> {
        return parent.Class().createReference(clazz)
    }

    public fun createPackageReference(pack: Package): CtPackageReference {
        return parent.Package().createReference(pack)
    }

    public fun createMethod(method: Method): CtExecutableReference<*> {
        return parent.Method().createReference<Method>(method)
    }

    public fun createConstructor(constructorEntity: Constructor<*>): CtExecutableReference<*> {
        return parent.Constructor().createReference(constructorEntity)
    }

    public fun createField(field: Field): CtFieldReference<*> {
        return parent.Field().createReference<Field>(field)
    }

    public fun<T: CtElement> clone(t: T): T {
        return parent.Core().clone(t)
    }
}