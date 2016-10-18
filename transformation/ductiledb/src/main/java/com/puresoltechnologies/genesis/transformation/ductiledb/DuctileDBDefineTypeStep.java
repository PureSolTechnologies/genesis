package com.puresoltechnologies.genesis.transformation.ductiledb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.puresoltechnologies.ductiledb.core.graph.ElementType;
import com.puresoltechnologies.ductiledb.core.graph.schema.DuctileDBSchemaManager;
import com.puresoltechnologies.genesis.commons.TransformationException;

public class DuctileDBDefineTypeStep extends AbstractDuctileDBTransformationStep {

    private final ElementType elementType;
    private final String typeName;
    private final Set<String> propertyKeys = new HashSet<>();

    public DuctileDBDefineTypeStep(DuctileDBTransformationSequence sequence, String developer, String comment,
	    ElementType elementType, String typeName, String... propertyKeys) {
	this(sequence, developer, comment, elementType, typeName, new HashSet<>(Arrays.asList(propertyKeys)));
    }

    public DuctileDBDefineTypeStep(DuctileDBTransformationSequence sequence, String developer, String comment,
	    ElementType elementType, String typeName, Set<String> propertyKeys) {
	super(sequence, developer,
		"define type '" + typeName + "': element=" + elementType + "; propertyKeys=" + propertyKeys, comment);
	this.elementType = elementType;
	this.typeName = typeName;
	this.propertyKeys.addAll(propertyKeys);
    }

    @Override
    public void transform() throws TransformationException {
	DuctileDBSchemaManager schemaManager = getDuctileDBGraph().createSchemaManager();
	Set<String> typeDefinition = schemaManager.getTypeDefinition(elementType, typeName);
	if (typeDefinition == null) {
	    schemaManager.defineType(elementType, typeName, propertyKeys);
	} else {
	    if (!typeDefinition.equals(propertyKeys)) {
		throw new TransformationException("Type '" + typeName + "' was defined with '" + typeDefinition
			+ "' already, but does not match the requested definition '" + propertyKeys + "'.");
	    }
	}
    }

}
