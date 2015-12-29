package com.puresoltechnologies.genesis.transformation.ductiledb;

import com.puresoltechnologies.ductiledb.api.DuctileDBGraph;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;

public abstract class AbstractDuctileDBTransformationStep implements TransformationStep {

    private final DuctileDBTransformationSequence sequence;
    private final TransformationMetadata metadata;

    public AbstractDuctileDBTransformationStep(DuctileDBTransformationSequence sequence, String developer,
	    String command, String comment) {
	super();
	this.sequence = sequence;
	metadata = new TransformationMetadata(sequence.getMetadata(), developer, command, comment);

    }

    protected DuctileDBGraph getDuctileDBGraph() {
	return sequence.getDuctileDBGraph();
    }

    @Override
    public TransformationMetadata getMetadata() {
	return metadata;
    }

}
