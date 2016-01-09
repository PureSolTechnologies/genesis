package com.puresoltechnologies.genesis.transformation.ductiledb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.puresoltechnologies.ductiledb.api.ElementType;
import com.puresoltechnologies.ductiledb.api.schema.DuctileDBSchemaManager;
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
	schemaManager.defineType(elementType, typeName, propertyKeys);
    }

}
