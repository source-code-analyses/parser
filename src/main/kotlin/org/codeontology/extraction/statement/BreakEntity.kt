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

package org.codeontology.extraction.statement

import org.apache.jena.rdf.model.RDFNode
import org.codeontology.Ontology
import org.codeontology.extraction.support.FlowBreakerEntity
import org.codeontology.extraction.support.TargetedLabelTagger
import spoon.reflect.code.CtBreak

class BreakEntity(element: CtBreak): StatementEntity<CtBreak>(element), FlowBreakerEntity<CtBreak> {
    override fun getType(): RDFNode {
        return Ontology.BREAK_ENTITY
    }

    override fun extract() {
        super.extract()
        tagTargetedLabel()
    }

    override fun tagTargetedLabel() {
        TargetedLabelTagger(this).tagTargetedLabel()
    }

    override fun getTargetedLabel(): String {
        return element!!.targetLabel
    }
}