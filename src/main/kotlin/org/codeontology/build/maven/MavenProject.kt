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