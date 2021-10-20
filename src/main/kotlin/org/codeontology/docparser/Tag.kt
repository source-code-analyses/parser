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

package org.codeontology.docparser

public open class Tag(name: String, text: String) {
    public var text: String = text
        private set(text) {field = text.trim().replace("\\s+".toRegex(), " ")}
    public var name: String = name
        private set(name) {field = name.trim()}

    public override fun toString(): String {
        return "$name $text"
    }

    protected fun splitText(): Array<String> {
        return ensureSize(text.split(" ".toRegex(), 2).toTypedArray())
    }

    private fun ensureSize(array: Array<String>): Array<String> {
        val result: Array<String>

        if(array.size >= 2) {
            result = array
        }
        else {
            result = Array(2) { "" }
            System.arraycopy(array, 0, result, 0, array.size)
        }

        return result
    }
}
