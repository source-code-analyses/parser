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

package org.codeontology.extraction.support

import org.codeontology.Ontology
import org.codeontology.extraction.EntityFactory
import org.codeontology.extraction.RDFLogger
import org.codeontology.extraction.statement.StatementEntity
import spoon.reflect.code.CtStatement

public class StatementsTagger(private final val entity: StatementsHolderEntity<*>) {
    public fun tagStatements() {
        val statements = entity.getStatements()
        val iterator = statements.listIterator()

        if(iterator.hasNext()) {
            var previous: StatementEntity<*> = iterator.next()
            tagStatement(previous)

            while(iterator.hasNext()) {
                val current: StatementEntity<*> = iterator.next()
                RDFLogger.getInstance().addTriple(previous, Ontology.NEXT_PROPERTY, current)
                previous = current
                tagStatement(current)
            }
        }
    }

    private fun tagStatement(statement: StatementEntity<*>) {
        RDFLogger.getInstance().addTriple(entity, Ontology.STATEMENT_PROPERTY, statement)
        statement.extract()
    }

    public fun asEntities(statements: List<CtStatement>): List<StatementEntity<*>> {
        val result = ArrayList<StatementEntity<*>>()

        statements.forEachIndexed { i, item ->
            val statement: StatementEntity<*> = EntityFactory.getInstance().wrap(item)

            statement.position = i
            statement.parent = entity
            result.add(statement)
        }

        return result
    }
}