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
import org.codeontology.Ontology
import org.codeontology.extraction.Entity
import org.codeontology.extraction.EntityRegister
import org.codeontology.extraction.ReflectionFactory
import spoon.reflect.declaration.CtType
import spoon.reflect.reference.CtTypeReference

class ParameterizedTypeEntity constructor(reference: CtTypeReference<*>) :
    TypeEntity<CtType<*>>(reference) {
    private val arguments: List<CtTypeReference<*>> = getReference().actualTypeArguments
    private var diamond: Boolean = false

    init {
        if (arguments.isNotEmpty()) {
            if (arguments[0].isImplicit) {
                diamond = true
            }
        }
    }

    override fun buildRelativeURI(): String {
        var uri: String = getReference().qualifiedName
        var argumentsString = ""
        val parent: Entity<*>? = this.parent

        if (diamond) {
            return uri + SEPARATOR + "diamond"
        }

        for(argument: CtTypeReference<*> in arguments) {
            val argumentEntity: TypeEntity<*> = getFactory().wrap(argument)!!

            argumentEntity.parent = parent

            argumentsString = if (argumentsString == "") {
                argumentEntity.getRelativeURI()
            } else {
                argumentsString + SEPARATOR + argumentEntity.getRelativeURI()
            }
        }

        uri = "$uri[$argumentsString]"
        uri = uri.replace(" ", "_")

        return uri
    }

    override fun extract() {
        tagType()
        tagGenericType()
        tagActualTypeArguments()
    }

    override fun getType(): RDFNode {
        return Ontology.PARAMETERIZED_TYPE_ENTITY
    }

    private fun tagGenericType() {
        val genericType: TypeEntity<*> = getGenericType()!!
        genericType.follow()
        getLogger().addTriple(this, Ontology.GENERIC_TYPE_PROPERTY, genericType)
    }

    fun getGenericType(): TypeEntity<*>? {
        val cloneReference: CtTypeReference<*> = ReflectionFactory.getInstance().clone(getReference())
        cloneReference.setActualTypeArguments<CtTypeReference<*>>(ArrayList())
        return getFactory().wrap(cloneReference)
    }

    private fun tagActualTypeArguments() {
        if (diamond) {
            return
        }

        for(i in arguments.indices) {
            val typeArgument = TypeArgumentEntity(arguments[i])
            typeArgument.position = i
            getLogger().addTriple(this, Ontology.ACTUAL_TYPE_ARGUMENT_PROPERTY, typeArgument)
            typeArgument.extract()
        }
    }

    override fun follow() {
        if(EntityRegister.getInstance().add(this))  {
            extract()
        }
    }

    inner class TypeArgumentEntity constructor(reference: CtTypeReference<*>) : TypeEntity<CtType<*>>(reference) {
        var position: Int = 0
        private val argument: TypeEntity<*> = getFactory().wrap(getReference())!!
        override var parent: Entity<*>? = super.parent
            set(value) {
                field = value
                argument.parent = value
            }

        init {
            this.parent = this@ParameterizedTypeEntity.parent
        }

        override fun extract() {
            tagType()
            tagJavaType()
            tagPosition()
        }

        private fun tagJavaType() {
            getLogger().addTriple(this, Ontology.JAVA_TYPE_PROPERTY, argument)
            argument.follow()
        }

        private fun tagPosition() {
            getLogger().addTriple(this, Ontology.POSITION_PROPERTY, model.createTypedLiteral(position))
        }

        override fun buildRelativeURI(): String {
            return this@ParameterizedTypeEntity.getRelativeURI() + SEPARATOR + position
        }

        override fun getType(): RDFNode {
            return Ontology.TYPE_ARGUMENT_ENTITY
        }
    }
}