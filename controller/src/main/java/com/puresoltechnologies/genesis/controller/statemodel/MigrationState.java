package com.puresoltechnologies.genesis.controller.statemodel;

import java.util.HashSet;
import java.util.Set;

import com.puresoltechnologies.genesis.transformation.spi.TransformationSequence;
import com.puresoltechnologies.graphs.statemodel.State;
import com.puresoltechnologies.versioning.Version;

public class MigrationState implements State<MigrationState, Migration> {

    private final Version version;
    private final Set<Migration> migrations = new HashSet<>();
    private final Set<Migration> edges = new HashSet<>();

    public MigrationState(Version version) {
	super();
	this.version = version;
    }

    @Override
    public Set<Migration> getEdges() {
	return edges;
    }

    @Override
    public String getName() {
	return "Version " + version.toString();
    }

    @Override
    public Set<Migration> getTransitions() {
	return migrations;
    }

    public Version getVersion() {
	return version;
    }

    private void addEdge(Migration migration) {
	if (!edges.add(migration)) {
	    throw new IllegalStateException("Edge '" + migration.toString() + "' was already included. "
		    + "Duplicates are forbidden to avoid ambiguities.");
	}
    }

    public void addMigration(MigrationState targetState, TransformationSequence sequence) {
	Migration migration = new Migration(this, targetState, sequence);
	if (!migrations.add(migration)) {
	    throw new IllegalStateException("Migration '" + migration.toString() + "' was already included. "
		    + "Duplicates are forbidden to avoid ambiguities.");
	}
	targetState.addEdge(migration);
	addEdge(migration);
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((version == null) ? 0 : version.hashCode());
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
	MigrationState other = (MigrationState) obj;
	if (version == null) {
	    if (other.version != null)
		return false;
	} else if (!version.equals(other.version))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return getName();
    }
}
