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

import com.martiansoftware.jsap.*
import java.io.File
import java.util.*
import kotlin.system.exitProcess


class CodeOntologyArguments(args: Array<String>) {
    companion object {
        @JvmStatic public val INPUT_LONG = "input"
        @JvmStatic public val INPUT_SHORT = 'i'

        @JvmStatic public val OUTPUT_LONG = "output"
        @JvmStatic public val OUTPUT_SHORT = 'o'

        @JvmStatic public val CLASSPATH_LONG = "classpath"

        @JvmStatic public val DO_NOT_DOWNLOAD_LONG = "do-not-download"

        @JvmStatic public val VERBOSE_LONG = "verbose"
        @JvmStatic public val VERBOSE_SHORT = 'v'

        @JvmStatic public val STACKTRACE_LONG = "stacktrace"
        @JvmStatic public val STACKTRACE_SHORT = 't'

        @JvmStatic public val HELP_LONG = "help"
        @JvmStatic public val HELP_SHORT = 'h'

        @JvmStatic public val SHUTDOWN_LONG = "shutdown"

        @JvmStatic public val JAR_INPUT_LONG = "jar"

        @JvmStatic public val DEPENDENCIES_LONG = "dependencies"
        @JvmStatic public val DEPENDENCIES_SHORT = 'd'

        @JvmStatic public val DO_NOT_EXTRACT_LONG = "do-not-extract"

        @JvmStatic public val FORCE_LONG = "force"
        @JvmStatic public val FORCE_SHORT = 'f'

        @JvmStatic public val PROJECT_STRUCTURE_LONG = "project"
        @JvmStatic public val PROJECT_STRUCTURE_SHORT = 'p'

        @JvmStatic public val STATEMENTS_LONG = "statements"
        @JvmStatic public val STATEMENTS_SHORT = 's'

        @JvmStatic public val EXPRESSIONS_LONG = "expressions"
        @JvmStatic public val EXPRESSIONS_SHORT = 'e'
    }

    private lateinit var jsap: JSAP
    private var result: JSAPResult? = null

    init {
        defineArgs()
        result = parseArgs(args)
    }

    @Throws(JSAPException::class)
    private fun defineArgs() {
        jsap = JSAP()
        var option = FlaggedOption(INPUT_LONG)
        option.shortFlag = INPUT_SHORT
        option.longFlag = INPUT_LONG
        option.stringParser = JSAP.STRING_PARSER
        option.help = "Path to source files."
        jsap.registerParameter(option)
        option = FlaggedOption(OUTPUT_LONG)
        option.shortFlag = OUTPUT_SHORT
        option.longFlag = OUTPUT_LONG
        option.stringParser = JSAP.STRING_PARSER
        option.setRequired(false)
        option.setDefault(getDefaultOutput())
        option.help = "Output file name."
        jsap.registerParameter(option)
        option = FlaggedOption(JAR_INPUT_LONG)
        option.longFlag = JAR_INPUT_LONG
        option.stringParser = JSAP.STRING_PARSER
        option.setRequired(false)
        option.help = "Path to a jar input file"
        jsap.registerParameter(option)
        option = FlaggedOption(CLASSPATH_LONG)
        option.longFlag = CLASSPATH_LONG
        option.stringParser = JSAP.STRING_PARSER
        option.setRequired(false)
        option.help = "Specifies a list of directories and JAR files separated by colons (:) to search for class files."
        jsap.registerParameter(option)
        var flag = Switch(DO_NOT_DOWNLOAD_LONG)
        flag.longFlag = DO_NOT_DOWNLOAD_LONG
        flag.setDefault("false")
        flag.help = "Do not download dependencies."
        jsap.registerParameter(flag)
        flag = Switch(VERBOSE_LONG)
        flag.longFlag = VERBOSE_LONG
        flag.shortFlag = VERBOSE_SHORT
        flag.setDefault("false")
        flag.help = "Verbosely lists all files processed."
        jsap.registerParameter(flag)
        flag = Switch(STACKTRACE_LONG)
        flag.longFlag = STACKTRACE_LONG
        flag.shortFlag = STACKTRACE_SHORT
        flag.setDefault("false")
        flag.help = "Print stack trace for exceptions."
        jsap.registerParameter(flag)
        flag = Switch(HELP_LONG)
        flag.longFlag = HELP_LONG
        flag.shortFlag = HELP_SHORT
        flag.setDefault("false")
        flag.help = "Print this help message."
        jsap.registerParameter(flag)
        flag = Switch(DEPENDENCIES_LONG)
        flag.longFlag = DEPENDENCIES_LONG
        flag.shortFlag = DEPENDENCIES_SHORT
        flag.setDefault("false")
        flag.help = "Explore jar files in classpath"
        jsap.registerParameter(flag)
        flag = Switch(SHUTDOWN_LONG)
        flag.longFlag = SHUTDOWN_LONG
        flag.setDefault("false")
        flag.help = "Shutdown after completing extraction"
        jsap.registerParameter(flag)
        flag = Switch(DO_NOT_EXTRACT_LONG)
        flag.longFlag = DO_NOT_EXTRACT_LONG
        flag.setDefault("false")
        flag.help = "Do not extract triples, just download dependencies"
        jsap.registerParameter(flag)
        flag = Switch(FORCE_LONG)
        flag.longFlag = FORCE_LONG
        flag.shortFlag = FORCE_SHORT
        flag.setDefault("false")
        flag.help = "Ignore files that prevent the model from being built."
        jsap.registerParameter(flag)
        flag = Switch(PROJECT_STRUCTURE_LONG)
        flag.longFlag = PROJECT_STRUCTURE_LONG
        flag.shortFlag = PROJECT_STRUCTURE_SHORT
        flag.setDefault("false")
        flag.help = "Extract project structure"
        jsap.registerParameter(flag)
        flag = Switch(STATEMENTS_LONG)
        flag.longFlag = STATEMENTS_LONG
        flag.shortFlag = STATEMENTS_SHORT
        flag.setDefault("false")
        flag.help = "Process all statements"
        jsap.registerParameter(flag)
        flag = Switch(EXPRESSIONS_LONG)
        flag.longFlag = EXPRESSIONS_LONG
        flag.shortFlag = EXPRESSIONS_SHORT
        flag.setDefault("false")
        flag.help = "Process all expressions"
        jsap.registerParameter(flag)
    }

    @Throws(JSAPException::class)
    fun parseArgs(args: Array<String>): JSAPResult? {
        defineArgs()

        val arguments = jsap.parse(args)
        if (arguments.getBoolean(HELP_LONG)) {
            printHelp()
            exitProcess(0)
        }
        if (!arguments.success()) {
            // print out specific error messages describing the problems
            val errs = arguments.errorMessageIterator
            while (errs.hasNext()) {
                System.err.println("Error: " + errs.next())
            }
            printHelp()
            exitProcess(-1)
        }
        return arguments
    }

    private fun printHelp() {
        printUsage()
        System.err.println("Options:")
        System.err.println()
        System.err.println(jsap.help)
    }

    private fun printUsage() {
        System.err.println("Usage:")
        System.err.println("codeontology -i <input_folder> -o <output_file>")
        System.err.println()
    }

    fun getInput(): String? {
        return result?.getString(INPUT_LONG)
    }

    fun getOutput(): String? {
        return result?.getString(OUTPUT_LONG)
    }

    fun downloadDependencies(): Boolean {
        return !result?.getBoolean(DO_NOT_DOWNLOAD_LONG)!!
    }

    fun verboseMode(): Boolean {
        return result?.getBoolean(VERBOSE_LONG) ?: false
    }

    fun stackTraceMode(): Boolean {
        return result?.getBoolean(STACKTRACE_LONG) ?: false
    }

    fun shutdownFlag(): Boolean {
        return result?.getBoolean(SHUTDOWN_LONG) ?: false
    }

    fun doNotExtractTriples(): Boolean {
        return result?.getBoolean(DO_NOT_EXTRACT_LONG) ?: false
    }

    private fun getDefaultOutput(): String? {
        val max = 100
        var i = 0
        var defaultName: String?
        var file: File
        do {
            val formatter = Formatter()
            formatter.format("triples%02d.nt", i)
            defaultName = formatter.toString()
            file = File(defaultName)
            i++
        } while (i < max && file.exists())
        if (i > max) {
            throw RuntimeException("Specify an output file")
        }
        return defaultName
    }

    fun getJarInput(): String? {
        return result?.getString(JAR_INPUT_LONG)
    }

    fun exploreJars(): Boolean {
        return result?.getBoolean(DEPENDENCIES_LONG) ?: false
    }

    fun getClasspath(): String? {
        return result?.getString(CLASSPATH_LONG)
    }

    fun removeTests(): Boolean {
        return result?.getBoolean(FORCE_LONG) ?: false
    }

    fun extractProjectStructure(): Boolean {
        return result?.getBoolean(PROJECT_STRUCTURE_LONG) ?: false
    }

    fun processStatements(): Boolean {
        return result?.getBoolean(STATEMENTS_LONG) ?: false
    }

    fun processExpressions(): Boolean {
        return result?.getBoolean(EXPRESSIONS_LONG) ?: false
    }
}
