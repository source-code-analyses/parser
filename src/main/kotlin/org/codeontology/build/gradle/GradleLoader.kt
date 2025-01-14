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

package org.codeontology.build.gradle

import org.apache.commons.io.FileUtils
import org.codeontology.CodeOntology
import org.codeontology.build.DependenciesLoader
import org.codeontology.build.Project

import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Scanner
import java.util.regex.Pattern


open class GradleLoader(project: GradleProject): DependenciesLoader<GradleProject>(project) {
    companion object {
        @JvmStatic
        val CLASSPATH_FILE_NAME: String = "cp" + CodeOntology.SUFFIX
    }

    private val gradleLocalRepository: File = File(System.getProperty("user.home") + "/.gradle")
    private val error: File = File(project.projectDirectory.path + "/error")
    private val output: File = File(project.projectDirectory.path + "/output")
    private var localPropertiesHandled: Boolean = false

    override fun loadDependencies() {
        println("Loading dependencies with gradle...")

        handleLocalProperties()

        if (CodeOntology.downloadDependencies()) {
            downloadDependencies()
        }

        jarProjects()
        runTasks()
        loadClasspath()
        runOnSubProjects()
        removeClassPathFiles()
    }

    private fun runOnSubProjects() {
        val subProjects: Collection<Project> = project.subProjects
        for(subProject: Project in subProjects) {
            println("Running on subproject: ${subProject.projectDirectory.path}")
            val loader: DependenciesLoader<*> = subProject.getLoader()
            loader.loadDependencies()
        }
    }

    fun handleLocalProperties() {
        if (localPropertiesHandled) {
            return
        }

        localPropertiesHandled = true
        val localProperties = File("${project.getPath()}/local.properties")
        val tmp = File("${project.getPath()}/.tmp.properties")
        if (!localProperties.exists()) {
            return
        }

        val scanner = Scanner(localProperties)

        try {
            val writer = BufferedWriter(FileWriter(tmp))

            try {
                while (scanner.hasNextLine()) {
                    val line: String = scanner.nextLine()
                    if (!line.trim().startsWith("sdk.dir")) {
                        writer.write(line + "\n")
                    }
                }

                var success: Boolean = localProperties.renameTo(File(localProperties.path + CodeOntology.SUFFIX))
                success = success && tmp.renameTo(localProperties)
                if (!success) {
                    FileUtils.forceDelete(localProperties)
                    FileUtils.forceDelete(tmp)
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            } finally {
                writer.close()
            }
        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        } finally {
            scanner.close()
        }
    }

    protected fun loadClasspath() {
        val classpathFile = File(project.getPath() + "/build/" + CLASSPATH_FILE_NAME)
        var classpath = ""

        val scanner = Scanner(classpathFile)

        try {
            scanner.useDelimiter("\\Z")
            if (scanner.hasNext()) {
                classpath = scanner.next().trim()
            }
            if (classpath == "") {
                loadAllAvailableJars()
            } else {
                loader.loadClasspath(classpath)
            }
        } catch (e: FileNotFoundException) {
            loadAllAvailableJars()
        } finally {
            scanner.close()
        }

        val src = File(project.getPath() + "/src/")
        if (src.exists()) {
            loader.loadAllJars(src)
        }
    }

    private fun removeClassPathFiles() {
        val pathString: String = project.getPath()
        try {
            Files.walk(Paths.get(pathString))
                    .filter{ path -> path.toFile().name.equals(CLASSPATH_FILE_NAME) }
                    .forEach{ path -> path.toFile().delete() }
        } catch (e: IOException) {
            CodeOntology.showWarning("Could not remove classpath files")
        }
    }

    private fun loadAllAvailableJars() {
        loader.loadAllJars(project.root)
        loader.lock()
        loader.loadAllJars(gradleLocalRepository)
        loader.release()
    }

    private fun downloadDependencies() {
        try {
            println("Downloading dependencies...")
            getProcessBuilder("dependencies").start().waitFor()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    private fun runTasks() {
        applyPlugin("java")

        val testJarTask = "CodeOntologyTestJar"
        val testJarTaskBody = "(type: Jar) {" + '\n' +
                '\t' + "classifier = 'tests'" + '\n' +
                '\t' + "from sourceSets.test.output" + '\n' +
                "}"

        addTask(testJarTask, testJarTaskBody)
        runTask(testJarTask)

        val separator = System.getProperty("line.separator")
        val cpFileTask = "CodeOntologyCpFile"
        val cpFileTaskBody = "{" + separator +
                '\t' + "buildDir.mkdirs()" + separator +
                '\t' + "new File(buildDir, \"" + CLASSPATH_FILE_NAME + "\").text = sourceSets.main.runtimeClasspath.asPath" + separator +
                "}"

        addTask(cpFileTask, cpFileTaskBody)
        runTask(cpFileTask)
    }

    private fun applyPlugin(plugin: String) {
        val writer = PrintWriter(BufferedWriter(FileWriter(project.getBuildFile()!!, true)))

        try {
            val build: String = project.getBuildFileContent()
            val applyPlugin = "apply plugin: \'$plugin\'"
            if (build != "" && !hasPlugin(plugin)) {
                writer.write("\n \n$applyPlugin")
            }
        } catch (e: IOException) {
            CodeOntology.showWarning("Could not apply plugin $plugin")
        } finally {
            writer.close()
        }
    }

    fun hasPlugin(plugin: String): Boolean {
        val buildFile: String = project.getBuildFileContent()
        val regex = ".*apply\\s+plugin\\s*:\\s+\'$plugin\'.*"
        val pattern: Pattern = Pattern.compile(regex, Pattern.DOTALL)
        return pattern.matcher(buildFile).matches()
    }

    protected fun addTask(name: String, body: String) {
        val writer = PrintWriter(BufferedWriter(FileWriter(project.getBuildFile()!!, true)))

        try {
            val build = project.getBuildFileContent()
            val pattern: Pattern = Pattern.compile(".*task\\s+$name.*", Pattern.DOTALL)
            if (!pattern.matcher(build).matches()) {
                writer.write("\n\ntask $name $body")
            }
        } catch (e: IOException) {
            CodeOntology.showWarning("Could not add task $name")
        } finally {
            writer.close()
        }
    }

    fun runTask(name: String) {
        try {
            getProcessBuilder(name).start().waitFor()
        } catch (e: IOException) {
            println("Could not run task $name")
        } catch (e: InterruptedException) {
            println("Could not run task $name")
        }
    }

    fun getProcessBuilder(command: String): ProcessBuilder {
        val builder: ProcessBuilder
        val gradlew = File(project.root.path + "/gradlew")
        if (gradlew.exists()) {
            if (!gradlew.setExecutable(true)) {
                CodeOntology.showWarning("Could not execute gradlew")
            }

            builder = ProcessBuilder("bash", "-c", "./gradlew $command")
            builder.directory(project.root)
        } else {
            builder = ProcessBuilder("gradle $command")
            builder.directory(project.projectDirectory)
        }

        builder.redirectError(error)
        builder.redirectOutput(output)

        return builder
    }

    private fun jarProjects() {
        try {
            val build = File(project.root.path + "/build.gradle")

            val scanner = Scanner(build)
            var plugin = false

            while(scanner.hasNextLine()) {
                val line: String = scanner.nextLine()

                if (line.contains("apply plugin: \'java\'")) {
                    plugin = true
                    break
                }
            }

            if (!plugin) {
                val writer = FileWriter(build, true)
                writer.append("\n\napply plugin: \'java\'")
                writer.close()
            }

            getProcessBuilder("jar").start().waitFor()

        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }
}
