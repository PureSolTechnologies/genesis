package com.puresoltechnologies.genesis.transformation.ductiledb;

import java.io.Serializable;

import com.puresoltechnologies.ductiledb.core.graph.ElementType;
import com.puresoltechnologies.ductiledb.core.graph.GraphStore;
import com.puresoltechnologies.ductiledb.core.graph.schema.DuctileDBSchemaManager;
import com.puresoltechnologies.ductiledb.core.graph.schema.PropertyDefinition;
import com.puresoltechnologies.ductiledb.core.graph.schema.UniqueConstraint;
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
	GraphStore graph = getDuctileDBGraph();
	DuctileDBSchemaManager schemaManager = graph.createSchemaManager();
	PropertyDefinition<Serializable> definedProperty = schemaManager
		.getPropertyDefinition(definition.getElementType(), definition.getPropertyKey());
	if (definedProperty == null) {
	    schemaManager.defineProperty(definition);
	} else {
	    if (!definedProperty.equals(definition)) {
		throw new TransformationException(
			"Property '" + definition.getPropertyKey() + "' was defined with '" + definedProperty.toString()
				+ "' already, but does not match the requested definition '" + definition + "'.");
	    }
	}
    }

}
