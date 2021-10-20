package org.codeontology.extraction.declaration

import org.apache.jena.rdf.model.RDFNode
import org.codeontology.Ontology
import org.codeontology.extraction.Entity
import org.codeontology.extraction.NamedElementEntity
import spoon.reflect.code.CtLambda

public class LambdaEntity(lambda: CtLambda<*>): NamedElementEntity<CtLambda<*>>(lambda) {
    companion object {
        @JvmStatic public val TAG: String = "lambda"
    }

    public override fun extract() {
        tagType()
        tagSourceCode()
        tagFunctionalImplements()
    }

    private fun tagFunctionalImplements() {
        val implementedType: Entity<*>? = getFactory().wrap(element!!.type)
        implementedType!!.parent = this.parent
        getLogger().addTriple(this, Ontology.IMPLEMENTS_PROPERTY, implementedType)
        implementedType.follow()
    }

    public override fun buildRelativeURI(): String {
        return parent!!.getRelativeURI() + SEPARATOR + TAG + SEPARATOR + element?.simpleName
    }

    protected override fun getType(): RDFNode {
        return Ontology.LAMBDA_ENTITY
    }
}