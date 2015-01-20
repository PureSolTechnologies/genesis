package com.puresoltechnologies.genesis.controller;

import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;

public class TestTransformationStep implements TransformationStep {

    private final SequenceMetadata sequenceMetadata;
    private final String developer;
    private final String component;
    private final String command;
    private final String comment;

    public TestTransformationStep(SequenceMetadata sequenceMetadata,
	    String developer, String component, String command, String comment) {
	super();
	this.sequenceMetadata = sequenceMetadata;
	this.developer = developer;
	this.component = component;
	this.command = command;
	this.comment = comment;
    }

    @Override
    public TransformationMetadata getMetadata() {
	return new TransformationMetadata(sequenceMetadata, sequenceMetadata
		.getProvidedVersionRange().getMinimum(), developer, component,
		command, comment);
    }

    @Override
    public void transform() throws TransformationException {
	// intentionally left empty
    }

}
