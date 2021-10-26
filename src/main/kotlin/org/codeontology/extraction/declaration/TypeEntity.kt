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

import org.apache.jena.rdf.model.Property
import org.codeontology.CodeOntology
import org.codeontology.Ontology
import org.codeontology.extraction.NamedElementEntity
import org.codeontology.extraction.ReflectionFactory
import org.codeontology.extraction.support.ModifiableEntity
import org.codeontology.extraction.support.Modifier
import spoon.reflect.declaration.CtField
import spoon.reflect.declaration.CtMethod
import spoon.reflect.declaration.CtType
import spoon.reflect.reference.CtExecutableReference
import spoon.reflect.reference.CtFieldReference
import spoon.reflect.reference.CtTypeReference
import java.lang.reflect.Field
import java.lang.reflect.Method

abstract class TypeEntity<T: CtType<*>>: NamedElementEntity<T>, ModifiableEntity<T> {
    private var methods: ArrayList<MethodEntity>? = null
    private var fields: ArrayList<FieldEntity>? = null

    constructor(type: T): super(type) {
        checkNullType()
    }

    constructor(reference: CtTypeReference<*>): super(reference) {
        checkNullType()
    }

    private fun checkNullType() {
        if (getReference().qualifiedName.equals(CtTypeReference.NULL_TYPE_NAME)) {
            throw NullTypeException()
        }
    }

    override fun buildRelativeURI(): String {
        return getReference().qualifiedName
    }

    fun getReference(): CtTypeReference<*> {
        return reference as CtTypeReference<*>
    }

    fun tagSuperInterfaces(property: Property) {
        val references: Set<CtTypeReference<*>> = getReference().superInterfaces

        for(reference: CtTypeReference<*> in references) {
            val superInterface: TypeEntity<*> = getFactory().wrap(reference)!!
            superInterface.parent = this
            getLogger().addTriple(this, property, superInterface.getResource())
            superInterface.follow()
        }
    }

    open fun tagMethods() {
        val methods: List<MethodEntity> = getMethods()
        methods.forEach { method ->
            getLogger().addTriple(this, Ontology.HAS_METHOD_PROPERTY, method)
        }
        methods.forEach(MethodEntity::extract)
    }

    fun getMethods(): List<MethodEntity> {
        if (methods == null) {
            setMethods()
        }
        return methods!!
    }

    private fun setMethods() {
        methods = ArrayList()

        if (!isDeclarationAvailable()) {
            setMethodsByReflection()
            return
        }

        val ctMethods: Set<CtMethod<*>> = element?.methods ?: HashSet()
        for(ctMethod: CtMethod<*> in ctMethods) {
            val method: MethodEntity = getFactory().wrap(ctMethod)
            method.parent = this
            methods!!.add(method)
        }
    }

    private fun setMethodsByReflection() {
        try {
            val actualMethods: Array<Method> = getReference().javaClass.declaredMethods
            for (actualMethod: Method in actualMethods) {
                val reference: CtExecutableReference<*> = ReflectionFactory.getInstance().createMethod(actualMethod)
                val method: MethodEntity = getFactory().wrap(reference) as MethodEntity
                method.parent = this
                methods?.add(method)
            }
        } catch (t: Throwable) {
            showMemberAccessWarning()
        }
    }

    fun getFields(): List<FieldEntity> {
        if (fields == null) {
            setFields()
        }

        return fields!!
    }

    private fun setFields() {
        fields = ArrayList()
        if (!isDeclarationAvailable()) {
            setFieldsByReflection()
            return
        }

        val ctFields: List<CtField<*>> = element?.fields ?: ArrayList()
        for(current: CtField<*> in ctFields) {
            val currentField: FieldEntity = getFactory().wrap(current)
            currentField.parent = this
            fields!!.add(currentField)
        }
    }

    private fun setFieldsByReflection() {
        try {
            val actualFields: Array<Field> = getReference().javaClass.fields
            for(current: Field in actualFields) {
                val reference: CtFieldReference<*> = ReflectionFactory.getInstance().createField(current)
                val currentField: FieldEntity = getFactory().wrap(reference)
                currentField.parent = this
                fields?.add(currentField)
            }
        } catch (t: Throwable) {
            showMemberAccessWarning()
        }
    }

    open fun tagFields() {
        val fields: List<FieldEntity> = getFields()
        fields.forEach{ field -> getLogger().addTriple(this, Ontology.HAS_FIELD_PROPERTY, field) }
        fields.forEach(FieldEntity::extract)
    }

    override fun getModifiers(): List<Modifier> {
        return if(isDeclarationAvailable()) {
            Modifier.asList(element?.modifiers ?: HashSet())
        } else {
            Modifier.asList(getReference().javaClass.modifiers)
        }
    }

    override fun tagModifiers() {
        ModifiableTagger(this).tagModifiers()
    }

    protected fun showMemberAccessWarning() {
        if (CodeOntology.verboseMode()) {
           CodeOntology.showWarning("Could not extract members of ${getReference().qualifiedName}")
        }
    }
}