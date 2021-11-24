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

package org.codeontology.build.maven

import org.apache.commons.lang3.StringUtils
import org.apache.maven.project.MavenProject as ApacheMaven
import org.codeontology.build.DependenciesLoader
import org.codeontology.build.Project
import org.codeontology.extraction.project.ProjectVisitor

import java.io.File
import java.io.FileNotFoundException
import java.util.Scanner

class MavenProject(project: File) : Project(project) {
    private lateinit var mavenProject: ApacheMaven
    private var buildFile: File? = null
    private lateinit var loader: MavenLoader
    private var setUp: Boolean = false
    public override var projectDirectory: File = super.projectDirectory
        get() {
            if (!setUp) {
                return field
            }
            return mavenProject.basedir
        }

    init {
        subProjects = findSubProjects()
        setUp()
    }

    private fun setUp() {
        if (!setUp) {
            mavenProject = ApacheMaven()
            buildFile = File(getPath() + "/pom.xml")
            mavenProject.file = buildFile
            loader = MavenLoader(this)
            setUp = true
        }
    }

    override fun findSubProjects(): Collection<Project> {
        setUp()
        try {
            val modules: HashSet<File> = HashSet()
            val pom = File("${mavenProject.basedir}/pom.xml")
            val scanner = Scanner(pom)

            while (scanner.hasNextLine()) {
                val line: String = scanner.nextLine()
                val match: String? = StringUtils.substringBetween(line, "<module>", "</module>")

                if (match != null && match != "") {
                    modules.add(File("${mavenProject.basedir}/$match"))
                    println("Module: ${mavenProject.basedir}/$match")
                }
            }

            mavenProject.modules.forEach { module ->
                run {
                    println("module: ${mavenProject.basedir }/$module")
                    modules.add(File("${mavenProject.basedir}/$module"))
                }
            }

            return initSubProjects(modules)
        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        }
    }

    override fun getLoader(): DependenciesLoader<MavenProject> {
        return loader
    }

    override fun accept(visitor: ProjectVisitor) {
        visitor.visit(this)
    }

    override fun getBuildFile(): File? {
        return buildFile
    }
}