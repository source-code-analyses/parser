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

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Property
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class Ontology {
    companion object {
        @JvmStatic val RDF: String = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        @JvmStatic val RDFS: String = "http://www.w3.org/2000/01/rdf-schema#"
        @JvmStatic val WOC: String = "http://rdf.webofcode.org/woc/"
        @JvmStatic val model: Model = ontology()
        @JvmStatic private fun ontology(): Model {
            try {
                val ontology = File("${System.getProperty("user.dir")}/ontology/CodeOntology.owl")
                val reader = FileInputStream(ontology)
                return ModelFactory.createDefaultModel().read(reader, "")
            } catch (e: FileNotFoundException) {
                throw RuntimeException(e)
            }
        }

        @JvmStatic val PACKAGE_ENTITY: Resource = model.getResource("${WOC}Package")
        @JvmStatic val CLASS_ENTITY: Resource = model.getResource("${WOC}Class")
        @JvmStatic val INTERFACE_ENTITY: Resource = model.getResource("${WOC}Interface")
        @JvmStatic val ENUM_ENTITY: Resource = model.getResource("${WOC}Enum")
        @JvmStatic val ANNOTATION_ENTITY: Resource = model.getResource("${WOC}Annotation")
        @JvmStatic val PRIMITIVE_ENTITY: Resource = model.getResource("${WOC}PrimitiveType")
        @JvmStatic val ARRAY_ENTITY: Resource = model.getResource("${WOC}ArrayType")
        @JvmStatic val TYPE_VARIABLE_ENTITY: Resource = model.getResource("${WOC}TypeVariable")
        @JvmStatic val PARAMETERIZED_TYPE_ENTITY: Resource = model.getResource("${WOC}ParameterizedType")
        @JvmStatic val FIELD_ENTITY: Resource = model.getResource("${WOC}Field")
        @JvmStatic val CONSTRUCTOR_ENTITY: Resource = model.getResource("${WOC}Constructor")
        @JvmStatic val METHOD_ENTITY: Resource = model.getResource("${WOC}Method")
        @JvmStatic val PARAMETER_ENTITY: Resource = model.getResource("${WOC}Parameter")
        @JvmStatic val LOCAL_VARIABLE_ENTITY: Resource = model.getResource("${WOC}LocalVariable")
        @JvmStatic val LAMBDA_ENTITY: Resource = model.getResource("${WOC}LambdaExpression")
        @JvmStatic val ANONYMOUS_CLASS_ENTITY: Resource = model.getResource("${WOC}AnonymousClass")
        @JvmStatic val TYPE_ARGUMENT_ENTITY: Resource = model.getResource("${WOC}TypeArgument")
        @JvmStatic val WILDCARD_ENTITY: Resource = model.getResource("${WOC}Wildcard")
        @JvmStatic val PROJECT_ENTITY: Resource = model.getResource("${WOC}Project")
        @JvmStatic val MAVEN_PROJECT_ENTITY: Resource = model.getResource("${WOC}MavenProject")
        @JvmStatic val GRADLE_PROJECT_ENTITY: Resource = model.getResource("${WOC}GradleProject")
        @JvmStatic val JAR_FILE_ENTITY: Resource = model.getResource("${WOC}JarFile")
        @JvmStatic val STATEMENT_ENTITY: Resource = model.getResource("${WOC}Statement")
        @JvmStatic val BLOCK_ENTITY: Resource = model.getResource("${WOC}BlockStatement")
        @JvmStatic val IF_THEN_ELSE_ENTITY: Resource = model.getResource("${WOC}IfThenElseStatement")
        @JvmStatic val SWITCH_ENTITY: Resource = model.getResource("${WOC}SwitchStatement")
        @JvmStatic val CASE_ENTITY: Resource = model.getResource("${WOC}CaseLabeledBlock")
        @JvmStatic val DEFAULT_ENTITY: Resource = model.getResource("${WOC}DefaultLabeledBlock")
        @JvmStatic val WHILE_ENTITY: Resource = model.getResource("${WOC}WhileStatement")
        @JvmStatic val DO_WHILE_ENTITY: Resource = model.getResource("${WOC}DoStatement")
        @JvmStatic val FOR_ENTITY: Resource = model.getResource("${WOC}ForStatement")
        @JvmStatic val FOR_EACH_ENTITY: Resource = model.getResource("${WOC}ForEachStatement")
        @JvmStatic val TRY_ENTITY: Resource = model.getResource("${WOC}TryStatement")
        @JvmStatic val RETURN_ENTITY: Resource = model.getResource("${WOC}ReturnStatement")
        @JvmStatic val THROW_ENTITY: Resource = model.getResource("${WOC}ThrowSatement")
        @JvmStatic val BREAK_ENTITY: Resource = model.getResource("${WOC}BreakStatement")
        @JvmStatic val CONTINUE_ENTITY: Resource = model.getResource("${WOC}ContinueStatement")
        @JvmStatic val ASSERT_ENTITY: Resource = model.getResource("${WOC}AssertStatement")
        @JvmStatic val SYNCHRONIZED_ENTITY: Resource = model.getResource("${WOC}SynchronizedStatement")
        @JvmStatic val LOCAL_VARIABLE_DECLARATION_ENTITY: Resource = model.getResource("${WOC}LocalVariableDeclarationStatement")
        @JvmStatic val CLASS_DECLARATION_ENTITY: Resource = model.getResource("${WOC}ClassDeclarationStatement")
        @JvmStatic val EXPRESSION_STATEMENT_ENTITY: Resource = model.getResource("${WOC}ExpressionStatement")
        @JvmStatic val STATEMENT_EXPRESSION_LIST_ENTITY: Resource = model.getResource("${WOC}StatementExpressionList")
        @JvmStatic val CATCH_ENTITY: Resource = model.getResource("${WOC}CatchBlock")
        @JvmStatic val FINALLY_ENTITY: Resource = model.getResource("${WOC}FinallyBlock")
        @JvmStatic val EXPRESSION_ENTITY: Resource = model.getResource("${WOC}Expression")
        @JvmStatic val ASSIGNMENT_EXPRESSION_ENTITY: Resource = model.getResource("${WOC}AssignmentExpression")
        @JvmStatic val METHOD_INVOCATION_EXPRESSION_ENTITY: Resource = model.getResource("${WOC}MethodInvocationExpression")
        @JvmStatic val ACTUAL_ARGUMENT_ENTITY: Resource = model.getResource("${WOC}ActualArgument")
        @JvmStatic val CLASS_INSTANCE_CREATION_EXPRESSION_ENTITY: Resource = model.getResource("${WOC}ClassInstanceCreationExpression")

        @JvmStatic val RDF_TYPE_PROPERTY: Property = model.getProperty(RDF + "type")
        @JvmStatic val RDFS_LABEL_PROPERTY: Property = model.getProperty(RDFS + "label")
        @JvmStatic val JAVA_TYPE_PROPERTY: Property = model.getProperty("${WOC}hasType")
        @JvmStatic val COMMENT_PROPERTY: Property = model.getProperty(RDFS + "comment")
        @JvmStatic val NAME_PROPERTY: Property = model.getProperty("${WOC}hasName")
        @JvmStatic val SIMPLE_NAME_PROPERTY: Property = model.getProperty("${WOC}hasSimpleName")
        @JvmStatic  val CANONICAL_NAME_PROPERTY: Property = model.getProperty("${WOC}hasCanonicalName")
        @JvmStatic  val DECLARED_BY_PROPERTY: Property = model.getProperty("${WOC}isDeclaredBy")
        @JvmStatic val HAS_PACKAGE_PROPERTY: Property = model.getProperty("${WOC}hasPackage")
        @JvmStatic val IS_PACKAGE_OF_PROPERTY: Property = model.getProperty("${WOC}isPackageOf")
        @JvmStatic val HAS_CONSTRUCTOR_PROPERTY: Property = model.getProperty("${WOC}hasConstructor")
        @JvmStatic val HAS_METHOD_PROPERTY: Property = model.getProperty("${WOC}hasMethod")
        @JvmStatic val HAS_FIELD_PROPERTY: Property = model.getProperty("${WOC}hasField")
        @JvmStatic val RETURN_TYPE_PROPERTY: Property = model.getProperty("${WOC}hasReturnType")
        @JvmStatic val RETURNS_VAR_PROPERTY: Property = model.getProperty("${WOC}returns")
        @JvmStatic val RETURN_DESCRIPTION_PROPERTY: Property = model.getProperty("${WOC}hasReturnDescription")
        @JvmStatic val CONSTRUCTS_PROPERTY: Property = model.getProperty("${WOC}constructs")
        @JvmStatic val PARAMETER_PROPERTY: Property = model.getProperty("${WOC}hasParameter")
        @JvmStatic val POSITION_PROPERTY: Property = model.getProperty("${WOC}hasPosition")
        @JvmStatic val SOURCE_CODE_PROPERTY: Property = model.getProperty("${WOC}hasSourceCode")
        @JvmStatic val THROWS_PROPERTY: Property = model.getProperty("${WOC}throws")
        @JvmStatic val MODIFIER_PROPERTY: Property = model.getProperty("${WOC}hasModifier")
        @JvmStatic val REFERENCES_PROPERTY: Property = model.getProperty("${WOC}references")
        @JvmStatic val EXTENDS_PROPERTY: Property = model.getProperty("${WOC}extends")
        @JvmStatic val IMPLEMENTS_PROPERTY: Property = model.getProperty("${WOC}implements")
        @JvmStatic val SUPER_PROPERTY: Property = model.getProperty("${WOC}hasSuperBound")
        @JvmStatic val ARRAY_OF_PROPERTY: Property = model.getProperty("${WOC}isArrayOf")
        @JvmStatic val DIMENSIONS_PROPERTY: Property = model.getProperty("${WOC}hasDimensions")
        @JvmStatic val FORMAL_TYPE_PARAMETER_PROPERTY: Property = model.getProperty("${WOC}hasFormalTypeParameter")
        @JvmStatic val ACTUAL_TYPE_ARGUMENT_PROPERTY: Property = model.getProperty("${WOC}hasActualTypeArgument")
        @JvmStatic val GENERIC_TYPE_PROPERTY: Property = model.getProperty("${WOC}hasGenericType")
        @JvmStatic val ANNOTATION_PROPERTY: Property = model.getProperty("${WOC}hasAnnotation")
        @JvmStatic val OVERRIDES_PROPERTY: Property = model.getProperty("${WOC}overrides")
        @JvmStatic val VAR_ARGS_PROPERTY: Property = model.getProperty("${WOC}isVarArgs")
        @JvmStatic val PROJECT_PROPERTY: Property = model.getProperty("${WOC}hasProject")
        @JvmStatic val SUBPROJECT_PROPERTY: Property = model.getProperty("${WOC}hasSubProject")
        @JvmStatic val BUILD_FILE_PROPERTY: Property = model.getProperty("${WOC}hasBuildFile")
        @JvmStatic val DEPENDENCY_PROPERTY: Property = model.getProperty("${WOC}hasDependency")
        @JvmStatic val LINE_PROPERTY: Property = model.getProperty("${WOC}hasLine")
        @JvmStatic val NEXT_PROPERTY: Property = model.getProperty("${WOC}hasNextStatement")
        @JvmStatic val CONDITION_PROPERTY: Property = model.getProperty("${WOC}hasCondition")
        @JvmStatic val STATEMENT_PROPERTY: Property = model.getProperty("${WOC}hasSubStatement")
        @JvmStatic val THEN_PROPERTY: Property = model.getProperty("${WOC}hasThenBranch")
        @JvmStatic val ELSE_PROPERTY: Property = model.getProperty("${WOC}hasElseBranch")
        @JvmStatic val BODY_PROPERTY: Property = model.getProperty("${WOC}hasBody")
        @JvmStatic val END_LINE_PROPERTY: Property = model.getProperty("${WOC}hasEndLine")
        @JvmStatic val FOR_INIT_PROPERTY: Property = model.getProperty("${WOC}hasForInit")
        @JvmStatic val FOR_UPDATE_PROPERTY: Property = model.getProperty("${WOC}hasForUpdate")
        @JvmStatic val EXPRESSION_PROPERTY: Property = model.getProperty("${WOC}hasSubExpression")
        @JvmStatic val RETURNED_EXPRESSION_PROPERTY: Property = model.getProperty("${WOC}hasReturnedExpression")
        @JvmStatic val THROWN_EXPRESSION_PROPERTY: Property = model.getProperty("${WOC}hasThrownExpression")
        @JvmStatic val ASSERT_EXPRESSION_PROPERTY: Property = model.getProperty("${WOC}hasAssertExpression")
        @JvmStatic val VARIABLE_PROPERTY: Property = model.getProperty("${WOC}hasVariable")
        @JvmStatic val CATCH_CLAUSE_PROPERTY: Property = model.getProperty("${WOC}hasCatchClause")
        @JvmStatic val CATCH_FORMAL_PARAMETER_PROPERTY: Property = model.getProperty("${WOC}hasCatchFormalParameter")
        @JvmStatic val FINALLY_CLAUSE_PROPERTY: Property = model.getProperty("${WOC}hasFinallyClause")
        @JvmStatic val RESOURCE_PROPERTY: Property = model.getProperty("${WOC}hasResource")
        @JvmStatic val TARGETED_LABEL_PROPERTY: Property = model.getProperty("${WOC}hasTargetedLabel")
        @JvmStatic val WOC_LABEL_PROPERTY: Property = model.getProperty("${WOC}hasLabel")
        @JvmStatic val INITIALIZER_PROPERTY: Property = model.getProperty("${WOC}hasInitializer")
        @JvmStatic val DECLARATION_PROPERTY: Property = model.getProperty("${WOC}hasDeclaration")
        @JvmStatic val SWITCH_LABEL_PROPERTY: Property = model.getProperty("${WOC}hasSwitchLabel")
        @JvmStatic val LEFT_HAND_SIDE_PROPERTY: Property = model.getProperty("${WOC}hasLeftHandSide")
        @JvmStatic val INVOKES_PROPERTY: Property = model.getProperty("${WOC}invokes")
        @JvmStatic val ARGUMENT_PROPERTY: Property = model.getProperty("${WOC}hasArgument")
        @JvmStatic val TARGET_PROPERTY: Property = model.getProperty("${WOC}hasTarget")

        @JvmStatic val PUBLIC_INDIVIDUAL: Resource = model.getResource("${WOC}Public")
        @JvmStatic val PRIVATE_INDIVIDUAL: Resource = model.getResource("${WOC}Private")
        @JvmStatic val PROTECTED_INDIVIDUAL: Resource = model.getResource("${WOC}Protected")
        @JvmStatic val DEFAULT_INDIVIDUAL: Resource = model.getResource("${WOC}Default")
        @JvmStatic val ABSTRACT_INDIVIDUAL: Resource = model.getResource("${WOC}Abstract")
        @JvmStatic val FINAL_INDIVIDUAL: Resource = model.getResource("${WOC}Final")
        @JvmStatic val STATIC_INDIVIDUAL: Resource = model.getResource("${WOC}Static")
        @JvmStatic val SYNCHRONIZED_INDIVIDUAL: Resource = model.getResource("${WOC}Synchronized")
        @JvmStatic val VOLATILE_INDIVIDUAL: Resource = model.getResource("${WOC}Volatile")
    }
}
