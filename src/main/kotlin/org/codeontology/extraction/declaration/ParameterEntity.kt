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
import org.apache.jena.rdf.model.RDFNode
import org.codeontology.Ontology
import org.codeontology.docparser.DocCommentParser
import org.codeontology.docparser.ParamTag
import org.codeontology.docparser.Tag
import org.codeontology.extraction.Entity
import org.codeontology.extraction.NamedElementEntity
import org.codeontology.extraction.support.JavaTypeTagger
import org.codeontology.extraction.support.TypedElementEntity
import spoon.reflect.declaration.CtExecutable
import spoon.reflect.declaration.CtParameter
import spoon.reflect.reference.CtTypeReference

public class ParameterEntity: NamedElementEntity<CtParameter<*>>, TypedElementEntity<CtParameter<*>> {
    public var position: Int = 0
    private var parameterAvailable: Boolean = true

    companion object {
        @JvmStatic private val TAG: String = "parameter"
    }

    public constructor(parameter: CtParameter<*>): super(parameter) {
        parameterAvailable = true
    }

    public constructor(reference: CtTypeReference<*>): super(reference) {
        parameterAvailable = false
        if (reference.qualifiedName.equals(CtTypeReference.NULL_TYPE_NAME)) {
            throw NullTypeException()
        }
    }

    public override fun extract() {
        tagType()
        tagJavaType()
        tagPosition()
        if (isDeclarationAvailable()) {
            tagAnnotations()
            tagName()
            tagLabel()
            tagComment()
        }
    }

    public override fun buildRelativeURI(): String {
        return parent!!.getRelativeURI() + SEPARATOR + TAG + SEPARATOR + position
    }

    public fun tagPosition() {
        getLogger().addTriple(this, Ontology.POSITION_PROPERTY, model.createTypedLiteral(position))
    }

    protected override fun getType(): RDFNode {
        return Ontology.PARAMETER_ENTITY
    }

    public override fun getJavaType(): TypeEntity<*> {
        val type: TypeEntity<*>? = if (isDeclarationAvailable()) {
            getFactory().wrap(element!!.type)
        } else {
            getFactory().wrap(reference as CtTypeReference<*>)
        }
        type!!.parent = parent
        return type
    }

    public override fun tagJavaType() {
        JavaTypeTagger(this).tagJavaType()
    }

    public override fun isDeclarationAvailable(): Boolean {
        return parameterAvailable
    }

    public override fun tagComment() {
        if(!(parent as ExecutableEntity<*>).isDeclarationAvailable()) {
            return
        }
        val methodComment: String = (parent as ExecutableEntity<*>).element?.docComment ?: return
        val parser = DocCommentParser(methodComment)
        val tags: List<Tag> = parser.getParamTags()

        for(tag: Tag in tags) {
            val paramTag: ParamTag = tag as ParamTag
            if (paramTag.parameterName == element?.simpleName) {
                val comment: Literal = model.createLiteral(paramTag.parameterComment)
                getLogger().addTriple(this, Ontology.COMMENT_PROPERTY, comment)
                break
            }
        }
    }
}