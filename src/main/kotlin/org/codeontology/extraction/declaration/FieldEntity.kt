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
import org.codeontology.extraction.Entity
import org.codeontology.extraction.NamedElementEntity
import org.codeontology.extraction.ReflectionFactory
import org.codeontology.extraction.statement.FieldDeclaration
import org.codeontology.extraction.support.*
import spoon.reflect.declaration.CtField
import spoon.reflect.reference.CtFieldReference
import spoon.reflect.reference.CtTypeReference
import java.lang.Error

import java.lang.reflect.Field
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

class FieldEntity: NamedElementEntity<CtField<*>>, ModifiableEntity<CtField<*>>, MemberEntity<CtField<*>>, TypedElementEntity<CtField<*>> {
    constructor(field: CtField<*>): super(field)

    constructor(field: CtFieldReference<*>): super(field)

    override fun buildRelativeURI(): String {
        var uri = (reference?.simpleName ?: "")
        val declarator = getDeclaringElement()

        if(declarator != null) {
            uri = declarator.getRelativeURI() + SEPARATOR + uri
        }

        return uri
    }

    override fun getType(): RDFNode {
        return Ontology.FIELD_ENTITY
    }

    override fun extract() {
        tagName()
        tagLabel()
        tagType()
        tagDeclaringElement()
        tagJavaType()
        tagModifiers()
        if (isDeclarationAvailable()) {
            tagSourceCode()
            tagComment()
            tagAnnotations()
            if (CodeOntology.processStatements()) {
                tagDeclaration()
            }
        }
    }

    override fun getModifiers(): List<Modifier> {
        if (isDeclarationAvailable()) {
            return Modifier.asList(element?.modifiers ?: HashSet())
        }
        return try {
            Modifier.asList((reference as CtFieldReference<*>).modifiers)
        } catch (e: Exception) {
            ArrayList()
        } catch(e: Error) {
            ArrayList()
        }
    }

    override fun tagModifiers() {
        ModifiableTagger(this).tagModifiers()
    }

    override fun getJavaType(): TypeEntity<*> {
        var type: TypeEntity<*>?
        if (isDeclarationAvailable()) {
            type = getFactory().wrap(element!!.type)
        } else {
            type = getGenericType()
            if (type == null) {
                var typeReference: CtTypeReference<*>? = (reference as CtFieldReference<*>).type

                if(typeReference == null) {
                    typeReference = (reference as CtFieldReference<*>).declaringType
                }

                type = getFactory().wrap(typeReference)
            }
        }

        type!!.parent = getDeclaringElement()
        return type
    }

    private fun getGenericType(): TypeEntity<*>? {
        var result: TypeEntity<*>? = null
        if (isDeclarationAvailable()) {
            return null
        }
        try {
            val reference: CtFieldReference<*> = this.reference as CtFieldReference<*>
            val field: Field = reference.actualField as Field
            val genericType: Type = field.genericType

            if (genericType is GenericArrayType || genericType is TypeVariable<*>) {
                result = getFactory().wrap(genericType)
            }

        } catch (t: Throwable) {
            return null
        }

        return result
    }

    override fun tagJavaType() {
        JavaTypeTagger(this).tagJavaType()
    }

    override fun getDeclaringElement(): Entity<*>? {
        return if(isDeclarationAvailable()) {
            getFactory().wrap(element!!.declaringType)
        } else {
            val reference: CtFieldReference<*> = this.reference as CtFieldReference<*>
            val declaringType: CtTypeReference<*>? = ReflectionFactory.getInstance().clone(reference.declaringType)

            declaringType?.setActualTypeArguments<CtTypeReference<*>>(ArrayList())

            getFactory().wrap(declaringType)
        }
    }

    override fun tagDeclaringElement() {
        DeclaringElementTagger(this).tagDeclaredBy()
    }

    private fun tagDeclaration() {
        val declaration = FieldDeclaration(element!!)
        declaration.parent = this
        declaration.extract()
    }
}