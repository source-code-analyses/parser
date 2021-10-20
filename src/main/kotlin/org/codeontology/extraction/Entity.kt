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

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource

public interface Entity<E> {
    public val element: E?
    public var parent: Entity<*>?
    public val model: Model
        get() = RDFLogger.getInstance().model

    val SEPARATOR: String
        get() = "-"

    fun extract()

    fun getFactory(): EntityFactory

    fun getLogger(): RDFLogger

    fun getParent(vararg classes: Class<*>): Entity<*>?

    fun follow()

    fun getRelativeURI(): String

    fun getResource(): Resource
}