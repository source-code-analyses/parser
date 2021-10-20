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
import org.apache.jena.rdf.model.RDFNode
import org.codeontology.Ontology
import org.codeontology.extraction.Entity
import org.codeontology.extraction.RDFLogger
import spoon.reflect.declaration.CtAnnotationType
import spoon.reflect.reference.CtTypeReference

public class AnnotationEntity(reference: CtTypeReference<*>):
    TypeEntity<CtAnnotationType<*>>(reference) {
    protected override fun getType(): RDFNode {
        return Ontology.ANNOTATION_ENTITY
    }

    public override fun extract() {
        tagType()
        tagName()
        tagLabel()
        if (isDeclarationAvailable()) {
            tagComment()
            tagSourceCode()
        }
    }
}