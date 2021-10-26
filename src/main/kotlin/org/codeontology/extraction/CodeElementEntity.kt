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

import org.codeontology.Ontology
import org.codeontology.extraction.declaration.TypeEntity
import spoon.reflect.cu.SourcePosition
import spoon.reflect.declaration.CtAnnotation
import spoon.reflect.declaration.CtElement
import spoon.reflect.declaration.CtType

abstract class CodeElementEntity<E: CtElement>(element: E?): AbstractEntity<E>(element) {
    public override fun buildRelativeURI(): String {
        return buildRelativeURI("")
    }

    protected constructor() : this(null)

    protected fun buildRelativeURI(uri: String?): String {
        val tag: String?
        val position: SourcePosition? = element!!.position
        val builder: StringBuilder = StringBuilder()
        tag = uri?.trim() ?: ""

        if (position == null) {
            builder.append(parent!!.getRelativeURI())
            if (tag != "") {
                builder.append(SEPARATOR).append(tag)
            }
            builder.append(SEPARATOR).append("-1")
            return builder.toString()
        }

        val mainType: CtType<*> = position.compilationUnit.mainType
        val mainTypeEntity: TypeEntity<*> = getFactory().wrap(mainType)!!
        builder.append(mainTypeEntity.getRelativeURI())
        if (tag != "") {
            builder.append(SEPARATOR).append(tag)
        }
        builder.append(SEPARATOR)
                .append(position.line)
                .append(SEPARATOR)
                .append(position.column)
                .append(SEPARATOR)
                .append(position.endColumn)

        return builder.toString()
    }

    open fun tagComment() {
        val comment: String? = element!!.docComment
        if (comment != null) {
            getLogger().addTriple(this, Ontology.COMMENT_PROPERTY, model.createLiteral(comment))
        }
    }

    fun tagAnnotations() {
        val annotations: List<CtAnnotation<*>> = element!!.annotations
        for(annotation: CtAnnotation<*> in annotations) {
            val annotationType: TypeEntity<*> = getFactory().wrap(annotation.annotationType)!!
            getLogger().addTriple(this, Ontology.ANNOTATION_PROPERTY, annotationType)
            annotationType.follow()
        }
    }
}