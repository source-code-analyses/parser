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

package org.codeontology.extraction.project

import org.apache.jena.rdf.model.RDFNode
import org.codeontology.Ontology
import org.codeontology.build.maven.MavenProject
import org.codeontology.extraction.Entity

public class MavenProjectEntity(project: MavenProject): ProjectEntity<MavenProject>(project) {
    protected override fun getType(): RDFNode {
        return Ontology.MAVEN_PROJECT_ENTITY
    }
}