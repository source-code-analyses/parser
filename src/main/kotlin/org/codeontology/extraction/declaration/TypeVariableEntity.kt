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

package org.codeontology.extraction.declaration

import org.apache.jena.rdf.model.RDFNode
import org.codeontology.CodeOntology
import org.codeontology.Ontology
import org.codeontology.extraction.Entity
import org.codeontology.extraction.NamedElementEntity
import org.codeontology.extraction.ReflectionFactory
import org.codeontology.extraction.support.GenericDeclarationEntity
import spoon.reflect.declaration.CtElement
import spoon.reflect.declaration.CtExecutable
import spoon.reflect.declaration.CtType
import spoon.reflect.declaration.ParentNotInitializedException
import spoon.reflect.reference.*
import java.lang.Error

import java.lang.reflect.Executable
import java.lang.reflect.TypeVariable
import java.util.*
import kotlin.collections.ArrayList

class TypeVariableEntity constructor(reference: CtTypeReference<*>) : TypeEntity<CtType<*>>(reference) {
    var position: Int = 0
    private var bounds = mutableListOf<CtTypeReference<*>>()
    override var parent: Entity<*>? = super.parent
        set(parent) {
            if(parent == null) {
                return
            }

            if (!CodeOntology.processGenerics() || isWildcard()) {
                field = parent
                return
            }

            val reference: CtReference? = (parent as NamedElementEntity).reference
            val simpleName: String = getReference().simpleName

            var realParent: Entity<*>? = TypeVariableCache.getInstance().getParent(simpleName, parent)

            if (realParent != null) {
                field = parent
                return
            }

            if (parent is GenericDeclarationEntity) {
                realParent = findParent(parent)
            }

            if (realParent == null) {
                if (reference is CtTypeReference<*>) {
                    realParent = findParent(reference)
                } else if (reference is CtExecutableReference<*>) {
                    realParent = findParent(reference)
                }
            }

            if (realParent != null) {
                field = parent
                TypeVariableCache.getInstance().putParent(simpleName, parent, realParent)
            }
        }

    init {
        setBounds()
    }

    private fun setBounds() {
        bounds = ArrayList()
        val reference: CtTypeParameterReference = getReference() as CtTypeParameterReference
        val boundingType: CtTypeReference<*>? = reference.boundingType
        if (boundingType is CtIntersectionTypeReference) {
            bounds = boundingType.asCtIntersectionTypeReference().bounds
        } else if (boundingType != null) {
            bounds.add(boundingType)
        }
    }

    override fun extract() {
        if (!CodeOntology.processGenerics()) {
            return
        }
        tagType()
        tagBounds()
        tagPosition()
    }

    private fun tagPosition() {
        getLogger().addTriple(this, Ontology.POSITION_PROPERTY, model.createTypedLiteral(position))
    }

    private fun tagBounds() {
        bounds.forEach(this::tagBound)
    }

    private fun tagBound(boundReference: CtTypeReference<*>) {
        val bound: TypeEntity<*> = getFactory().wrap(boundReference)!!
        bound.parent = this.parent

        val ownReference = getReference()

        if (ownReference is CtWildcardReference && ownReference.isUpper) {
            getLogger().addTriple(this, Ontology.EXTENDS_PROPERTY, bound)
        } else {
            getLogger().addTriple(this, Ontology.SUPER_PROPERTY, bound)
        }
        bound.follow()
    }

    override fun buildRelativeURI(): String {
        if (!CodeOntology.processGenerics() || parent == null) {
            return getReference().simpleName
        }

        if (isWildcard()) {
            return wildcardURI()
        }

        return getReference().qualifiedName + ":" + parent!!.getRelativeURI()
    }

    private fun wildcardURI(): String {
        val ownReference = getReference()

        val clause: String = if(ownReference is CtWildcardReference && ownReference.isUpper) {
            "extends"
        } else {
            "super"
        }

        var uri = "?"
        val separator = "_"

        if (bounds.size > 0) {
            uri = uri + separator + clause
        }

        for(bound: CtTypeReference<*> in bounds) {
            val entity: TypeEntity<*> = getFactory().wrap(bound)!!
            entity.parent = parent
            uri = uri + separator + entity.getRelativeURI()
        }

        return uri
    }

    override fun getType(): RDFNode {
        return if (isWildcard()) {
            Ontology.WILDCARD_ENTITY
        } else {
            Ontology.TYPE_VARIABLE_ENTITY
        }
    }

    private fun findParent(executable: ExecutableEntity<*>): Entity<*>? {
        val executableReference: CtExecutableReference<*> = executable.reference as CtExecutableReference<*>

        return if (executable.isDeclarationAvailable()) {
            val parameters: List<CtTypeReference<*>> = TypeVariableList((executable.element as CtExecutable<*>).reference.actualTypeArguments)
            if (parameters.contains(getReference())) {
                executable
            } else {
                findParent(executableReference.declaringType)
            }
        } else {
            findParent(executableReference)
        }
    }

    private fun findParent(executableReference: CtExecutableReference<*>): Entity<*>? {
        if (executableReference.declaration != null) {
            return findParent(getFactory().wrap(executableReference))
        }
        if (isWildcard()) {
            return getFactory().wrap(executableReference)
        }

        val executable: Executable = ReflectionFactory.getInstance().createActualExecutable(executableReference)
            ?: return null
        val typeParameters: Array<TypeVariable<*>> = executable.typeParameters
        val declaringClass: Class<*> = executable.declaringClass

        for(current: TypeVariable<*> in typeParameters) {
            if (current.name.equals(getReference().qualifiedName)) {
                return getFactory().wrap(executableReference)
            }
        }

        return findParent(declaringClass)
    }


    private fun findParent(clazz: Class<*>?): Entity<*>? {
        if (clazz != null) {
            val typeParameters: Array<out TypeVariable<*>> = clazz.typeParameters

            for (variable: TypeVariable<*> in typeParameters) {
                if (variable.name.equals(getReference().qualifiedName)) {
                    val reference: CtTypeReference<*> = ReflectionFactory.getInstance().createTypeReference(clazz)
                    return getFactory().wrap(reference)
                }
            }

            return if(clazz.isAnonymousClass) {
                try {
                    val reference: CtExecutableReference<*> = ReflectionFactory.getInstance().createMethod(clazz.enclosingMethod)
                    findParent(reference)
                } catch (e: Exception) {
                    null
                } catch (e: Error) {
                    null
                }
            } else {
                findParent(clazz.declaringClass)
            }
        }

        return null
    }

    private fun findParent(reference: CtTypeReference<*>?): Entity<*>? {
        if (reference != null) {
            return if(isWildcard()) {
                getFactory().wrap(reference)
            } else if (reference.declaration != null) {
                findParent(reference.declaration)
            } else {
                findParent(reference.javaClass)
            }
        }

        return null
    }

    private fun findParent(type: CtType<*>?): Entity<*>? {
        if (type != null) {
            val formalTypes: List<CtTypeReference<*>> = TypeVariableList(type.referencedTypes.toList())
            if (formalTypes.contains(getReference())) {
                return getFactory().wrap(type)
            } else {
                val parent: CtElement? = try {
                    type.parent
                } catch (e: ParentNotInitializedException) {
                    null
                }

                if (parent != null) {
                    val executable: CtExecutable<*>? = parent.getParent(CtExecutable::class.java)
                    if (executable != null) {
                        val result: Entity<*>? = findParent(executable.reference)
                        if (result != null) {
                            return result
                        }
                    }
                }
                return findParent(type.declaringType)
            }
        }

        return null
    }

    private fun findParent(context: GenericDeclarationEntity<*>?): Entity<*>? {
        var tempContext: GenericDeclarationEntity<*>? = context

        while (tempContext != null) {
            val parameters: List<TypeVariableEntity> = tempContext.getFormalTypeParameters()
            for (typeVariable: TypeVariableEntity in parameters) {
                if (typeVariable.getName() == this.getName()) {
                    return tempContext
                }
            }

            val newParent: Entity<*>? = tempContext.getParent(GenericDeclarationEntity::class.java)

            tempContext = if(newParent != null) {
                newParent as GenericDeclarationEntity<*>
            } else {
                null
            }
        }
        return null
    }

    private fun isWildcard(): Boolean {
        return getReference().simpleName.equals("?")
    }
}

class TypeVariableList(parameters: List<CtTypeReference<*>>): ArrayList<CtTypeReference<*>>(parameters) {
    override fun contains(element: CtTypeReference<*>): Boolean {
        if (element !is CtTypeParameterReference) {
            return super.contains(element)
        }
        val parameter: CtTypeReference<*> = element

        if (parameter.simpleName.equals("?")) {
            return true
        }

        for(currentParameter: CtTypeReference<*> in this) {
            if (currentParameter.qualifiedName.equals(parameter.qualifiedName)) {
                return true
            }
        }
        return false
    }
}

class TypeVariableCache {
    companion object {
        @JvmStatic private var instance: TypeVariableCache? = null
        @JvmStatic private val ROWS: Int = 16
        @JvmStatic private val COLS: Int = 48
        @JvmStatic private val MAX_SIZE: Int = (ROWS * COLS) / 2

        @JvmStatic fun getInstance(): TypeVariableCache {
            if (instance == null) {
                instance = TypeVariableCache()
            }

            return instance as TypeVariableCache
        }
    }

    private var table: Hashtable<String, Hashtable<Entity<*>, Entity<*>>> = Hashtable(MAX_SIZE)
    private var size: Int = 0

    fun getParent(name: String, context: Entity<*>): Entity<*>? {
        return table[name]?.get(context)
    }

    fun putParent(name: String, context: Entity<*>, parent: Entity<*>) {
        handleSize()
        var contexts: Hashtable<Entity<*>, Entity<*>>? = table[name]

        if(contexts == null) {
            table[name] = Hashtable()
            contexts = table[name]
        }

        contexts!![context] = parent
    }

    private fun handleSize() {
        size++
        if (size > MAX_SIZE) {
            table = Hashtable()
            size = 0
        }
    }
}