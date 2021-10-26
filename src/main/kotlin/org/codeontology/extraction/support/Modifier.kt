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

package org.codeontology.extraction.support

import org.apache.jena.rdf.model.Resource
import org.codeontology.Ontology
import spoon.reflect.declaration.ModifierKind

enum class Modifier(private val individual: Resource) {
    PUBLIC (Ontology.PUBLIC_INDIVIDUAL),
    PRIVATE (Ontology.PRIVATE_INDIVIDUAL),
    PROTECTED (Ontology.PROTECTED_INDIVIDUAL),
    DEFAULT (Ontology.DEFAULT_INDIVIDUAL),
    ABSTRACT (Ontology.ABSTRACT_INDIVIDUAL),
    FINAL (Ontology.FINAL_INDIVIDUAL),
    STATIC (Ontology.STATIC_INDIVIDUAL),
    SYNCHRONIZED (Ontology.SYNCHRONIZED_INDIVIDUAL),
    VOLATILE (Ontology.VOLATILE_INDIVIDUAL);

    fun getIndividual(): Resource {
        return individual
    }

    companion object {
        @JvmStatic
        fun asList(set: Set<ModifierKind>): List<Modifier> {
            val list = ArrayList<Modifier>()

            for(current in set) {
                val modifier: Modifier = valueOf(current)

                list.add(modifier)
            }

            return list
        }

        @JvmStatic
        fun asList(code: Int): List<Modifier> {
            val list = ArrayList<Modifier>()

            if(java.lang.reflect.Modifier.isPublic(code)) {
                list.add(PUBLIC)
            }
            else if(java.lang.reflect.Modifier.isPrivate(code)) {
                list.add(PRIVATE)
            }
            else if(java.lang.reflect.Modifier.isProtected(code)) {
                list.add(PROTECTED)
            }
            else {
                list.add(DEFAULT)
            }

            if(java.lang.reflect.Modifier.isAbstract(code)) {
                list.add(ABSTRACT)
            }

            if(java.lang.reflect.Modifier.isFinal(code)) {
                list.add(FINAL)
            }

            if(java.lang.reflect.Modifier.isStatic(code)) {
                list.add(STATIC)
            }

            if(java.lang.reflect.Modifier.isSynchronized(code)) {
                list.add(SYNCHRONIZED)
            }

            if(java.lang.reflect.Modifier.isVolatile(code)) {
                list.add(VOLATILE)
            }

            return list
        }

        @JvmStatic
        fun valueOf(modifier: ModifierKind): Modifier {
            return when(modifier) {
                ModifierKind.PUBLIC -> PUBLIC
                ModifierKind.PRIVATE -> PRIVATE
                ModifierKind.PROTECTED -> PROTECTED
                ModifierKind.ABSTRACT -> ABSTRACT
                ModifierKind.FINAL -> FINAL
                ModifierKind.STATIC -> STATIC
                ModifierKind.SYNCHRONIZED -> SYNCHRONIZED
                ModifierKind.VOLATILE -> VOLATILE
                else -> DEFAULT
            }
        }
    }
}