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
import spoon.reflect.reference.CtTypeReference

public class PrimitiveTypeEntity(reference: CtTypeReference<*>): TypeEntity<CtType<*>>(reference) {
    protected override fun getType(): RDFNode {
        return Ontology.PRIMITIVE_ENTITY
    }

    public override fun buildRelativeURI(): String {
        val uri: String =  super.buildRelativeURI()
        return uri.substring(0, 1).uppercase() + uri.substring(1)
    }

    public override fun extract() {
        tagType()
        tagName()
    }
}