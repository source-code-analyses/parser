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

import org.codeontology.CodeOntology
import org.codeontology.build.ClasspathLoader
import org.codeontology.build.DependenciesLoader
import org.codeontology.build.Project

import java.io.File
import java.io.IOException
import java.util.Scanner

class MavenLoader(project: MavenProject): DependenciesLoader<MavenProject>(project) {
    companion object {
        @JvmStatic private val PATH_TO_DEPENDENCIES: String = "/target/dependency/"
        @JvmStatic private var m2Loaded: Boolean = false
    }

    private val output: File = File(project.getPath() + "/output")
    private val error: File = File(project.getPath() + "/error")

    override fun loadDependencies() {
        println("Loading dependencies with Maven")
        try {
            if (CodeOntology.downloadDependencies()) {
                downloadDependencies()
                jarModules()
            }

            val builder = ProcessBuilder("mvn", "dependency:build-classpath", "-Dmdep.outputFile=.cp")
            builder.directory(project.projectDirectory)
            builder.redirectError(error)
            builder.redirectOutput(output)
            val exitStatus: Int = builder.start().waitFor()

            if (exitStatus == 0) {
                val classpath = File("${project.getPath()}/.cp")
                val reader = Scanner(classpath)
                reader.useDelimiter("\\Z")
                if (reader.hasNext()) {
                    loader.loadClasspath(reader.next())
                }
                reader.close()
                classpath.deleteOnExit()
            } else {
                loader.loadAllJars(project.projectDirectory)
                if (!m2Loaded) {
                    val loader: ClasspathLoader = loader
                    loader.lock()
                    loader.loadAllJars("${System.getProperty("user.home")}/.m2")
                    loader.release()
                    m2Loaded = true
                }
            }

            val modules: Collection<Project> = project.subProjects
            for(module: Project in modules) {
                println("Running on module " + module.getPath())
                module.getLoader().loadDependencies()
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch(e: InterruptedException) {
            throw java.lang.RuntimeException(e)
        }
    }

    /**
     * Get dependencies for maven project in
     * folder {@code projectRoot}, and save them in
     * projectRoot/target/dependency/.
     */
    private fun downloadDependencies() {
        try {
            val downloadDirectory = File(project.getPath() + PATH_TO_DEPENDENCIES)

            if (!downloadDirectory.exists()) {
                if (!downloadDirectory.mkdirs()) {
                    throw IOException("Could not create download directory for dependencies")
                }
            }

            println("Downloading dependencies...")
            val builder = ProcessBuilder("mvn", "dependency:copy-dependencies")
            builder.directory(project.projectDirectory)
            builder.redirectError(error)
            builder.redirectOutput(output)

            builder.start().waitFor()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    /**
     * Create jars for every dependency in {@code projectRoot}.
     * Output folder is the project root, name goes as:
     * {@code dependencyName.jar}.
     */
    private fun jarModules() {
        project.subProjects.forEach{module ->
            run {
                if (!module.projectDirectory.toPath().equals(project.projectDirectory.toPath())) {
                    println("Preparing module " + module.getPath())
                    try {
                        val builder = ProcessBuilder("mvn", "jar:jar")
                        builder.directory(module.projectDirectory)
                        builder.redirectError(error)
                        builder.redirectOutput(output)

                        builder.start().waitFor()
                    } catch (e: Exception) {
                        throw RuntimeException (e)
                    }
                }
            }
        }
    }
}