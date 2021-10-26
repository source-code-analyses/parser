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

import org.apache.jena.rdf.model.Literal
import org.codeontology.Ontology
import org.codeontology.extraction.AbstractEntity
import org.codeontology.extraction.Entity
import org.codeontology.build.Project
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

abstract class ProjectEntity<T: Project>(project: T): AbstractEntity<T>(project) {
    override fun buildRelativeURI(): String {
        val code: String
        if (element!!.getBuildFile() == null) {
            code = try {
                val names: Array<String> = Files.walk(Paths.get(element!!.getPath()))
                    .filter{ path -> path.toFile().path.endsWith(".java") }
                    .map{ path -> path.toFile().name }
                    .toList().toTypedArray()
                Arrays.sort(names)
                names.contentHashCode().toString()
            } catch (e: IOException) {
                "0"
            }
        } else {
            val subProjects: Array<String> = element!!.subProjects.stream().map(Project::getName).toList().toTypedArray()
            Arrays.sort(subProjects)
            val buildFileCode: Int = element!!.getBuildFileContent().hashCode()
            code = subProjects.contentHashCode().toString() + SEPARATOR + buildFileCode.toString()
        }

        return getPrefix() + element!!.getName() + SEPARATOR + code
    }

    override fun extract() {
        tagType()
        tagBuildFile()
        tagSubProjects()
        tagName()
    }

    fun tagName() {
        val name: String = element!!.getName()
        val label: Literal = model.createTypedLiteral(name)
        getLogger().addTriple(this, Ontology.RDFS_LABEL_PROPERTY, label)
    }

    fun tagSubProjects() {
        val subProjects: Collection<Project> = element!!.subProjects
        for (subProject: Project in subProjects) {
            val visitor = ProjectVisitor()
            subProject.accept(visitor)
            val entity: ProjectEntity<*>? = visitor.getLastEntity()
            entity!!.parent = this
            getLogger().addTriple(this, Ontology.SUBPROJECT_PROPERTY, entity)
            entity.extract()
        }
    }

    fun tagBuildFile() {
        if (element!!.getBuildFile() != null) {
            val buildFileContent: String = element!!.getBuildFileContent()
            val buildFileLiteral: Literal = model.createTypedLiteral(buildFileContent)
            getLogger().addTriple(this, Ontology.BUILD_FILE_PROPERTY, buildFileLiteral)
        }
    }

    private fun getPrefix(): String {
        val builder: StringBuilder = StringBuilder()
        var current: Entity<*>? = this
        while (current?.parent != null) {
            val parent: Entity<*>? = current.parent
            if (parent is ProjectEntity<*>) {
                builder.append(parent.element!!.getName())
                builder.append(SEPARATOR)
            }
            current = parent
        }

        return builder.toString()
    }
}