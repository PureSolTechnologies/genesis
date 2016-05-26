package com.puresoltechnologies.genesis.controller;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.puresoltechnologies.genesis.transformation.spi.ComponentTransformator;
import com.puresoltechnologies.genesis.transformation.spi.TransformationSequence;

public class TestTransformator implements ComponentTransformator {

    private final String componentName;
    private final boolean hostBased;
    private final Set<TransformationSequence> sequences = new LinkedHashSet<>();

    public TestTransformator(String componentName, boolean hostBased) {
	super();
	this.componentName = componentName;
	this.hostBased = hostBased;
    }

    @Override
    public String getComponentName() {
	return componentName;
    }

    @Override
    public Set<String> getDependencies() {
	return Collections.emptySet();
    }

    @Override
    public boolean isHostBased() {
	return hostBased;
    }

    public void addSequence(TransformationSequence sequence) {
	sequences.add(sequence);
    }

    @Override
    public Set<TransformationSequence> getSequences() {
	return sequences;
    }

    @Override
    public String toString() {
	return "ComponentTransformator for '" + componentName + "' (" + (hostBased ? "" : "not ") + "host based)";
    }

    @Override
    public void dropAll() {
	// intentionally left empty
    }
}
