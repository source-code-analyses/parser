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

import org.codeontology.CodeOntology
import org.codeontology.Ontology
import org.codeontology.extraction.Entity
import org.codeontology.extraction.NamedElementEntity
import org.codeontology.extraction.ReflectionFactory
import org.codeontology.extraction.statement.StatementEntity
import org.codeontology.extraction.support.*
import spoon.reflect.code.*
import spoon.reflect.declaration.*
import spoon.reflect.reference.*
import spoon.reflect.visitor.filter.ReferenceTypeFilter
import spoon.support.reflect.reference.CtExecutableReferenceImpl
import spoon.support.reflect.reference.CtFieldReferenceImpl
import spoon.support.reflect.reference.CtLocalVariableReferenceImpl

import java.lang.reflect.Executable
import java.util.stream.Collectors

abstract class ExecutableEntity<E>: NamedElementEntity<E>, ModifiableEntity<E>, MemberEntity<E>, BodyHolderEntity<E>
        where E: CtExecutable<*>, E: CtTypeMember {
    private lateinit var executables: HashSet<ExecutableEntity<*>>
    private lateinit var requestedTypes: HashSet<TypeEntity<*>>
    private lateinit var lambdas: HashSet<LambdaEntity>
    private lateinit var anonymousClasses: HashSet<AnonymousClassEntity<*>>
    private lateinit var localVariables: HashSet<LocalVariableEntity>
    private lateinit var fields: HashSet<FieldEntity>
    private var parameters: ArrayList<ParameterEntity>? = null

    constructor(executable: E): super(executable) {
        initSets()
    }

    constructor(reference: CtExecutableReference<*>): super(reference) {
        initSets()
    }

    private fun initSets() {
        executables = HashSet()
        requestedTypes = HashSet()
        lambdas = HashSet()
        anonymousClasses = HashSet()
        localVariables = HashSet()
        fields = HashSet()
    }

    override fun buildRelativeURI(): String {
        var uri: String = reference.toString()

        when(this) {
            is MethodEntity -> {
                if (parent == null) {
                    parent = (element?.parent as CtType<*>?)?.let { getFactory().wrap(it) }
                }

                if (parent != null) {
                    uri = parent!!.getRelativeURI() + SEPARATOR + uri
                }
            }
        }

        uri = uri.replace(", |#", SEPARATOR)
        return uri
    }

    override fun extract() {
        tagName()
        tagLabel()
        tagType()
        tagDeclaringElement()
        tagParameters()
        tagModifiers()
        tagVarArgs()
        if (isDeclarationAvailable()) {
            processStatements()
            tagRequestedTypes()
            tagExecutables()
            tagRequestedFields()
            tagLocalVariables()
            tagLambdas()
            tagAnonymousClasses()
            tagAnnotations()
            tagComment()
            tagSourceCode()
            tagThrows()
            tagBody()
        }
    }

    override fun getDeclaringElement(): Entity<*>? {
        val reference: CtExecutableReference<*> = reference as CtExecutableReference<*>
        return getFactory().wrap(reference.declaringType)
    }

    override fun tagDeclaringElement() {
        DeclaringElementTagger(this).tagDeclaredBy()
    }

    override fun tagModifiers() {
        ModifiableTagger(this).tagModifiers()
    }

    override fun getModifiers(): List<Modifier> {
        if (isDeclarationAvailable()) {
            return Modifier.asList(element?.modifiers ?: HashSet())
        } else {
            val reference: CtExecutableReference<*> = reference as CtExecutableReference<*>
            val executable: Executable? = ReflectionFactory.getInstance().createActualExecutable(reference)
            if (executable != null) {
                val modifiersCode: Int = executable.modifiers
                return Modifier.asList(modifiersCode)
            }
            return ArrayList()
        }
    }

    private fun tagParameters() {
        val parameters: List<ParameterEntity> = getParameters()
        for(i in parameters.indices) {
            val parameter: ParameterEntity = parameters[i]
            parameter.parent = this
            parameter.position = i
            getLogger().addTriple(this, Ontology.PARAMETER_PROPERTY, parameter)
            parameter.extract()
        }
    }

    private fun getParameters(): List<ParameterEntity> {
        if (parameters == null) {
            setParameters()
        }

        return parameters!!
    }

    private fun setParameters() {
        parameters = if(isDeclarationAvailable()) {
            val parameterList: List<CtParameter<*>> = element?.parameters ?: ArrayList()
            parameterList.stream()
                .map(getFactory()::wrap)
                .collect(Collectors.toCollection(::ArrayList))
        } else {
            val references: List<CtTypeReference<*>> = (reference as CtExecutableReference<*>).parameters
            references.stream()
                .map(getFactory()::wrapByTypeReference)
                .collect(Collectors.toCollection(::ArrayList))
        }
    }

    private fun tagThrows() {
        val thrownTypes: Set<CtTypeReference<out Throwable>> = element?.thrownTypes ?: HashSet()
        for(current: CtTypeReference<out Throwable> in thrownTypes) {
            val thrownType: TypeEntity<*>? = getFactory().wrap(current)
            thrownType!!.parent = this
            getLogger().addTriple(this, Ontology.THROWS_PROPERTY, thrownType)
        }
    }

    private fun processStatements() {
        addRequestedTypes(HashSet(element?.thrownTypes ?: HashSet()))

        val executable: CtExecutable<*>? = element
        val body: CtBlock<*>? = executable?.body

        val statements: List<CtStatement>
        try {
            statements = body?.statements ?: ArrayList()
        } catch (e: NullPointerException) {
            return
        }

        for(statement: CtStatement in statements) {
            if (createsAnonymousClass(statement) || statement is CtClass<*>) {
                addAnonymousClasses(statement)
            } else {
                addRequestedTypes(statement.referencedTypes)
                addInvocations(statement)
                addRequestedFields(statement)
                addLocalVariables(statement)
                addLambdas(statement)
            }
            if (statement is CtReturn<*>) {
                tagReturnsVariable(statement)
            }
        }

    }

    private fun addAnonymousClasses(statement: CtStatement) {
        val newClasses: List<CtNewClass<*>> = statement.getElements{ element -> element != null }
        for(newClass: CtNewClass<*> in newClasses) {
            val anonymousClass: AnonymousClassEntity<*> = AnonymousClassEntity(newClass.anonymousClass)
            anonymousClass.parent = this
            anonymousClasses.add(anonymousClass)
        }
    }

    private fun tagAnonymousClasses() {
        for(anonymousClass: AnonymousClassEntity<*> in anonymousClasses) {
            getLogger().addTriple(this, Ontology.CONSTRUCTS_PROPERTY, anonymousClass)
            anonymousClass.extract()
            anonymousClass.requestedResources.forEach(this::tagRequests)
        }
    }

    private fun addInvocations(statement: CtStatement) {
        val references: List<CtExecutableReference<*>> = statement.getElements(ReferenceTypeFilter(CtExecutableReferenceImpl::class.java))

        for(reference: CtExecutableReference<*> in references) {
            val executable: CtExecutable<*>? = reference.declaration
            if (executable is CtLambda<*>) {
                val lambda: LambdaEntity = getFactory().wrap(executable)
                lambda.parent = this
                lambdas.add(lambda)
            } else if (reference.parent !is CtExecutableReferenceExpression<*, *>) {
                executables.add(getFactory().wrap(reference))
            }
        }
    }

    private fun tagLambdas() {
        for(lambda: LambdaEntity in lambdas) {
            tagRequests(lambda)
            lambda.extract()
        }
    }

    private fun addLambdas(statement: CtStatement) {
        val lambdas: List<CtLambda<*>> = statement.getElements{ element -> element != null }
        for(lambda: CtLambda<*> in lambdas) {
            val lambdaEntity: LambdaEntity = getFactory().wrap(lambda)
            lambdaEntity.parent = this
            this.lambdas.add(lambdaEntity)
        }
    }

    private fun addRequestedFields(statement: CtStatement) {
        val references: List<CtFieldReference<*>> = statement.getElements(ReferenceTypeFilter(CtFieldReferenceImpl::class.java))
        references.stream()
                .map(getFactory()::wrap)
                .forEach(fields::add)
    }

    private fun tagRequestedFields() {
        for(field: FieldEntity in fields) {
            tagRequests(field)
            field.follow()
        }
    }

    private fun addRequestedTypes(types: Set<CtTypeReference<*>>) {
        for(reference: CtTypeReference<*> in types) {
            if(!reference.isImplicit) {
                val type: TypeEntity<*>? = getFactory().wrap(reference)
                if (type != null) {
                    type.parent = this
                    requestedTypes.add(type)
                }
            }
        }
    }

    private fun tagRequestedTypes() {
        for(type: TypeEntity<*> in requestedTypes) {
            tagRequests(type)
            type.follow()
        }
    }


    private fun createsAnonymousClass(statement: CtStatement): Boolean {
        val newClasses: List<CtNewClass<*>> = statement.getElements{ element -> element != null }
        return newClasses.isNotEmpty()
    }

    private fun addLocalVariables(statement: CtStatement) {
        val references: List<CtLocalVariableReference<*>> = statement.getElements(ReferenceTypeFilter(CtLocalVariableReferenceImpl::class.java))

        for(reference: CtLocalVariableReference<*> in references) {
            val variable: CtLocalVariable<*>? = reference.declaration
            if (variable != null) {
                val localVariable: LocalVariableEntity = getFactory().wrap(variable)
                localVariable.parent = this
                localVariables.add(localVariable)
            }
        }
    }

    private fun tagLocalVariables() {
        for(variable: LocalVariableEntity in localVariables) {
            tagRequests(variable)
            variable.extract()
        }
    }

    private fun tagExecutables() {
        for(executable: ExecutableEntity<*> in executables) {
            tagRequests(executable)
            if (executable is ConstructorEntity) {
                tagConstructs(executable)
            }
            executable.follow()
        }
    }

    private fun tagConstructs(executable: ExecutableEntity<*>) {
        val declaringType: Entity<*>? = executable.getDeclaringElement()
        getLogger().addTriple(this, Ontology.CONSTRUCTS_PROPERTY, declaringType!!)
        declaringType.follow()
    }

    private fun tagRequests(requested: Entity<*>) {
        getLogger().addTriple(this, Ontology.REFERENCES_PROPERTY, requested)
    }

    private fun tagReturnsVariable(returnStatement: CtReturn<*>) {
        val returned: CtExpression<*> = returnStatement.returnedExpression ?: return

        if (returned is CtVariableAccess<*>) {
            val reference: CtVariableReference<*> = returned.variable ?: return

            val variable: CtVariable<*>? = reference.declaration
            if (variable != null) {
                val entity: Entity<*>? = getFactory().wrap(variable)
                if (entity != null) {
                    entity.parent = this
                    getLogger().addTriple(this, Ontology.RETURNS_VAR_PROPERTY, entity)
                }
            }
        }
    }

    fun getRequestedResources(): List<Entity<*>> {
        val requestedResources: ArrayList<Entity<*>> = ArrayList()

        requestedResources.addAll(executables)
        requestedResources.addAll(fields)
        requestedResources.addAll(requestedTypes)

        return requestedResources
    }

    private fun tagVarArgs() {
        val parameters: List<ParameterEntity> = getParameters()
        val size: Int = parameters.size
        var value = false
        if (size != 0) {
            val last: ParameterEntity = parameters[size - 1]
            if (last.isDeclarationAvailable()) {
                value = last.element!!.isVarArgs
            } else {
                val reference: CtExecutableReference<*> = reference as CtExecutableReference<*>
                val executable: Executable? = ReflectionFactory.getInstance().createActualExecutable(reference)
                if (executable != null) {
                    value = executable.isVarArgs
                }
            }
        }
        getLogger().addTriple(this, Ontology.VAR_ARGS_PROPERTY, model.createTypedLiteral(value))
    }

    override fun getBody(): StatementEntity<*>? {
        val executable: CtExecutable<*>? = element
        val body: CtBlock<*>? = executable?.body
        if (body != null) {
            val bodyEntity: StatementEntity<*> = getFactory().wrap(body)
            bodyEntity.parent = this
            return bodyEntity
        }

        return null
    }

    override fun tagBody() {
        if (CodeOntology.processStatements()) {
            BodyTagger(this).tagBody()
        }
    }
}