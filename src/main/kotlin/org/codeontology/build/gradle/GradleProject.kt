package org.codeontology.build.gradle

import org.codeontology.CodeOntology
import org.codeontology.build.BuildFiles
import org.codeontology.build.DependenciesLoader
import org.codeontology.build.Project
import org.codeontology.extraction.project.ProjectVisitor

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Scanner

open class GradleProject(projectDirectory: File) : Project(projectDirectory) {
    companion object {
        @JvmStatic private val SUBPROJECTS_FILE_NAME: String = "subProjects${CodeOntology.SUFFIX}"
    }

    private var buildFile: File? = null

    private lateinit var loader: GradleLoader
    private var setUp: Boolean = false

    init {
        subProjects = findSubProjects()
        setUp()
    }

    final override fun findSubProjects(): Collection<Project> {
        setUp()
        val subProjects: HashSet<File> = HashSet()
        val task: String = "subprojects {\n" +
                "\ttask CodeOntologySub << {\n" +
                "\t\ttask -> new File(rootDir, \"" + SUBPROJECTS_FILE_NAME + "\").append(\"\$task.project.projectDir\\n\");\n" +
                "\t}\n" +
                "}"
        val buildFile: File? = getBuildFile()
        val content: String = getBuildFileContent()
        val writer = BufferedWriter(FileWriter(buildFile!!, true))
        try {
            if (!content.contains(task)) {
                writer.write("\n\n" + task)
                writer.close()
            }

            loader.runTask("CodeOntologySub")

            val subProjectsFile = File("${root}/$SUBPROJECTS_FILE_NAME")

            val scanner = Scanner(subProjectsFile)

            scanner.use {
                while (it.hasNextLine()) {
                    subProjects.add(File(it.nextLine()))
                }
            }

            if (subProjects.isNotEmpty()) {
                println("Subprojects of ${projectDirectory.name}:")
                for(file: File in subProjects) {
                    println(file.name)
                }
            }
        } catch (e: IOException) {
            println("No subproject found for project ${projectDirectory.name}.")
        } finally {
            writer.close()
        }

        return initSubProjects(subProjects)
    }

    override fun initSubProjects(files: Collection<File>): Collection<Project> {
        val result: Collection<Project> = super.initSubProjects(files)
        removeSubProjectsFile()
        return result
    }

    private fun removeSubProjectsFile() {
        val subProjectsFile = File("$root/$SUBPROJECTS_FILE_NAME")
        if (!subProjectsFile.exists()) {
            return
        }

        val success: Boolean = subProjectsFile.delete()
        if (!success) {
            CodeOntology.showWarning("Could not delete subProjects file")
        }
    }

    private fun setUp() {
        if (!setUp) {
            loader = GradleLoader(this)
            buildFile = File("${getPath()}/${BuildFiles.GRADLE_FILE}")
            loader.handleLocalProperties()
            backup()
            setUp = true
        }
    }

    private fun backup() {
        val content: String = getBuildFileContent()
        val buildFile: File = getBuildFile()!!
        val backup = File(buildFile.path + CodeOntology.SUFFIX)

        val writer = BufferedWriter(FileWriter(backup))

        try {
            writer.write(content)
        } catch (e: IOException) {
            CodeOntology.showWarning("Could not backup build file")
        } finally {
            writer.close()
        }
    }

    override fun getLoader(): DependenciesLoader<GradleProject> {
        return loader
    }

    override fun getBuildFile(): File? {
        return buildFile
    }

    override fun accept(visitor: ProjectVisitor) {
        visitor.visit(this)
    }
}