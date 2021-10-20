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
import org.codeontology.Ontology
import org.codeontology.extraction.Entity
import org.codeontology.extraction.NamedElementEntity
import org.codeontology.extraction.support.*
import spoon.reflect.code.CtLocalVariable
import spoon.reflect.reference.CtTypeReference

public class LocalVariableEntity(variable: CtLocalVariable<*>): NamedElementEntity<CtLocalVariable<*>>(variable), MemberEntity<CtLocalVariable<*>>, TypedElementEntity<CtLocalVariable<*>>, ModifiableEntity<CtLocalVariable<*>> {
    public override fun extract() {
        tagType()
        tagName()
        tagLabel()
        tagJavaType()
        tagModifiers()
        tagDeclaringElement()
        tagSourceCode()
    }

    public override fun getModifiers(): List<Modifier> {
        return Modifier.asList(element?.modifiers ?: HashSet())
    }

    public override fun tagModifiers() {
        ModifiableTagger(this).tagModifiers()
    }

    public override fun buildRelativeURI(): String {
        return parent?.getRelativeURI() + SEPARATOR + element!!.simpleName
    }

    protected override fun getType(): RDFNode {
        return Ontology.LOCAL_VARIABLE_ENTITY
    }

    public override fun getDeclaringElement(): Entity<*> {
        return parent!!
    }

    public override fun tagDeclaringElement() {
        DeclaringElementTagger(this).tagDeclaredBy()
    }

    public override fun getJavaType(): TypeEntity<*> {
        val type: CtTypeReference<*> = element!!.type
        val entity: TypeEntity<*>? = getFactory().wrap(type)
        entity!!.parent = getParent(ExecutableEntity::class.java, TypeEntity::class.java)!!
        return entity
    }

    public override fun tagJavaType() {
        JavaTypeTagger(this).tagJavaType()
    }
}