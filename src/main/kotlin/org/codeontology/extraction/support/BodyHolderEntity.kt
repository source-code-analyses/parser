package org.codeontology.extraction.support

import spoon.reflect.declaration.CtElement
import org.codeontology.extraction.Entity
import org.codeontology.extraction.statement.StatementEntity

public interface BodyHolderEntity<E: CtElement>: Entity<E> {
    fun getBody(): StatementEntity<*>?
    fun tagBody()
}