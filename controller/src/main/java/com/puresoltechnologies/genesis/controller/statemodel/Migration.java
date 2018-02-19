package com.puresoltechnologies.genesis.controller.statemodel;

import com.puresoltechnologies.genesis.transformation.spi.TransformationSequence;
import com.puresoltechnologies.graphs.graph.Pair;
import com.puresoltechnologies.graphs.statemodel.Transition;

public class Migration implements Transition<MigrationState, Migration> {

    private final MigrationState startState;
    private final MigrationState targetState;
    private final TransformationSequence sequence;

    public Migration(MigrationState startState, MigrationState targetState, TransformationSequence sequence) {
	super();
	this.startState = startState;
	this.targetState = targetState;
	this.sequence = sequence;
    }

    @Override
    public Pair<MigrationState> getVertices() {
	return new Pair<MigrationState>(startState, targetState);
    }

    @Override
    public String getName() {
	return "Target Version: " + targetState.getVersion();
    }

    @Override
    public MigrationState getTargetState() {
	return targetState;
    }

    public TransformationSequence getSequence() {
	return sequence;
    }

    @Override
    public String toString() {
	return "Migration " + startState.getVersion() + " -> " + targetState.getVersion();
    }
}