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

package org.codeontology

import com.martiansoftware.jsap.JSAPException
import org.codeontology.build.DependenciesLoader
import org.codeontology.build.Project
import org.codeontology.build.ProjectFactory
import org.codeontology.extraction.JarProcessor
import org.codeontology.extraction.RDFLogger
import org.codeontology.extraction.ReflectionFactory
import org.codeontology.extraction.SourceProcessor
import org.codeontology.extraction.project.ProjectEntity
import org.codeontology.extraction.project.ProjectVisitor
import org.joda.time.Period
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import spoon.Launcher
import spoon.compiler.ModelBuildingException
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.regex.Pattern
import kotlin.system.exitProcess

class CodeOntology(args: Array<String>) {
    companion object {
        @JvmStatic public var codeOntology: CodeOntology? = null
            private set
        @JvmStatic private var status: Int = 0
        @JvmStatic public val SUFFIX: String = ".codeontology"

        @JvmStatic public fun main(args: Array<String>) {
            codeOntology = CodeOntology(args)

            try {
                codeOntology!!.processSources()
                codeOntology!!.processProjectStructure()
                codeOntology!!.processJars()
            } catch (e: Exception) {
                codeOntology!!.handleFailure(e)
            } catch (e: Error) {
                e.printStackTrace()
                codeOntology!!.handleFailure(e)
            }

            codeOntology!!.postCompletionTasks()
            exitProcess(status)
        }

        @JvmStatic public fun downloadDependencies(): Boolean {
            if(codeOntology == null) {
                return true
            }
            return codeOntology!!.downloadDependencies
        }

        @JvmStatic public fun signalDependenciesDownloaded() {
            codeOntology!!.downloadDependencies = true
        }

        @JvmStatic public fun verboseMode(): Boolean {
            return codeOntology!!.arguments.verboseMode()
        }

        @JvmStatic public fun isJarExplorationEnabled(): Boolean {
            return codeOntology!!.exploreJarsFlag
        }

        @JvmStatic private fun isInputSet(): Boolean {
            return codeOntology!!.arguments.getInput() != null
        }

        @JvmStatic
        fun showWarning(message: String?) {
            println("[WARNING] $message")
        }

        @JvmStatic private fun exit(status: Int) {
            try {
                val timer = Timer()
                timer.schedule(object: TimerTask(){
                    override fun run() {
                        Runtime.getRuntime().halt(status)
                    }

                }, 30000)

                exitProcess(status)
            } catch (t: Throwable) {
                try {
                    Thread.sleep(30000)
                    Runtime.getRuntime().halt(status)
                } catch (e: Exception) {
                    Runtime.getRuntime().halt(status)
                } catch (e: Error) {
                    Runtime.getRuntime().halt(status)
                }
            }
        }

        @JvmStatic public fun getProject(): ProjectEntity<*> {
            return codeOntology!!.getProjectEntity()
        }

        @JvmStatic public fun extractProjectStructure(): Boolean {
            return codeOntology!!.arguments.extractProjectStructure()
        }

        @JvmStatic public fun processStatements(): Boolean {
            return codeOntology!!.arguments.processStatements()
        }

        @JvmStatic public fun processExpressions(): Boolean {
            return codeOntology!!.arguments.processExpressions()
        }

        @JvmStatic public fun processGenerics(): Boolean {
            return true
        }
    }

    private var downloadDependencies: Boolean = false
    public lateinit var arguments: CodeOntologyArguments
        private set
    private lateinit var spoon: Launcher
    private var exploreJarsFlag: Boolean = false
    private var project: Project? = null
    private var projectEntity: ProjectEntity<*>? = null
    private var loader: DependenciesLoader<Project>? = null
    private lateinit var formatter: PeriodFormatter
    private var tries: Int = 0
    private val directories: Array<String> = arrayOf("test", "examples", "debug", "androidTest", "samples", "sample",
    "example", "demo", ".*test.*", ".*demo.*", ".*sample.*", ".*example.*", "app")

    init {
        try {
            spoon = Launcher()
            arguments = CodeOntologyArguments(args)
            exploreJarsFlag = arguments.exploreJars() || (arguments.getJarInput() != null)
            ReflectionFactory.getInstance().parent = spoon.createFactory()
            RDFLogger.getInstance().setOutputFile(arguments.getOutput()!!)
            downloadDependencies = arguments.downloadDependencies()
            formatter = PeriodFormatterBuilder()
                .appendHours()
                .appendSuffix(" h ")
                .appendMinutes()
                .appendSuffix(" min ")
                .appendSeconds()
                .appendSuffix(" s ")
                .toFormatter()

            setUncaughtExceptionHandler()
        } catch(e: JSAPException) {
            println("Could not process arguments")
        }
    }

    private fun processSources() {
        try {
            if(isInputSet()) {
                println("Running on ${arguments.getInput()}")

                project = ProjectFactory.getInstance().getProject(arguments.getInput()!!)

                loadDependencies()

                if(!arguments.doNotExtractTriples()) {
                    spoon()
                    extractAllTriples()
                }
            }
        } catch(e: Exception) {
            handleFailure(e)
        }
    }

    private fun processProjectStructure() {
        if(arguments.extractProjectStructure() && project != null) {
            getProjectEntity().extract()
            RDFLogger.getInstance().writeRDF()
        }
    }

    fun handleFailure(t: Throwable?) {
        println("It was a good plan that went awry.")
        if(t != null) {
            if(t.message != null) {
                println(t.message)
            }
            if(arguments.stackTraceMode()) {
                t.printStackTrace()
            }
        }

        status = -1
    }

    private fun spoon() {
        checkInput()

        try {
            val start: Long = System.currentTimeMillis()
            spoon.addInputResource(arguments.getInput())
            println("Building model...")
            spoon.buildModel()
            val end: Long = System.currentTimeMillis()
            val period = Period(start, end)
            println("Model built successfully in ${formatter.print(period)}")
        } catch (e: ModelBuildingException) {
            if(arguments.removeTests() && tries < directories.size) {
                var result: Boolean

                do {
                    result = removeDirectoriesByName(directories[tries])
                    tries++
                }while (!result && tries < directories.size)

                if(result) {
                    spoon = Launcher()
                    spoon()
                    return
                }
            }
            throw e
        }
    }

    private fun loadDependencies() {
        val start: Long = System.currentTimeMillis()
        loader = project!!.getLoader()
        loader!!.loadDependencies()

        val classpath: String? = arguments.getClasspath()

        if(classpath != null) {
            loader!!.loadClasspath(classpath)
        }
        val end: Long = System.currentTimeMillis()

        println("Dependencies loaded in ${formatter.print(Period(start, end))}.")
    }

    private fun extractAllTriples() {
        val start: Long = System.currentTimeMillis()

        println("Extracting triples...")
        spoon.addProcessor(SourceProcessor())
        spoon.process()
        RDFLogger.getInstance().writeRDF()

        val end: Long = System.currentTimeMillis()

        val period = Period(start, end)

        println("Triples extracted successfully in ${formatter.print(period)}.")
        spoon = Launcher()
    }

    private fun processJars() {
        val start: Long = System.currentTimeMillis()
        val path: String? = arguments.getJarInput()

        if(path != null) {
            val processor = JarProcessor(path)
            processor.process()
        }

        if(arguments.exploreJars() && loader != null) {
            val jars: Set<File> = loader!!.getJarsLoaded()

            for(jar: File in jars) {
                JarProcessor(jar).process()
            }

            val end: Long = System.currentTimeMillis()
            val period = Period(start, end)
            println("Jar files processed successfully in ${formatter.print(period)}.")
        }
    }

    private fun postCompletionTasks() {
        try {
            scheduleShutdownTask()
            restore()
        } catch (e: IOException) {
            handleFailure(e)
        }
    }

    private fun restore() {
        val root: String? = arguments.getInput()
        Files.walk(Paths.get(root!!))
            .map(Path::toFile)
            .filter{ file -> file.absolutePath.endsWith(SUFFIX) }
            .forEach(this::restore)
    }

    private fun restore(file: File) {
        val original: File = removeSuffix(file)
        var success = true
        if(original.exists()) {
            success = original.delete()
        }
        success = success && file.renameTo(original)

        if(!success) {
            showWarning("Could not restore file ${file.path}")
        }
    }

    private fun removeSuffix(suffixed: File): File {
        val suffixLength: Int = SUFFIX.length
        val path: String = suffixed.path
        val builder = StringBuilder(path)
        val index: Int = builder.lastIndexOf(SUFFIX)
        builder.replace(index, index + suffixLength, "")
        return File(builder.toString())
    }

    private fun scheduleShutdownTask() {
        if(codeOntology!!.arguments.shutdownFlag()) {
            val shutdownThread = Thread {
                try {
                    println("Shutting down...")
                    val processBuilder = ProcessBuilder("bash", "-c", "sleep3; shutdown -h now")
                    processBuilder.start()
                } catch (e: Exception) {
                    println("Shutdown failed")
                }
            }

            Runtime.getRuntime().addShutdownHook(shutdownThread)
        }
    }

    private fun checkInput() {
        val input = File(arguments.getInput()!!)
        if(!input.exists()) {
            println("File ${input.path} doesn't seem to exist.")
            exitProcess(-1)
        }
        if(!input.canRead() && !input.setReadable(true)) {
            println("File ${input.path} doesn't seem to be readable.")
            exitProcess(-1)
        }
    }

    private fun removeDirectoriesByName(name: String): Boolean {
        try {
            val tests: Array<Path> = Files.walk(Paths.get(arguments.getInput()!!))
                .filter{ path -> match(path, name) && path.toFile().isDirectory }.toList().toTypedArray()

            if(tests.isEmpty()) {
                return false
            }

            for(testPath: Path in tests) {
                println("Ignoring sources in ${testPath.toFile().absolutePath}")
                Files.walk(testPath)
                    .filter{ path -> path.toFile().absolutePath.endsWith(".java") }
                    .forEach{ path -> path.toFile().renameTo(File(path.toFile().path + SUFFIX)) }
            }
        } catch (e: IOException) {
            showWarning(e.message)
        }

        return true
    }

    private fun match(path: Path, name: String): Boolean {
        return if(!name.contains("*")) {
            path.toFile().name.equals(name)
        } else {
            val pattern: Pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            pattern.matcher(path.toFile().name).matches()
        }
    }

    private fun setUncaughtExceptionHandler() {
        Thread.currentThread().setUncaughtExceptionHandler{ _, _ -> exit(-1)}
    }

    public fun getProjectEntity(): ProjectEntity<*> {
        if(projectEntity == null) {
            val visitor = ProjectVisitor()
            project!!.accept(visitor)
            projectEntity = visitor.getLastEntity()
        }

        return projectEntity!!
    }
}
