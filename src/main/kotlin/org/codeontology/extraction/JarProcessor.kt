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

package org.codeontology.extraction

import org.codeontology.CodeOntology
import org.codeontology.build.ClasspathLoader

import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.util.jar.JarFile

class JarProcessor {
    private lateinit var jarFile: JarFile
    private lateinit var systemErr: PrintStream

    constructor(path: String) {
        try {
            if(File(path).exists()) {
                this.jarFile = JarFile(path)
                ClasspathLoader.getInstance().load(path)
                systemErr = System.err
            }
        } catch (e: Exception) {
            CodeOntology.showWarning("Could not access file $path")
        } catch (e: Error) {
            CodeOntology.showWarning("Could not access file $path")
        }
    }

    constructor(jar: File): this(jar.path)

    fun process() {
        try {
            try {
                hideMessages()
                EntityFactory.getInstance().wrap(jarFile).extract()
            } finally {
                System.setErr(systemErr)
            }
        } catch (e: Exception) {
            CodeOntology.codeOntology!!.handleFailure(e)
        } catch (e: Error) {
            CodeOntology.codeOntology!!.handleFailure(e)
        }
    }

    private fun hideMessages() {
        val tmpErr = PrintStream(object: OutputStream() { override fun write(p0: Int) {} })
        System.setErr(tmpErr)
    }
}