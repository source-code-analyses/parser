package org.codeontology.extraction.support

import org.codeontology.extraction.Entity
import spoon.reflect.declaration.CtModifiable

interface ModifiableEntity<T: CtModifiable>: Entity<T> {
    fun getModifiers(): List<Modifier>

    fun tagModifiers()
}