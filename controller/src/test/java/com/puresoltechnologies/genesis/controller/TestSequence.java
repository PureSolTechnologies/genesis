package com.puresoltechnologies.genesis.controller;

import java.util.ArrayList;
import java.util.List;

import com.puresoltechnologies.genesis.commons.ProvidedVersionRange;
import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.transformation.spi.TransformationSequence;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;
import com.puresoltechnologies.versioning.Version;

public class TestSequence implements TransformationSequence {

	private boolean isOpen = false;

	private final List<TransformationStep> transformations = new ArrayList<>();

	private final String componentName;
	private final Version startVersion;
	private final Version targetVersion;
	private final Version finalVersion;

	public TestSequence(String componentName, Version startVersion,
			Version targetVersion, Version finalVersion) {
		super();
		this.componentName = componentName;
		this.startVersion = startVersion;
		this.targetVersion = targetVersion;
		this.finalVersion = finalVersion;
	}

	@Override
	public void open() {
		if (isOpen) {
			throw new IllegalStateException(
					"Tracker shall not be open before opening.");
		}
		isOpen = true;
	}

	@Override
	public void close() {
		if (!isOpen) {
			throw new IllegalStateException(
					"Tracker shall be open before closing.");
		}
		isOpen = false;
	}

	@Override
	public SequenceMetadata getMetadata() {
		return new SequenceMetadata(componentName, startVersion,
				new ProvidedVersionRange(targetVersion, finalVersion));
	}

	public void addTransformation(TransformationStep transformation) {
		transformations.add(transformation);
	}

	@Override
	public List<TransformationStep> getTransformations() {
		return transformations;
	}

	@Override
	public String toString() {
		return "Sequence from " + startVersion + " to " + targetVersion
				+ " for " + componentName;
	}
}
