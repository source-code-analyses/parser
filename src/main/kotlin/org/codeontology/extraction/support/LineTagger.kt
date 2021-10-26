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

import org.apache.jena.rdf.model.Literal
import org.codeontology.Ontology
import org.codeontology.extraction.CodeElementEntity
import org.codeontology.extraction.RDFLogger
import spoon.reflect.cu.SourcePosition

class LineTagger(private val entity: CodeElementEntity<*>) {
    private val position: SourcePosition = entity.element!!.position

    fun tagLine() {
        val line: Literal = entity.model.createTypedLiteral(position.line)
        RDFLogger.getInstance().addTriple(entity, Ontology.LINE_PROPERTY, line)
    }

    fun tagEndLine() {
        val endLine: Literal = entity.model.createTypedLiteral(position.endLine)
        RDFLogger.getInstance().addTriple(entity, Ontology.END_LINE_PROPERTY, endLine)
    }
}