package com.puresoltechnologies.genesis.controller.statemodel;

import com.puresoltechnologies.commons.statemodel.Transition;
import com.puresoltechnologies.commons.versioning.Version;
import com.puresoltechnologies.genesis.transformation.spi.TransformationSequence;

public class Migration implements Transition<MigrationState, Migration> {

    private final Version startVersion;
    private final Version targetVersion;
    private final MigrationState targetState;
    private final TransformationSequence sequence;

    public Migration(Version startVersion, Version targetVersion,
	    MigrationState targetState, TransformationSequence sequence) {
	super();
	this.startVersion = startVersion;
	this.targetVersion = targetVersion;
	this.targetState = targetState;
	this.sequence = sequence;
    }

    @Override
    public String getName() {
	return "Target Version: " + targetVersion.toString();
    }

    @Override
    public MigrationState getTargetState() {
	return targetState;
    }

    public TransformationSequence getSequence() {
	return sequence;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result
		+ ((startVersion == null) ? 0 : startVersion.hashCode());
	result = prime * result
		+ ((targetState == null) ? 0 : targetState.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Migration other = (Migration) obj;
	if (startVersion == null) {
	    if (other.startVersion != null)
		return false;
	} else if (!startVersion.equals(other.startVersion))
	    return false;
	if (targetState == null) {
	    if (other.targetState != null)
		return false;
	} else if (!targetState.equals(other.targetState))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "Migration " + startVersion.toString() + " -> "
		+ targetVersion.toString();
    }
}