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

import org.codeontology.Ontology
import org.codeontology.extraction.RDFLogger
import org.codeontology.extraction.support.ModifiableEntity
import org.codeontology.extraction.support.Modifier

class ModifiableTagger(private val modifiable: ModifiableEntity<*>) {
    fun tagModifiers() {
        val modifiers: List<Modifier> = modifiable.getModifiers()

        for(modifier: Modifier in modifiers) {
            RDFLogger.getInstance().addTriple(modifiable, Ontology.MODIFIER_PROPERTY, modifier.getIndividual())
        }
    }
}