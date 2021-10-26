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

import org.codeontology.extraction.EntityFactory
import org.codeontology.build.DefaultProject
import org.codeontology.build.gradle.AndroidProject
import org.codeontology.build.gradle.GradleProject
import org.codeontology.build.maven.MavenProject

class ProjectVisitor {
    private var lastEntity: ProjectEntity<*>? = null

    fun visit(project: DefaultProject) {
        lastEntity = EntityFactory.getInstance().wrap(project)
    }

    fun visit(project: GradleProject) {
        lastEntity = EntityFactory.getInstance().wrap(project)
    }

    fun visit(project: MavenProject) {
        lastEntity = EntityFactory.getInstance().wrap(project)
    }

    fun visit(project: AndroidProject) {
        lastEntity = EntityFactory.getInstance().wrap(project)
    }

    fun getLastEntity(): ProjectEntity<*>? {
        return lastEntity
    }
}