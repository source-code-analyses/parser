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

import org.apache.jena.rdf.model.Literal
import org.codeontology.CodeOntology
import org.codeontology.Ontology
import spoon.reflect.declaration.CtNamedElement
import spoon.reflect.reference.CtReference

abstract class NamedElementEntity<E: CtNamedElement>: CodeElementEntity<E> {
    public final override var element: E? = null
        set(value) {
            if (value == null) {
                throw IllegalArgumentException()
            }
            field = value
            if (reference == null) {
                try {
                    this.reference = field!!.reference
                } catch (e: ClassCastException) {
                    // leave reference null
                }
            }
        }

    var reference: CtReference? = null
        private set(value) {
            field = value
            if (reference?.declaration != null && element == null) {
                this.element = reference!!.declaration as E?
            }
        }

    protected constructor(element: E) : super() {
        this.element = element
    }

    protected constructor(reference: CtReference) : super() {
        this.reference = reference
    }

    open fun getName(): String {
        return reference?.simpleName ?: ""
    }

    fun tagName() {
        val name: Literal = model.createTypedLiteral(getName())
        getLogger().addTriple(this, Ontology.NAME_PROPERTY, name)
    }

    fun tagLabel() {
        val labelString: String = splitCamelCase(getName())
        val label: Literal = model.createTypedLiteral(labelString)
        getLogger().addTriple(this, Ontology.RDFS_LABEL_PROPERTY, label)
    }

    fun splitCamelCase(s: String): String {
        return s.replace(
            String.format("%s|%s|%s",
                "(?<=[A-Z])(?=[A-Z][a-z])",
                "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"
            ), " "
        )
    }

    override fun follow() {
        if (!isDeclarationAvailable() && !CodeOntology.isJarExplorationEnabled()
                && EntityRegister.getInstance().add(this)) {
            extract()
        }
    }

    open fun isDeclarationAvailable(): Boolean {
        return element != null
    }
}