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

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.FileFilterUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader

class ClasspathLoader {
    companion object {
        @JvmStatic private var instance: ClasspathLoader? = null
        @JvmStatic
        fun getInstance(): ClasspathLoader {
            if(instance == null) {
                instance = ClasspathLoader()
            }

            return instance as ClasspathLoader
        }
    }

    private val classpath: HashSet<File> = HashSet()
    private var locked: Boolean = false

    fun load(path: String) {
        load(File(path))
    }

    fun load(file: File) {
        if (file.isDirectory) {
            loadAllJars(file)
            return
        }

        if (file.path.endsWith(".jar") && !locked) {
            classpath.add(file)
        }

        try {
            load(file.toURI().toURL())
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        }
    }

    private fun load(url: URL) {
        try {
            URLClassLoader(arrayOf(url), ClassLoader.getSystemClassLoader())
        } catch (t: Throwable) {
           System.err.println("Error loading ${url.path}")
        }
    }

    fun loadAllJars(root: File) {
        if (root.isDirectory) {
            val jars: HashSet<File> = HashSet()

            jars.addAll(FileUtils.listFiles(root,
                    FileFilterUtils.suffixFileFilter(".jar"),
                    TrueFileFilter.INSTANCE))

            jars.forEach(this::load)
        }
    }

    fun loadAllJars(path: String) {
        loadAllJars(File(path))
    }

    fun loadClasspath(classpath: String) {
        val paths: List<String> = classpath.split(":")
        for(path: String in paths) {
            load(path)
        }
    }

    fun getJarsLoaded(): Set<File> {
        return classpath
    }

    fun lock() {
        locked = true
    }

    fun release() {
        locked = false
    }
}
