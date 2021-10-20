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

import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.RDFNode
import org.codeontology.Ontology
import org.codeontology.docparser.DocCommentParser
import org.codeontology.docparser.Tag
import org.codeontology.extraction.Entity
import org.codeontology.extraction.ReflectionFactory
import org.codeontology.extraction.support.FormalTypeParametersTagger
import org.codeontology.extraction.support.GenericDeclarationEntity
import spoon.reflect.declaration.CtMethod
import spoon.reflect.reference.CtExecutableReference
import spoon.reflect.reference.CtTypeReference

import java.lang.reflect.GenericArrayType
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

public class MethodEntity: ExecutableEntity<CtMethod<*>>, GenericDeclarationEntity<CtMethod<*>> {
    public constructor(method: CtMethod<*>): super(method)
    public constructor(reference: CtExecutableReference<*>): super(reference)

    protected override fun getType(): RDFNode {
        return Ontology.METHOD_ENTITY
    }

    public override fun extract() {
        super.extract()
        tagReturns()
        if (isDeclarationAvailable()) {
            tagOverrides()
            tagFormalTypeParameters()
            tagReturnDescription()
        }
    }

    public fun tagOverrides() {
        try {
            val reference: CtExecutableReference<*>? = (this.reference as CtExecutableReference<*>).overridingExecutable
            if (reference != null) {
                val overridingMethod: ExecutableEntity<*> = getFactory().wrap(reference)
                getLogger().addTriple(this, Ontology.OVERRIDES_PROPERTY, overridingMethod)
                overridingMethod.follow()
            }
        } catch (e: Exception) {
            // could not get an overriding executable
        } catch (e: Error) {
            // could not get an overriding executable
        }
    }

    public fun tagReturns() {
        getLogger().addTriple(this, Ontology.RETURN_TYPE_PROPERTY, getReturnType())
    }

    private fun getReturnType(): TypeEntity<*> {
        var returnType: TypeEntity<*>? = getGenericReturnType()
        if (returnType != null) {
            return returnType
        }

        val reference: CtTypeReference<*> = (this.reference as CtExecutableReference<*>).type
        returnType = getFactory().wrap(reference)
        returnType?.parent = this
        returnType?.follow()

        return returnType!!
    }

    private fun getGenericReturnType(): TypeEntity<*>? {
        if (!isDeclarationAvailable()) {
            return null
        }
        try {
            val reference: CtExecutableReference<*> = this.reference as CtExecutableReference<*>
            val method: Method = ReflectionFactory.getInstance().createActualExecutable(reference) as Method
            val returnType: Type = method.genericReturnType

            if (returnType is GenericArrayType ||
                returnType is TypeVariable<*> ) {

                val result: TypeEntity<*> = getFactory().wrap(returnType)!!
                result.parent = this
                return result
            }

            return null

        } catch (t: Throwable) {
            return null
        }
    }

    public override fun getFormalTypeParameters(): List<TypeVariableEntity> {
        return FormalTypeParametersTagger.formalTypeParametersOf(this)
    }

    public override fun tagFormalTypeParameters() {
        FormalTypeParametersTagger(this).tagFormalTypeParameters()
    }

    public fun getReturnDescription(): String? {
        val comment: String = element?.docComment ?: return null

        val parser = DocCommentParser(comment)
        val tags: List<Tag> = parser.getReturnTags()
        if (tags.isEmpty()) {
            return null
        }

        return tags[0].text
    }

    public fun tagReturnDescription() {
        val description: String? = getReturnDescription()
        if (getReturnDescription() != null) {
            val literal: Literal = model.createTypedLiteral(description)
            getLogger().addTriple(this, Ontology.RETURN_DESCRIPTION_PROPERTY, literal)
        }
    }
}