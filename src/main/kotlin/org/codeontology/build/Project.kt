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

package org.codeontology.build

import org.codeontology.extraction.project.ProjectVisitor

import java.io.File
import java.io.FileNotFoundException
import java.util.ArrayList
import java.util.Scanner

public abstract class Project(projectDirectory: File) {
    public lateinit var subProjects: Collection<Project>
        protected set
    public var root = projectDirectory
    public open var projectDirectory: File = projectDirectory
        protected set

    protected abstract fun findSubProjects(): Collection<Project>

    public fun getFactory(): ProjectFactory {
        return ProjectFactory.getInstance()
    }

    public fun getBuildFileContent(): String {
        val buildPath: File? = getBuildFile()

        if (buildPath != null) {
            val scanner = Scanner(buildPath)
            return try {
                scanner.useDelimiter("\\Z")
                var build = ""
                if (scanner.hasNext()) {
                    build = scanner.next()
                }
                build
            } catch (e: FileNotFoundException) {
                ""
            } finally {
                scanner.close()
            }
        }

        return ""
    }

    public abstract fun getBuildFile(): File?

    public fun getPath(): String {
        return projectDirectory.path
    }

    public abstract fun getLoader(): DependenciesLoader<Project>

    protected open fun initSubProjects(files: Collection<File>): Collection<Project> {
        val result: ArrayList<Project> = ArrayList()
        for (file: File in files) {
            val subProject: Project = getFactory().getProject(file)
            subProject.root = root
            result.add(subProject)
        }

        return result
    }

    public fun getName(): String {
        return projectDirectory.name
    }

    public abstract fun accept(visitor: ProjectVisitor)
}