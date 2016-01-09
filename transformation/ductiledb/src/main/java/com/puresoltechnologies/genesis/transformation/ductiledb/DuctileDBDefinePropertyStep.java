package com.puresoltechnologies.genesis.transformation.ductiledb;

import java.io.Serializable;

import com.puresoltechnologies.ductiledb.api.DuctileDBGraph;
import com.puresoltechnologies.ductiledb.api.ElementType;
import com.puresoltechnologies.ductiledb.api.schema.DuctileDBSchemaManager;
import com.puresoltechnologies.ductiledb.api.schema.PropertyDefinition;
import com.puresoltechnologies.ductiledb.api.schema.UniqueConstraint;
import com.puresoltechnologies.genesis.commons.TransformationException;

public class DuctileDBDefinePropertyStep<T extends Serializable> extends AbstractDuctileDBTransformationStep {

    private final PropertyDefinition<T> definition;

    public DuctileDBDefinePropertyStep(DuctileDBTransformationSequence sequence, String developer, String comment,
	    ElementType elementType, String propertyKey, Class<T> propertyType, UniqueConstraint uniqueConstraint) {
	super(sequence, developer, "define property '" + propertyKey + "': element=" + elementType + "; type="
		+ propertyType.getName() + "; unique=" + uniqueConstraint.name(), comment);
	definition = new PropertyDefinition<>(elementType, propertyKey, propertyType, uniqueConstraint);
    }

    @Override
    public void transform() throws TransformationException {
	DuctileDBGraph graph = getDuctileDBGraph();
	DuctileDBSchemaManager schemaManager = graph.createSchemaManager();
	schemaManager.defineProperty(definition);
    }

}
