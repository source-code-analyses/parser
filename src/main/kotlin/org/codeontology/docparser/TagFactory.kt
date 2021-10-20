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


public class TagFactory {
    companion object {
        @JvmStatic private var instance: TagFactory? = null
        @JvmStatic public fun getInstance(): TagFactory {
            if(instance == null) {
                instance = TagFactory()
            }

            return instance as TagFactory
        }
    }

    public fun createTag(name: String, text: String): Tag {
        return when(name) {
            ParamTag.TAG -> ParamTag(text)
            ReturnTag.TAG -> ReturnTag(text)
            else -> Tag(name, text)
        }
    }
}