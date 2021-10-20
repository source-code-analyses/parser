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

import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.RDFNode
import org.codeontology.CodeOntology
import org.codeontology.Ontology
import org.codeontology.extraction.Entity
import org.codeontology.extraction.RDFLogger
import org.codeontology.extraction.ReflectionFactory
import org.codeontology.extraction.support.FormalTypeParametersTagger
import org.codeontology.extraction.support.GenericDeclarationEntity
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtConstructor
import spoon.reflect.declaration.CtType
import spoon.reflect.reference.CtExecutableReference
import spoon.reflect.reference.CtTypeReference

import java.lang.reflect.Constructor

public open class ClassEntity<T>: TypeEntity<CtClass<T>>, GenericDeclarationEntity<CtClass<T>> {
    private var constructors: ArrayList<ConstructorEntity>? = null
    override val model = RDFLogger.getInstance().model

    public constructor(clazz: CtClass<T>): super(clazz)

    public constructor(reference: CtTypeReference<*>): super(reference)

    protected override fun getType(): RDFNode {
        return Ontology.CLASS_ENTITY
    }

    public override fun extract() {
        tagType()
        tagSimpleName()
        tagCanonicalName()
        tagLabel()
        tagSuperClass()
        tagSuperInterfaces()
        tagModifiers()
        if (isDeclarationAvailable() || CodeOntology.isJarExplorationEnabled()) {
            tagFields()
            tagConstructors()
            tagMethods()
        }
        if (isDeclarationAvailable()) {
            tagAnnotations()
            tagComment()
            tagSourceCode()
            tagNestedTypes()
            tagFormalTypeParameters()
        }
    }

    private fun tagCanonicalName() {
        val qualifiedName: String = getReference().qualifiedName
        val canonicalName: Literal = model.createTypedLiteral(qualifiedName)
        getLogger().addTriple(this, Ontology.CANONICAL_NAME_PROPERTY, canonicalName)
    }

    private fun tagSimpleName() {
        val name: Literal = model.createTypedLiteral(getName())
        getLogger().addTriple(this, Ontology.SIMPLE_NAME_PROPERTY, name)
    }

    public fun tagSuperClass() {
        var superclass: CtTypeReference<*>? = getReference().superclass
        if (superclass == null) {
            superclass = ReflectionFactory.getInstance().createTypeReference(Object::class.java)
        }
        val superClass: TypeEntity<*>? = getFactory().wrap(superclass)
        superClass!!.parent = this
        getLogger().addTriple(this, Ontology.EXTENDS_PROPERTY, superClass)
        superClass.follow()
    }

    public fun tagSuperInterfaces() {
        tagSuperInterfaces(Ontology.IMPLEMENTS_PROPERTY)
    }

    public fun tagConstructors() {
        val constructors: List<ConstructorEntity> = getConstructors()
        constructors.forEach { constructor ->
            getLogger().addTriple(this, Ontology.HAS_CONSTRUCTOR_PROPERTY, constructor)
        }
        getConstructors().forEach(ConstructorEntity::extract)
    }

    public fun getConstructors(): List<ConstructorEntity> {
        if (constructors == null) {
            setConstructors()
        }

        return constructors!!
    }

    private fun setConstructors() {
        constructors = ArrayList()

        if (!isDeclarationAvailable()) {
            setConstructorsByReflection()
            return
        }
        val ctConstructors: Set<CtConstructor<T>> = element?.constructors ?: HashSet()
        for(ctConstructor: CtConstructor<*> in ctConstructors) {
            val constructor: ConstructorEntity = getFactory().wrap(ctConstructor)
            constructor.parent = this
            constructors?.add(constructor)
        }
    }

    private fun setConstructorsByReflection() {
        try {
            val actualConstructors: Array<Constructor<*>> = getReference().javaClass.declaredConstructors
            for (actualConstructor: Constructor<*> in actualConstructors) {
                val reference: CtExecutableReference<*> = ReflectionFactory.getInstance().createConstructor(actualConstructor)
                val constructor: ConstructorEntity = getFactory().wrap(reference) as ConstructorEntity
                constructor.parent = this
                constructors?.add(constructor)
            }
        } catch (t: Throwable) {
            showMemberAccessWarning()
        }
    }

    public fun tagNestedTypes() {
        val nestedTypes: Set<CtType<*>> = element?.nestedTypes ?: HashSet()
        for(type: CtType<*> in nestedTypes) {
            val entity: Entity<*>? = getFactory().wrap(type)
            getLogger().addTriple(entity!!, Ontology.DECLARED_BY_PROPERTY, this)
            entity.extract()
        }
    }

    public override fun getFormalTypeParameters(): List<TypeVariableEntity> {
        return FormalTypeParametersTagger.formalTypeParametersOf(this)
    }

    public override fun tagFormalTypeParameters() {
        FormalTypeParametersTagger(this).tagFormalTypeParameters()
    }
}