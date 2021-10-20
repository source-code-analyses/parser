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

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.RDFNode
import org.codeontology.Ontology
import org.codeontology.extraction.Entity
import org.codeontology.extraction.RDFLogger
import spoon.reflect.declaration.CtClass
import spoon.reflect.reference.CtTypeReference

public class AnonymousClassEntity<T> public constructor(anonymousClass: CtClass<T>):
    ClassEntity<T>(anonymousClass) {
    companion object {
        @JvmStatic public val TAG: String = "anonymous-class"
    }

    public val requestedResources: HashSet<Entity<*>> = HashSet()

    public override fun buildRelativeURI(): String {
        return parent!!.getRelativeURI() + SEPARATOR + TAG + SEPARATOR + element!!.simpleName
    }

    public override fun getType(): RDFNode {
        return Ontology.ANONYMOUS_CLASS_ENTITY
    }

    public override fun extract() {
        tagType()
        tagSuperType()
        tagComment()
        tagFields()
        tagMethods()
        tagSourceCode()
        tagNestedTypes()
    }

    public fun tagSuperType() {
        val references: Set<CtTypeReference<*>> = getReference().superInterfaces
        val superTypeReference: CtTypeReference<*>
        val property: Property
        if (references.isEmpty()) {
            superTypeReference = getReference().superclass
            property = Ontology.EXTENDS_PROPERTY
        } else {
            superTypeReference = references.toTypedArray()[0]
            property = Ontology.IMPLEMENTS_PROPERTY
        }
        val superType: TypeEntity<*>? = getFactory().wrap(superTypeReference)
        superType!!.parent = this.parent!!
        getLogger().addTriple(this, property, superType)
        requestedResources.add(superType)
        superType.follow()
    }

    public override fun tagMethods() {
        val methods: List<MethodEntity> = getMethods()
        for(method: MethodEntity in methods) {
            method.extract()
            requestedResources.addAll(method.getRequestedResources())
        }
    }

    public override fun tagFields() {
        val fields: List<FieldEntity> = getFields()
        for (field: FieldEntity in fields) {
            field.extract()
            requestedResources.add(field.getJavaType())
        }
    }
}