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

import org.codeontology.extraction.support.BodyHolderEntity
import org.codeontology.extraction.support.BodyTagger
import spoon.reflect.code.CtLoop
import spoon.reflect.code.CtStatement

open class LoopEntity<T: CtLoop>(element: T): StatementEntity<T>(element), BodyHolderEntity<T> {
    override fun extract() {
        super.extract()
        tagBody()
    }

    override fun getBody(): StatementEntity<*>? {
        val statement: CtStatement? = element!!.body
        if (statement != null) {
            val body: StatementEntity<*> = getFactory().wrap(statement)
            body.parent = this
            return body
        }

        return null
    }

    override fun tagBody() {
        BodyTagger(this).tagBody()
    }
}