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

package org.codeontology.extraction.statement


import org.apache.jena.rdf.model.RDFNode
import org.codeontology.Ontology
import org.codeontology.extraction.declaration.LocalVariableEntity
import org.codeontology.extraction.support.BodyHolderEntity
import org.codeontology.extraction.support.BodyTagger
import spoon.reflect.code.*

class TryEntity(element: CtTry):
    StatementEntity<CtTry>(element), BodyHolderEntity<CtTry> {
    override fun getType(): RDFNode {
        return Ontology.TRY_ENTITY
    }

    override fun extract() {
        super.extract()
        tagBody()
        tagCatches()
        tagFinally()
        tagResources()
    }

    fun tagCatches() {
        val iterator: Iterator<CatchEntity> = getCatches().iterator()

        if (!iterator.hasNext()) {
            return
        }

        var current: CatchEntity = iterator.next()
        getLogger().addTriple(this, Ontology.CATCH_CLAUSE_PROPERTY, current)
        current.extract()

        var previous: CatchEntity = current

        while (iterator.hasNext()) {
            current = iterator.next()
            getLogger().addTriple(this, Ontology.CATCH_CLAUSE_PROPERTY, current)
            getLogger().addTriple(previous, Ontology.NEXT_PROPERTY, current)
            current.extract()
            previous = current
        }
    }

    fun tagFinally() {
        val finallyBlock: FinallyEntity? = getFinally()
        if (finallyBlock != null) {
            getLogger().addTriple(this, Ontology.FINALLY_CLAUSE_PROPERTY, finallyBlock)
            finallyBlock.extract()
        }
    }

    private fun getCatches(): List<CatchEntity> {
        val catches: ArrayList<CatchEntity> = ArrayList()
        val catchers: List<CtCatch> = element?.catchers ?: ArrayList()

        for (i in catchers.indices) {
            val catchEntity: CatchEntity = getFactory().wrap(catchers[i])
            catchEntity.position = i
            catchEntity.parent = this
            catches.add(catchEntity)
        }

        return catches
    }

    fun tagResources() {
        val resources: List<LocalVariableEntity> = getResources()
        for (resource: LocalVariableEntity in resources) {
            getLogger().addTriple(this, Ontology.RESOURCE_PROPERTY, resource)
            resource.extract()
        }
    }

    fun getResources(): List<LocalVariableEntity> {
        val result: ArrayList<LocalVariableEntity> = ArrayList()

        if (element is CtTryWithResource) {
            val tryWithResources: CtTryWithResource = element as CtTryWithResource
            val resources: List<CtLocalVariable<*>> = tryWithResources.resources

            for (resource: CtLocalVariable<*> in resources) {
                val variable: LocalVariableEntity = getFactory().wrap(resource)
                variable.parent = this
                result.add(variable)
            }

        }

        return result
    }

    fun getFinally(): FinallyEntity? {
        val block: CtBlock<*>? = element?.finalizer
        if (block != null) {
            val finallyBlock = FinallyEntity(block)
            finallyBlock.parent = this
            return finallyBlock
        }

        return null
    }

    override fun getBody(): StatementEntity<*> {
        val body: StatementEntity<*> = getFactory().wrap(element!!.body)
        body.position = 0
        body.parent = this
        return body
    }

    override fun tagBody() {
        BodyTagger(this).tagBody()
    }
}