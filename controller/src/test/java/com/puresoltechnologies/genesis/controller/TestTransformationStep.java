package com.puresoltechnologies.genesis.controller;

import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;

public class TestTransformationStep implements TransformationStep {

	private final SequenceMetadata sequenceMetadata;
	private final String developer;
	private final String command;
	private final String comment;

	public TestTransformationStep(SequenceMetadata sequenceMetadata,
			String developer, String command, String comment) {
		super();
		this.sequenceMetadata = sequenceMetadata;
		this.developer = developer;
		this.command = command;
		this.comment = comment;
	}

	@Override
	public TransformationMetadata getMetadata() {
		return new TransformationMetadata(sequenceMetadata, developer, command,
				comment);
	}

	@Override
	public void transform() throws TransformationException {
		// intentionally left empty
	}

}
