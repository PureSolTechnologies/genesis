package com.puresoltechnologies.genesis.controller.statemodel;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.puresoltechnologies.genesis.controller.InvalidSequenceException;
import com.puresoltechnologies.genesis.transformation.spi.ComponentTransformator;
import com.puresoltechnologies.genesis.transformation.spi.TransformationSequence;
import com.puresoltechnologies.graph.GraphVerfier;
import com.puresoltechnologies.statemodel.AbstractStateModel;
import com.puresoltechnologies.versioning.Version;
import com.puresoltechnologies.versioning.VersionMath;
import com.puresoltechnologies.versioning.VersionRange;

/**
 * This class represents a complete migration model for a certain
 * {@link ComponentTransformator}.
 * 
 * @author Rick-Rainer Ludwig
 */
public class MigrationModel extends
	AbstractStateModel<MigrationState, Migration> {

    /**
     * Use this method to create a new {@link MigrationModel} from a
     * {@link ComponentTransformator}.
     * 
     * @param transformator
     *            is the {@link ComponentTransformator} for which the migration
     *            model is to be created.
     * @return A {@link MigrationModel} is returned containing all sequences as
     *         model.
     * @throws InvalidSequenceException
     *             is thrown in case the transformator provides and
     *             transformation sequences, which are invalid.
     */
    public static MigrationModel create(ComponentTransformator transformator)
	    throws InvalidSequenceException {
	MigrationModel model = new MigrationModel(transformator);
	if (GraphVerfier.hasDeadEnds(model, model.getStartState(),
		model.getEndStates())) {
	    throw new InvalidSequenceException(
		    "There are dead ends in the model which do not allow to migrate to latest version.");
	}
	return model;
    }

    private static final Version VERSION_0_0_0 = new Version(0, 0, 0);
    private static final MigrationState START_STATE = new MigrationState(
	    VERSION_0_0_0);

    /**
     * This map contains a
     */
    private final Map<Version, MigrationState> states = new HashMap<>();
    private final Set<MigrationState> endStates = new HashSet<>();

    private final ComponentTransformator transformator;

    private MigrationModel(ComponentTransformator transformator)
	    throws InvalidSequenceException {
	this.transformator = transformator;
	states.put(VERSION_0_0_0, START_STATE);
	createModel();
    }

    private void createModel() throws InvalidSequenceException {
	Version maxTargetVersion = START_STATE.getVersion();
	endStates.clear();
	for (TransformationSequence sequence : transformator.getSequences()) {
	    Version startVersion = sequence.getStartVersion();
	    Version targetVersion = sequence.getProvidedVersionRange()
		    .getMinimum();
	    maxTargetVersion = VersionMath.max(maxTargetVersion, targetVersion);
	    if (startVersion.compareTo(targetVersion) >= 0) {
		throw new InvalidSequenceException(
			"Sequence with start version "
				+ startVersion.toString()
				+ " and target version "
				+ targetVersion.toString()
				+ " is not allowed. Down migrations are not supported.");
	    }
	    MigrationState startState = states.get(startVersion);
	    if (startState == null) {
		startState = new MigrationState(startVersion);
		states.put(startVersion, startState);
	    }
	    MigrationState targetState = states.get(targetVersion);
	    if (targetState == null) {
		targetState = new MigrationState(targetVersion);
		states.put(targetVersion, targetState);
	    }
	    startState.addMigration(targetState, sequence);
	}
	endStates.add(states.get(maxTargetVersion));
    }

    @Override
    public Set<MigrationState> getVertices() {
	return new HashSet<>(states.values());
    }

    @Override
    public MigrationState getStartState() {
	return START_STATE;
    }

    @Override
    public Set<MigrationState> getEndStates() {
	return endStates;
    }

    public void print(PrintStream stream) {
	MigrationState state = START_STATE;
	print(stream, state, new Vector<Migration>());
    }

    public void print(PrintStream stream, MigrationState state,
	    Vector<Migration> migrations) {
	if (state == null) {
	    return;
	}
	Version version = state.getVersion();
	Version nextVersion = null;
	for (int index = 0; index < migrations.size(); index++) {
	    Migration migration = migrations.get(index);
	    VersionRange providedVersionRange = migration.getSequence()
		    .getProvidedVersionRange();
	    if (providedVersionRange.getMinimum().compareTo(version) > 0) {
		stream.print(". ");
	    } else if (providedVersionRange.getMinimum().compareTo(version) == 0) {
		stream.print("* ");
	    } else if (providedVersionRange.includes(version)) {
		stream.print("| ");
		nextVersion = nextVersion != null ? //
		VersionMath.min(nextVersion, providedVersionRange.getMaximum()) //
			: providedVersionRange.getMaximum();
	    } else if (providedVersionRange.getMaximum().compareTo(version) < 0) {
		stream.print("  ");
	    } else if (providedVersionRange.getMaximum().compareTo(version) == 0) {
		stream.print("- ");
	    }
	}
	for (Migration migration : state.getTransitions()) {
	    VersionRange providedVersionRange = migration.getSequence()
		    .getProvidedVersionRange();
	    nextVersion = nextVersion != null ? //
	    VersionMath.min(nextVersion, providedVersionRange.getMinimum()) //
		    : providedVersionRange.getMinimum();
	    migrations.addElement(migration);
	    stream.print("V ");
	}
	stream.println(version.toString());
	for (int index = 0; index < migrations.size(); index++) {
	    Migration migration = migrations.get(index);
	    VersionRange providedVersionRange = migration.getSequence()
		    .getProvidedVersionRange();
	    if (providedVersionRange.includes(version)) {
		stream.print("| ");
	    } else if (providedVersionRange.getMinimum().compareTo(version) > 0) {
		stream.print(". ");
	    } else {
		stream.print("  ");
	    }
	}
	stream.println();
	print(stream, states.get(nextVersion), migrations);
    }
}
