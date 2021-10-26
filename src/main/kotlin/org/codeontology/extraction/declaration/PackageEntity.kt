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
import org.codeontology.extraction.NamedElementEntity
import org.codeontology.extraction.ReflectionFactory
import spoon.reflect.declaration.CtPackage
import spoon.reflect.declaration.CtType
import spoon.reflect.reference.CtPackageReference
import spoon.reflect.reference.CtTypeReference

class PackageEntity: NamedElementEntity<CtPackage> {
    var types: ArrayList<TypeEntity<*>> = setTypes()
        private set

    constructor(pack: CtPackage): super(pack)

    constructor(pack: CtPackageReference): super(pack)

    override fun buildRelativeURI(): String {
        val relativeURI: String = getPackageName()
        return relativeURI.replace(" ", SEPARATOR)
    }

    override fun getType(): RDFNode {
        return Ontology.PACKAGE_ENTITY
    }

    override fun extract() {
        val types: Collection<TypeEntity<*>> = types

        if (types.isEmpty()) {
            return
        }

        tagType()
        tagName()
        tagLabel()
        tagTypes()
        tagParent()

        if (isDeclarationAvailable()) {
            tagComment()
        }
    }

    fun tagParent() {
        if (CodeOntology.extractProjectStructure()) {
            getLogger().addTriple(this, Ontology.PROJECT_PROPERTY, parent!!)
        }
    }

    fun tagTypes() {
        for(type: TypeEntity<*> in types) {
            getLogger().addTriple(this, Ontology.IS_PACKAGE_OF_PROPERTY, type)
            getLogger().addTriple(type, Ontology.HAS_PACKAGE_PROPERTY, this)
            if (CodeOntology.verboseMode()) {
                println("Running on ${type.getReference().qualifiedName}")
            }
            type.extract()
        }
    }

     private fun setTypes(): ArrayList<TypeEntity<*>> {
         val array: ArrayList<TypeEntity<*>> = ArrayList()
         if (isDeclarationAvailable()) {
             val ctTypes: Set<CtType<*>> = element?.types ?: HashSet()
             ctTypes.stream().map{ current -> getFactory().wrap(current) }.forEach { type -> array.add(type!!) }
         }

         return array
     }

    private fun getPackageName(): String {
        return if(isDeclarationAvailable()) {
            element?.qualifiedName ?: ""
        } else {
            (reference as CtPackageReference).actualPackage.name
        }
    }

    fun setTypes(types: List<Class<*>>) {
        this.types = ArrayList()
        for(type: Class<*> in types) {
            val reference: CtTypeReference<*> = ReflectionFactory.getInstance().createTypeReference(type)
            this.types.add(getFactory().wrap(reference)!!)
        }
    }
}