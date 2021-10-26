package org.codeontology.extraction.support

import spoon.reflect.declaration.CtElement
import org.codeontology.extraction.Entity
import org.codeontology.extraction.statement.StatementEntity

interface BodyHolderEntity<E: CtElement>: Entity<E> {
    fun getBody(): StatementEntity<*>?
    fun tagBody()
}