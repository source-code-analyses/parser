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

class EntityRegister {
    companion object {
        @JvmStatic private var instance: EntityRegister? = null
        @JvmStatic private val size: Int = 2048
        @JvmStatic private val load: Int = size / 2
        @JvmStatic
        fun getInstance(): EntityRegister {
            if(instance == null) {
                instance = EntityRegister()
            }

            return instance as EntityRegister
        }
    }

    private var register = hashSetOf<String>()
    private var localSize: Int = 0

    fun add(entity: Entity<*>): Boolean {
        handleSize()
        return register.add(entity.getRelativeURI())
    }

    private fun handleSize() {
        localSize++
        if (localSize > load) {
            localSize = 0
            register = hashSetOf()
        }
    }
}