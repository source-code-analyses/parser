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
import spoon.reflect.declaration.CtType
import spoon.reflect.reference.CtArrayTypeReference
import spoon.reflect.reference.CtTypeReference

class ArrayEntity(reference: CtTypeReference<*>):
    TypeEntity<CtType<*>>(reference) {
    private val componentType: TypeEntity<*> = getFactory().wrap((getReference() as CtArrayTypeReference<*>).arrayType)!!
    override var parent: Entity<*>? = super.parent
        get() = super.parent
        set(value) {
            field = value
            componentType.parent = value
        }

    override fun extract() {
        tagType()
        tagName()
        tagLabel()
        tagArrayOf()
        tagDimensions()
    }

    override fun getType(): RDFNode {
        return Ontology.ARRAY_ENTITY
    }

    override fun buildRelativeURI(): String {
        return componentType.getRelativeURI() + "[]"
    }

    private fun tagArrayOf() {
        getLogger().addTriple(this, Ontology.ARRAY_OF_PROPERTY, componentType)
        componentType.follow()
    }

    private fun tagDimensions() {
        val dimensions: Int = (getReference() as CtArrayTypeReference<*>).dimensionCount
        getLogger().addTriple(this, Ontology.DIMENSIONS_PROPERTY, model.createTypedLiteral(dimensions))
    }

    override fun getName(): String {
        var componenTypeName: String = componentType.buildRelativeURI()
        if (componentType is PrimitiveTypeEntity) {
            componenTypeName = componenTypeName.lowercase()
        }
        return "$componenTypeName[]"
    }
}