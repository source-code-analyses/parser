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
        @JvmStatic public val RDF: String = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        @JvmStatic public val RDFS: String = "http://www.w3.org/2000/01/rdf-schema#"
        @JvmStatic public val WOC: String = "http://rdf.webofcode.org/woc/"
        @JvmStatic public val model: Model = ontology()
        @JvmStatic private fun ontology(): Model {
            try {
                val ontology: File = File("${System.getProperty("user.dir")}/ontology/CodeOntology.owl")
                val reader: FileInputStream = FileInputStream(ontology)
                return ModelFactory.createDefaultModel().read(reader, "")
            } catch (e: FileNotFoundException) {
                throw RuntimeException(e)
            }
        }

        @JvmStatic public val PACKAGE_ENTITY: Resource = model.getResource("${WOC}Package")
        @JvmStatic public val CLASS_ENTITY: Resource = model.getResource("${WOC}Class")
        @JvmStatic public val INTERFACE_ENTITY: Resource = model.getResource("${WOC}Interface")
        @JvmStatic public val ENUM_ENTITY: Resource = model.getResource("${WOC}Enum")
        @JvmStatic public val ANNOTATION_ENTITY = model.getResource("${WOC}Annotation")
        @JvmStatic public val PRIMITIVE_ENTITY = model.getResource("${WOC}PrimitiveType")
        @JvmStatic public val ARRAY_ENTITY = model.getResource("${WOC}ArrayType")
        @JvmStatic public val TYPE_VARIABLE_ENTITY = model.getResource("${WOC}TypeVariable")
        @JvmStatic public val PARAMETERIZED_TYPE_ENTITY = model.getResource("${WOC}ParameterizedType")
        @JvmStatic public val FIELD_ENTITY = model.getResource("${WOC}Field")
        @JvmStatic public val CONSTRUCTOR_ENTITY = model.getResource("${WOC}Constructor")
        @JvmStatic public val METHOD_ENTITY = model.getResource("${WOC}Method")
        @JvmStatic public val PARAMETER_ENTITY = model.getResource("${WOC}Parameter")
        @JvmStatic public val LOCAL_VARIABLE_ENTITY = model.getResource("${WOC}LocalVariable")
        @JvmStatic public val LAMBDA_ENTITY = model.getResource("${WOC}LambdaExpression")
        @JvmStatic public val ANONYMOUS_CLASS_ENTITY = model.getResource("${WOC}AnonymousClass")
        @JvmStatic public val TYPE_ARGUMENT_ENTITY = model.getResource("${WOC}TypeArgument")
        @JvmStatic public val WILDCARD_ENTITY = model.getResource("${WOC}Wildcard")
        @JvmStatic public val PROJECT_ENTITY = model.getResource("${WOC}Project")
        @JvmStatic public val MAVEN_PROJECT_ENTITY = model.getResource("${WOC}MavenProject")
        @JvmStatic public val GRADLE_PROJECT_ENTITY = model.getResource("${WOC}GradleProject")
        @JvmStatic public val JAR_FILE_ENTITY = model.getResource("${WOC}JarFile")
        @JvmStatic public val STATEMENT_ENTITY = model.getResource("${WOC}Statement")
        @JvmStatic public val BLOCK_ENTITY = model.getResource("${WOC}BlockStatement")
        @JvmStatic public val IF_THEN_ELSE_ENTITY = model.getResource("${WOC}IfThenElseStatement")
        @JvmStatic public val SWITCH_ENTITY = model.getResource("${WOC}SwitchStatement")
        @JvmStatic public val CASE_ENTITY = model.getResource("${WOC}CaseLabeledBlock")
        @JvmStatic public val DEFAULT_ENTITY = model.getResource("${WOC}DefaultLabeledBlock")
        @JvmStatic public val WHILE_ENTITY = model.getResource("${WOC}WhileStatement")
        @JvmStatic public val DO_WHILE_ENTITY = model.getResource("${WOC}DoStatement")
        @JvmStatic public val FOR_ENTITY = model.getResource("${WOC}ForStatement")
        @JvmStatic public val FOR_EACH_ENTITY = model.getResource("${WOC}ForEachStatement")
        @JvmStatic public val TRY_ENTITY = model.getResource("${WOC}TryStatement")
        @JvmStatic public val RETURN_ENTITY = model.getResource("${WOC}ReturnStatement")
        @JvmStatic public val THROW_ENTITY = model.getResource("${WOC}ThrowSatement")
        @JvmStatic public val BREAK_ENTITY = model.getResource("${WOC}BreakStatement")
        @JvmStatic public val CONTINUE_ENTITY = model.getResource("${WOC}ContinueStatement")
        @JvmStatic public val ASSERT_ENTITY = model.getResource("${WOC}AssertStatement")
        @JvmStatic public val SYNCHRONIZED_ENTITY = model.getResource("${WOC}SynchronizedStatement")
        @JvmStatic public val LOCAL_VARIABLE_DECLARATION_ENTITY = model.getResource("${WOC}LocalVariableDeclarationStatement")
        @JvmStatic public val CLASS_DECLARATION_ENTITY = model.getResource("${WOC}ClassDeclarationStatement")
        @JvmStatic public val EXPRESSION_STATEMENT_ENTITY = model.getResource("${WOC}ExpressionStatement")
        @JvmStatic public val STATEMENT_EXPRESSION_LIST_ENTITY = model.getResource("${WOC}StatementExpressionList")
        @JvmStatic public val CATCH_ENTITY = model.getResource("${WOC}CatchBlock")
        @JvmStatic public val FINALLY_ENTITY = model.getResource("${WOC}FinallyBlock")
        @JvmStatic public val EXPRESSION_ENTITY = model.getResource("${WOC}Expression")
        @JvmStatic public val ASSIGNMENT_EXPRESSION_ENTITY = model.getResource("${WOC}AssignmentExpression")
        @JvmStatic public val METHOD_INVOCATION_EXPRESSION_ENTITY = model.getResource("${WOC}MethodInvocationExpression")
        @JvmStatic public val ACTUAL_ARGUMENT_ENTITY = model.getResource("${WOC}ActualArgument")
        @JvmStatic public val CLASS_INSTANCE_CREATION_EXPRESSION_ENTITY = model.getResource("${WOC}ClassInstanceCreationExpression")

        @JvmStatic public val RDF_TYPE_PROPERTY: Property = model.getProperty(RDF + "type")
        @JvmStatic public val RDFS_LABEL_PROPERTY: Property = model.getProperty(RDFS + "label")
        @JvmStatic public val JAVA_TYPE_PROPERTY: Property = model.getProperty("${WOC}hasType")
        @JvmStatic public val COMMENT_PROPERTY: Property = model.getProperty(RDFS + "comment")
        @JvmStatic public val NAME_PROPERTY: Property = model.getProperty("${WOC}hasName")
        @JvmStatic public val SIMPLE_NAME_PROPERTY: Property = model.getProperty("${WOC}hasSimpleName")
        @JvmStatic public val CANONICAL_NAME_PROPERTY: Property = model.getProperty("${WOC}hasCanonicalName")
        @JvmStatic public val DECLARED_BY_PROPERTY: Property = model.getProperty("${WOC}isDeclaredBy")
        @JvmStatic public val HAS_PACKAGE_PROPERTY: Property = model.getProperty("${WOC}hasPackage")
        @JvmStatic public val IS_PACKAGE_OF_PROPERTY: Property = model.getProperty("${WOC}isPackageOf")
        @JvmStatic public val HAS_CONSTRUCTOR_PROPERTY: Property = model.getProperty("${WOC}hasConstructor")
        @JvmStatic public val HAS_METHOD_PROPERTY: Property = model.getProperty("${WOC}hasMethod")
        @JvmStatic public val HAS_FIELD_PROPERTY: Property = model.getProperty("${WOC}hasField")
        @JvmStatic public val RETURN_TYPE_PROPERTY: Property = model.getProperty("${WOC}hasReturnType")
        @JvmStatic public val RETURNS_VAR_PROPERTY: Property = model.getProperty("${WOC}returns")
        @JvmStatic public val RETURN_DESCRIPTION_PROPERTY: Property = model.getProperty("${WOC}hasReturnDescription")
        @JvmStatic public val CONSTRUCTS_PROPERTY: Property = model.getProperty("${WOC}constructs")
        @JvmStatic public val PARAMETER_PROPERTY: Property = model.getProperty("${WOC}hasParameter")
        @JvmStatic public val POSITION_PROPERTY: Property = model.getProperty("${WOC}hasPosition")
        @JvmStatic public val SOURCE_CODE_PROPERTY: Property = model.getProperty("${WOC}hasSourceCode")
        @JvmStatic public val THROWS_PROPERTY: Property = model.getProperty("${WOC}throws")
        @JvmStatic public val MODIFIER_PROPERTY: Property = model.getProperty("${WOC}hasModifier")
        @JvmStatic public val REFERENCES_PROPERTY: Property = model.getProperty("${WOC}references")
        @JvmStatic public val EXTENDS_PROPERTY: Property = model.getProperty("${WOC}extends")
        @JvmStatic public val IMPLEMENTS_PROPERTY: Property = model.getProperty("${WOC}implements")
        @JvmStatic public val SUPER_PROPERTY: Property = model.getProperty("${WOC}hasSuperBound")
        @JvmStatic public val ARRAY_OF_PROPERTY: Property = model.getProperty("${WOC}isArrayOf")
        @JvmStatic public val DIMENSIONS_PROPERTY: Property = model.getProperty("${WOC}hasDimensions")
        @JvmStatic public val FORMAL_TYPE_PARAMETER_PROPERTY: Property = model.getProperty("${WOC}hasFormalTypeParameter")
        @JvmStatic public val ACTUAL_TYPE_ARGUMENT_PROPERTY: Property = model.getProperty("${WOC}hasActualTypeArgument")
        @JvmStatic public val GENERIC_TYPE_PROPERTY: Property = model.getProperty("${WOC}hasGenericType")
        @JvmStatic public val ANNOTATION_PROPERTY: Property = model.getProperty("${WOC}hasAnnotation")
        @JvmStatic public val OVERRIDES_PROPERTY: Property = model.getProperty("${WOC}overrides")
        @JvmStatic public val VAR_ARGS_PROPERTY: Property = model.getProperty("${WOC}isVarArgs")
        @JvmStatic public val PROJECT_PROPERTY: Property = model.getProperty("${WOC}hasProject")
        @JvmStatic public val SUBPROJECT_PROPERTY: Property = model.getProperty("${WOC}hasSubProject")
        @JvmStatic public val BUILD_FILE_PROPERTY: Property = model.getProperty("${WOC}hasBuildFile")
        @JvmStatic public val DEPENDENCY_PROPERTY: Property = model.getProperty("${WOC}hasDependency")
        @JvmStatic public val LINE_PROPERTY: Property = model.getProperty("${WOC}hasLine")
        @JvmStatic public val NEXT_PROPERTY: Property = model.getProperty("${WOC}hasNextStatement")
        @JvmStatic public val CONDITION_PROPERTY: Property = model.getProperty("${WOC}hasCondition")
        @JvmStatic public val STATEMENT_PROPERTY: Property = model.getProperty("${WOC}hasSubStatement")
        @JvmStatic public val THEN_PROPERTY: Property = model.getProperty("${WOC}hasThenBranch")
        @JvmStatic public val ELSE_PROPERTY: Property = model.getProperty("${WOC}hasElseBranch")
        @JvmStatic public val BODY_PROPERTY: Property = model.getProperty("${WOC}hasBody")
        @JvmStatic public val END_LINE_PROPERTY: Property = model.getProperty("${WOC}hasEndLine")
        @JvmStatic public val FOR_INIT_PROPERTY: Property = model.getProperty("${WOC}hasForInit")
        @JvmStatic public val FOR_UPDATE_PROPERTY: Property = model.getProperty("${WOC}hasForUpdate")
        @JvmStatic public val EXPRESSION_PROPERTY: Property = model.getProperty("${WOC}hasSubExpression")
        @JvmStatic public val RETURNED_EXPRESSION_PROPERTY: Property = model.getProperty("${WOC}hasReturnedExpression")
        @JvmStatic public val THROWN_EXPRESSION_PROPERTY: Property = model.getProperty("${WOC}hasThrownExpression")
        @JvmStatic public val ASSERT_EXPRESSION_PROPERTY: Property = model.getProperty("${WOC}hasAssertExpression")
        @JvmStatic public val VARIABLE_PROPERTY: Property = model.getProperty("${WOC}hasVariable")
        @JvmStatic public val CATCH_CLAUSE_PROPERTY: Property = model.getProperty("${WOC}hasCatchClause")
        @JvmStatic public val CATCH_FORMAL_PARAMETER_PROPERTY: Property = model.getProperty("${WOC}hasCatchFormalParameter")
        @JvmStatic public val FINALLY_CLAUSE_PROPERTY: Property = model.getProperty("${WOC}hasFinallyClause")
        @JvmStatic public val RESOURCE_PROPERTY: Property = model.getProperty("${WOC}hasResource")
        @JvmStatic public val TARGETED_LABEL_PROPERTY: Property = model.getProperty("${WOC}hasTargetedLabel")
        @JvmStatic public val WOC_LABEL_PROPERTY: Property = model.getProperty("${WOC}hasLabel")
        @JvmStatic public val INITIALIZER_PROPERTY: Property = model.getProperty("${WOC}hasInitializer")
        @JvmStatic public val DECLARATION_PROPERTY: Property = model.getProperty("${WOC}hasDeclaration")
        @JvmStatic public val SWITCH_LABEL_PROPERTY: Property = model.getProperty("${WOC}hasSwitchLabel")
        @JvmStatic public val LEFT_HAND_SIDE_PROPERTY: Property = model.getProperty("${WOC}hasLeftHandSide")
        @JvmStatic public val INVOKES_PROPERTY: Property = model.getProperty("${WOC}invokes")
        @JvmStatic public val ARGUMENT_PROPERTY: Property = model.getProperty("${WOC}hasArgument")
        @JvmStatic public val TARGET_PROPERTY: Property = model.getProperty("${WOC}hasTarget")

        @JvmStatic public val PUBLIC_INDIVIDUAL = model.getResource("${WOC}Public")
        @JvmStatic public val PRIVATE_INDIVIDUAL = model.getResource("${WOC}Private")
        @JvmStatic public val PROTECTED_INDIVIDUAL = model.getResource("${WOC}Protected")
        @JvmStatic public val DEFAULT_INDIVIDUAL = model.getResource("${WOC}Default")
        @JvmStatic public val ABSTRACT_INDIVIDUAL = model.getResource("${WOC}Abstract")
        @JvmStatic public val FINAL_INDIVIDUAL = model.getResource("${WOC}Final")
        @JvmStatic public val STATIC_INDIVIDUAL = model.getResource("${WOC}Static")
        @JvmStatic public val SYNCHRONIZED_INDIVIDUAL = model.getResource("${WOC}Synchronized")
        @JvmStatic public val VOLATILE_INDIVIDUAL = model.getResource("${WOC}Volatile")
    }
}
