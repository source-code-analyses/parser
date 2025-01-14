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

import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.codeontology.Ontology
import spoon.SpoonException

abstract class AbstractEntity<E>(element: E?): Entity<E>, Comparable<Entity<*>> {
    public override var element: E? = element
        protected set

    override var parent: Entity<*>? = null
    private lateinit var uri: String

    override fun getResource(): Resource {
        return model.createResource(Ontology.WOC + getRelativeURI())
    }

    protected abstract fun buildRelativeURI(): String

    fun tagType() {
        getLogger().addTriple(this, Ontology.RDF_TYPE_PROPERTY, getType())
    }

    protected abstract fun getType(): RDFNode?

    fun tagSourceCode() {
        getLogger().addTriple(this, Ontology.SOURCE_CODE_PROPERTY, model.createLiteral(getSourceCode()))
    }

    open fun getSourceCode(): String {
        return try {
            element.toString()
        } catch (e: SpoonException) {
            ""
        }
    }


    override fun getFactory(): EntityFactory {
        return EntityFactory.getInstance()
    }

    override fun getLogger(): RDFLogger {
        return RDFLogger.getInstance()
    }

    override fun follow() {
        extract()
    }

    final override fun getRelativeURI(): String {
        // assert(uri == null)

        if(!this::uri.isInitialized) {
            uri = buildRelativeURI()
        }

        return uri
    }

    override fun equals(other: Any?): Boolean {
        if(other !is Entity<*>) {
            return false
        }

        val otherEntity: Entity<*> = other
        return otherEntity.getRelativeURI() == this.getRelativeURI()
    }

    override fun hashCode(): Int {
        return getRelativeURI().hashCode()
    }

    override fun getParent(vararg classes: Class<*>): Entity<*>? {
        var parent: Entity<*>? = this.parent
        while (parent != null) {
            for(currentClass: Class<*> in classes) {
                if (currentClass.isAssignableFrom(parent.javaClass)) {
                    return parent
                }
            }
            parent = parent.parent
        }

        return null
    }

    override fun compareTo(other: Entity<*>): Int {
        return this.getRelativeURI().compareTo(other.getRelativeURI())
    }
}

