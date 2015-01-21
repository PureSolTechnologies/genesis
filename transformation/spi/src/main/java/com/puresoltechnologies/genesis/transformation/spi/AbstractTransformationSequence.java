package com.puresoltechnologies.genesis.transformation.spi;

import java.util.ArrayList;
import java.util.List;

import com.puresoltechnologies.genesis.commons.SequenceMetadata;

public abstract class AbstractTransformationSequence implements
		TransformationSequence {

	private final List<TransformationStep> transformations = new ArrayList<>();

	private final SequenceMetadata metadata;

	public AbstractTransformationSequence(SequenceMetadata metadata) {
		super();
		this.metadata = metadata;
	}

	@Override
	public final SequenceMetadata getMetadata() {
		return metadata;
	}

	@Override
	public final List<TransformationStep> getTransformations() {
		return transformations;
	}

	public void appendTransformation(TransformationStep transformation) {
		transformations.add(transformation);
	}
}
