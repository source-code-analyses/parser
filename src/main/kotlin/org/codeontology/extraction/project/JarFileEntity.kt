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

package org.codeontology.extraction.project

import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.RDFNode
import org.codeontology.CodeOntology
import org.codeontology.Ontology
import org.codeontology.extraction.AbstractEntity
import org.codeontology.extraction.EntityFactory
import org.codeontology.extraction.ReflectionFactory
import org.codeontology.extraction.declaration.PackageEntity
import spoon.reflect.reference.CtPackageReference
import java.io.File
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile

class JarFileEntity(element: JarFile) : AbstractEntity<JarFile>(element) {
    private lateinit var packs: ArrayList<PackageEntity>
    private lateinit var map: HashMap<Package, List<Class<*>>>

    init {
        setPackages()
    }

    override fun buildRelativeURI(): String {
        return "${getName()}$SEPARATOR${packs.hashCode()}"
    }

    private fun getName(): String {
        return File(element?.name ?: "").name
    }

    override fun getType(): RDFNode {
        return Ontology.JAR_FILE_ENTITY
    }

    override fun extract() {
        println("Running on ${element?.name}")
        if (CodeOntology.extractProjectStructure()) {
            tagName()
            tagType()
            tagDependency()
        }
        tagPackages()
        println("Triples extracted successfully.")
    }

    private fun tagDependency() {
        val mainProject: ProjectEntity<*> = CodeOntology.getProject()
        getLogger().addTriple(mainProject, Ontology.DEPENDENCY_PROPERTY, this)
    }

    private fun tagPackages() {
        packs.forEach(PackageEntity::extract)
    }

    private fun setPackages() {
        packs = ArrayList()
        buildMap()
        val packages: Set<Package> = map.keys
        for (pack: Package in packages) {
            val packageReference: CtPackageReference = ReflectionFactory.getInstance().createPackageReference(pack)
            val entity: PackageEntity = EntityFactory.getInstance().wrap(packageReference)
            entity.setTypes(map[pack]!!)
            entity.parent = this
            packs.add(entity)
        }
    }

    fun tagName() {
        val name: String = getName()
        val label: Literal = model.createTypedLiteral(name)
        getLogger().addTriple(this, Ontology.RDFS_LABEL_PROPERTY, label)
    }

    private fun buildMap() {
        println("Analyzing file ${element?.name}")
        val entries: Enumeration<JarEntry>? = element?.entries()
        map = HashMap()
        if (entries != null) {
            while (entries.hasMoreElements()) {
                val entry: JarEntry = entries.nextElement() as JarEntry
                val entryPath: String = entry.name
                if (entryPath.endsWith(".class")) {
                    val typeName: String = entry.name.replace("/", ".").substring(0, entryPath.length - 6)
                    try {
                        val clazz: Class<*> = Class.forName(typeName)
                        val pack: Package? = clazz.getPackage()
                        val types: ArrayList<Class<*>> = map[pack] as ArrayList<Class<*>>
                        if (pack != null) {
                            types.add(clazz)
                            map[pack] = types
                        }
                    } catch (e: Throwable) {
                        // Cannot get a class object from this jar entry
                        // we just skip this entry
                    }
                }
            }
        }
    }
}