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

import org.apache.jena.rdf.model.*
import org.codeontology.Ontology

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import kotlin.system.exitProcess

class RDFLogger {
    var model: Model = Ontology.model
        private set
    private var outputFile: String = "triples.nt"
    private var counter: Int = 0

    companion object {
        @JvmStatic private var instance: RDFLogger? = null
        @JvmStatic private val maxSize: Int = 10000
        @JvmStatic
        fun getInstance(): RDFLogger {
            if(instance == null) {
                instance = RDFLogger()
            }

            return instance as RDFLogger
        }
    }

    fun setOutputFile(path: String) {
        outputFile = path
    }

    fun writeRDF() {
        val writer = PrintWriter(BufferedWriter(FileWriter(outputFile, true)))

        try {
            model.write(writer, "N-TRIPLE")
        } catch (e: IOException) {
            println("Cannot write triples.")
            exitProcess(-1)
        } finally {
            writer.close()
        }
    }

    fun addTriple(subject: Entity<*>, property: Property, obj: Entity<*>) {
        addTriple(subject, property, obj.getResource())
    }

    fun addTriple(subject: Entity<*>, property: Property?, obj: RDFNode?) {
        if (property != null && obj != null) {
            val triple: Statement = model.createStatement(subject.getResource(), property, obj)
            model.add(triple)
            counter++
            if (counter > maxSize) {
                writeRDF()
                free()
            }
        }
    }

    private fun free() {
        model = ModelFactory.createDefaultModel()
        counter = 0
    }
}