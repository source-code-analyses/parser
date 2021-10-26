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
import org.codeontology.build.DefaultProject
import org.codeontology.build.gradle.AndroidProject
import org.codeontology.build.gradle.GradleProject
import org.codeontology.build.maven.MavenProject
import org.codeontology.extraction.declaration.*
import org.codeontology.extraction.expression.AssignmentExpressionEntity
import org.codeontology.extraction.expression.ClassInstanceCreationExpression
import org.codeontology.extraction.expression.ExpressionEntity
import org.codeontology.extraction.expression.MethodInvocationExpressionEntity
import org.codeontology.extraction.project.DefaultProjectEntity
import org.codeontology.extraction.project.GradleProjectEntity
import org.codeontology.extraction.project.JarFileEntity
import org.codeontology.extraction.project.MavenProjectEntity
import org.codeontology.extraction.statement.*
import spoon.reflect.code.*
import spoon.reflect.declaration.*
import spoon.reflect.reference.*
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

import java.util.ArrayList
import java.util.jar.JarFile
import java.util.stream.Collectors

class EntityFactory {
    companion object {
        @JvmStatic private var instance: EntityFactory? = null
        @JvmStatic fun getInstance(): EntityFactory {
            if(instance == null) {
                instance = EntityFactory()
            }

            return instance as EntityFactory
        }
    }

    fun wrap(pack: CtPackage): PackageEntity {
        return PackageEntity(pack)
    }

    fun wrap(pack: CtPackageReference): PackageEntity {
        return PackageEntity(pack)
    }

    fun wrap(field: CtField<*>): FieldEntity {
        return FieldEntity(field)
    }

    fun wrap(field: CtFieldReference<*>): FieldEntity {
        return FieldEntity(field)
    }

    fun wrap(method: CtMethod<*>): MethodEntity {
        return MethodEntity(method)
    }

    fun wrap(type: CtType<*>): TypeEntity<*>? {
        return wrap(type.reference)
    }

    fun wrap(reference: CtTypeReference<*>?): TypeEntity<*>? {
        if(reference == null) {
            return null
        }

        var entity: TypeEntity<*>? = null

        if (reference.qualifiedName.equals(CtTypeReference.NULL_TYPE_NAME)) {
            return null
        }

        val kind: TypeKind = TypeKind.getKindOf(reference) ?: return null

        when(kind) {
            TypeKind.CLASS -> entity = ClassEntity<TypeEntity<*>>(reference)
            TypeKind.INTERFACE -> entity = InterfaceEntity(reference)
            TypeKind.ENUM -> entity = EnumEntity<Enum<*>>(reference)
            TypeKind.ANNOTATION -> entity = AnnotationEntity(reference)
            TypeKind.PRIMITIVE -> entity = PrimitiveTypeEntity(reference)
            TypeKind.ARRAY -> entity = ArrayEntity(reference)
            TypeKind.TYPE_VARIABLE -> entity = TypeVariableEntity(reference)
            TypeKind.PARAMETERIZED_TYPE -> {
                entity = if(CodeOntology.processGenerics()) {
                    ParameterizedTypeEntity(reference)
                } else {
                    ParameterizedTypeEntity(reference).getGenericType()
                }
            }
        }

        return entity
    }

    fun wrap(variable: CtLocalVariable<*>): LocalVariableEntity {
        return LocalVariableEntity(variable)
    }

    fun wrap(parameter: CtParameter<*>): ParameterEntity? {
        return try {
            ParameterEntity(parameter)
        } catch (e: NullTypeException) {
            null
        }
    }

    fun wrapByTypeReference(reference: CtTypeReference<*>): ParameterEntity {
        return ParameterEntity(reference)
    }

    fun wrap(construct: CtConstructor<*>): ConstructorEntity {
        return ConstructorEntity(construct)
    }

    fun wrap(reference: CtExecutableReference<*>): ExecutableEntity<*> {
        return if (reference.isConstructor) {
            ConstructorEntity(reference)
        } else {
            MethodEntity(reference)
        }
    }

    fun wrap(lambda: CtLambda<*>): LambdaEntity {
        return LambdaEntity(lambda)
    }

    fun wrap(typeVariable: TypeVariable<*>): TypeVariableEntity {
        val reference: CtTypeParameterReference = reflectionFactory().createTypeVariableReference(typeVariable)
        return TypeVariableEntity(reference)
    }

    fun wrap(array: GenericArrayType): ArrayEntity {
        val reference: CtTypeReference<*> = reflectionFactory().createGenericArrayReference(array)
        return ArrayEntity(reference)
    }

    fun wrap(parameterizedType: ParameterizedType): ParameterizedTypeEntity {
        val reference: CtTypeReference<*> = reflectionFactory().createParameterizedTypeReference(parameterizedType)
        return ParameterizedTypeEntity(reference)
    }

    fun wrap(type: Type): TypeEntity<*>? {
        return wrap(reflectionFactory().createTypeReference(type))
    }

    fun wrap(project: DefaultProject): DefaultProjectEntity {
        return DefaultProjectEntity(project)
    }

    fun wrap(project: GradleProject): GradleProjectEntity {
        return GradleProjectEntity(project)
    }

    fun wrap(project: MavenProject): MavenProjectEntity {
        return MavenProjectEntity(project)
    }

    fun wrap(project: AndroidProject): GradleProjectEntity {
        return GradleProjectEntity(project)
    }

    fun wrap(jarFile: JarFile): JarFileEntity {
        return JarFileEntity(jarFile)
    }

    private fun reflectionFactory(): ReflectionFactory {
        return ReflectionFactory.getInstance()
    }

    /************************************************************
     *                                                          *
     *              STATEMENTS AND EXPRESSIONS                  *
     *                                                          *
     ************************************************************/

    fun wrap(statement: CtStatement): StatementEntity<*> {
        return when(StatementKind.getKindOf(statement)) {
            StatementKind.BLOCK -> BlockEntity(statement as CtBlock<*>)
            StatementKind.IF_THEN_ELSE -> IfThenElseEntity(statement as CtIf)
            StatementKind.SWITCH -> SwitchEntity(statement as CtSwitch<*>)
            StatementKind.WHILE -> WhileEntity(statement as CtWhile)
            StatementKind.DO -> DoWhileEntity(statement as CtDo)
            StatementKind.FOR -> ForEntity(statement as CtFor)
            StatementKind.FOREACH -> ForEachEntity(statement as CtForEach)
            StatementKind.TRY -> TryEntity(statement as CtTry)
            StatementKind.RETURN -> ReturnEntity(statement as CtReturn<*>)
            StatementKind.THROW -> ThrowEntity(statement as CtThrow)
            StatementKind.BREAK -> BreakEntity(statement as CtBreak)
            StatementKind.CONTINUE -> ContinueEntity(statement as CtContinue)
            StatementKind.ASSERT -> AssertEntity(statement as CtAssert<*>)
            StatementKind.SYNCHRONIZED -> SynchronizedEntity(statement as CtSynchronized)
            StatementKind.LOCAL_VARIABLE_DECLARATION -> LocalVariableDeclarationEntity(statement as CtLocalVariable<*>)
            StatementKind.CLASS_DECLARATION -> ClassDeclarationStatement(statement as CtClass<*>)
            StatementKind.EXPRESSION_STATEMENT -> ExpressionStatementEntity(statement)
            else -> StatementEntity(statement)
        }
    }

    fun wrap(statements: List<CtStatement>): StatementExpressionListEntity {
        val statementExpressionWrapper = { statement: CtStatement ->
            if (statement is CtExpression<*>) {
                wrap(statement as CtExpression<*>)
            }
            wrap(statement)
        }

        val list: List<Entity<*>> = statements.stream()
                .map(statementExpressionWrapper)
                .collect(Collectors.toCollection(::ArrayList))

        return StatementExpressionListEntity(list)
    }

    fun wrap(catcher: CtCatch): CatchEntity {
        return CatchEntity(catcher)
    }

    fun wrap(expression: CtExpression<*>): ExpressionEntity<*> {
        if (!CodeOntology.processExpressions()) {
            return ExpressionEntity(expression)
        }

        return when(expression) {
            is CtAssignment<*, *> -> AssignmentExpressionEntity(expression)
            is CtConstructorCall<*> -> ClassInstanceCreationExpression(expression)
            is CtInvocation -> MethodInvocationExpressionEntity(expression)
            else -> ExpressionEntity(expression)
        }
    }

    fun wrap(variable: CtVariable<*>): Entity<*>? {
        if (variable is CtField<*>) {
            return FieldEntity(variable)
        }

        if (variable is CtLocalVariable<*>){
            return LocalVariableEntity(variable)
        }

        return null
    }

    fun wrap(label: CtCase<*>): SwitchLabelEntity {
        if (label.caseExpression != null) {
            return CaseLabelEntity(label)
        }

        return DefaultLabelEntity(label)
    }
}