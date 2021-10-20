package org.codeontology.build.gradle

import org.codeontology.CodeOntology

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Scanner
import java.util.regex.Matcher
import java.util.regex.Pattern

public class AndroidLoader(project: AndroidProject): GradleLoader(project) {
    public override fun loadDependencies() {
        println("Loading dependencies for Android project...")
        addClasspathTask()
        build()
        runTask("CodeOntologyCpFile")
        CodeOntology.signalDependenciesDownloaded()
        loadClasspath()
        loadAndroidSdkDependencies()
    }

    private fun build() {
        try {
            getProcessBuilder("build").start().waitFor()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    protected fun addClasspathTask() {
        val name = "CodeOntologyCpFile"
        var variants: String? = null
        if (hasPlugin("com.android.application")) {
            variants = "applicationVariants"
        } else if (hasPlugin("com.android.library")) {
            variants = "libraryVariants"
        }
        if (variants != null) {
            val body =  "{\n" +
                    "\tbuildDir.mkdirs()\n" +
                    "\tandroid." + variants + ".all { variant -> \n" +
                    "\t\tnew File(buildDir, \"" + CLASSPATH_FILE_NAME + "\").text = variant.javaCompile.classpath.asPath\t\n" +
                    "\t}\n" +
                    "}"
            addTask(name, body)
        }
    }

    private fun loadAndroidSdkDependencies() {
        val androidHome: String? = System.getenv()["ANDROID_HOME"]
        if (androidHome == null) {
            CodeOntology.showWarning("ANDROID_HOME environment variable is not set.")
        }
        val appBuild = File(project.getPath() + "/build.gradle")

        var build = ""

        val scanner = Scanner(appBuild)

        try {
            scanner.useDelimiter("\\Z")
            if (scanner.hasNext()) {
                build = scanner.next()
            }
        } catch (e: FileNotFoundException) {
            CodeOntology.showWarning("Could not find file build.gradle for module app.")
        } finally {
            scanner.close()
        }

        var sdkVersion: String? = null

        val androidBlock = ".*android\\s*\\{.*"
        var pattern: Pattern = Pattern.compile(androidBlock + "compileSdkVersion\\s+([0-9]+).*", Pattern.DOTALL)
        var matcher: Matcher = pattern.matcher(build)
        if (matcher.matches()) {
            sdkVersion = matcher.group(1)
        } else {
            pattern = Pattern.compile(androidBlock + "targetSdkVersion\\s+([0-9]+).*", Pattern.DOTALL)
            matcher = pattern.matcher(build)
            if (matcher.matches()) {
                sdkVersion = matcher.group(1)
            }
        }
        if (sdkVersion != null) {
            loader.lock()
            loader.loadAllJars("$androidHome/platforms/android-$sdkVersion")
            loader.release()
        } else {
            CodeOntology.showWarning("Could not find sdk version.")
        }
    }
}