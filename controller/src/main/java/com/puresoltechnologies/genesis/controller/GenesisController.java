package com.puresoltechnologies.genesis.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.controller.statemodel.Migration;
import com.puresoltechnologies.genesis.controller.statemodel.MigrationModel;
import com.puresoltechnologies.genesis.controller.statemodel.MigrationState;
import com.puresoltechnologies.genesis.tracker.spi.Severity;
import com.puresoltechnologies.genesis.tracker.spi.TransformationTracker;
import com.puresoltechnologies.genesis.transformation.spi.ComponentTransformator;
import com.puresoltechnologies.genesis.transformation.spi.TransformationSequence;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;
import com.puresoltechnologies.versioning.Version;

/**
 * This is the central class to start environmental transformations and
 * migrations.
 * 
 * <b>Attention:</b> Due to its nature of a migrator to change states, this
 * {@link GenesisController} is <b>not</b> thread-safe and even worse: There
 * should be no multiple instance running at the same time due to global state!
 * 
 * @author Rick-Rainer Ludwig
 */
public class GenesisController implements AutoCloseable {

    private static final Logger logger = LoggerFactory
	    .getLogger(GenesisController.class);

    private final TransformationTracker tracker;
    private final InetAddress machine;

    public GenesisController() {
	super();
	Transformators.loadAll();
	tracker = loadTracker();
	machine = determineHost();
    }

    private TransformationTracker loadTracker() {
	ServiceLoader<TransformationTracker> trackerServices = ServiceLoader
		.load(TransformationTracker.class);
	Iterator<TransformationTracker> iterator = trackerServices.iterator();
	if (!iterator.hasNext()) {
	    throw new IllegalStateException("No tracker found.");
	}
	TransformationTracker tracker = iterator.next();
	logger.info("Found migration tracker '" + tracker.getClass().getName()
		+ "'.");
	if (iterator.hasNext()) {
	    logger.error("Found another migration tracker '"
		    + tracker.getClass().getName() + "'!");
	    throw new IllegalStateException("Multiple trackers found.");
	}
	return tracker;
    }

    private InetAddress determineHost() {
	try {
	    return InetAddress.getLocalHost();
	} catch (UnknownHostException e) {
	    logger.warn("Could not determin host.", e);
	    return null;
	}
    }

    public String getHost() {
	return machine.getHostAddress();
    }

    @Override
    public void close() {
	Transformators.unloadAll();
    }

    /**
     * 
     * @param targetVersion
     *            is the version to which the transformations shall take place.
     * @throws TransformationException
     *             is thrown in case of transformation issues.
     * @throws InvalidSequenceException
     *             is thrown in case the sequences do not provide a valid
     *             migration model.
     */
    public void transform(Version targetVersion)
	    throws TransformationException, InvalidSequenceException {
	tracker.open();
	try {
	    for (ComponentTransformator transformator : Transformators.getAll()) {
		runTransformator(transformator, targetVersion);
	    }
	} finally {
	    tracker.close();
	}
    }

    public void transform() throws TransformationException,
	    InvalidSequenceException {
	tracker.open();
	try {
	    for (ComponentTransformator transformator : Transformators.getAll()) {
		runTransformator(transformator, null);
	    }
	} finally {
	    tracker.close();
	}
    }

    private void runTransformator(ComponentTransformator transformator,
	    Version targetVersion) throws InvalidSequenceException,
	    TransformationException {
	MigrationModel model = MigrationModel.create(transformator);
	if (targetVersion == null) {
	    targetVersion = model.getMaximumVersion();
	}
	runTransformations(transformator.getComponentName(), model,
		targetVersion);
    }

    private void runTransformations(String componentName, MigrationModel model,
	    Version targetVersion) throws TransformationException {
	TransformationMetadata lastTransformation = tracker
		.getLastTransformationMetadata(machine.getHostAddress(),
			componentName);
	setModelToCurrentState(model, lastTransformation);
	Migration migration = findNextMigration(model, targetVersion);
	while (migration != null) {
	    runSequence(migration.getSequence());
	    model.performTransition(migration);
	    migration = findNextMigration(model, targetVersion);
	}
    }

    private Migration findNextMigration(MigrationModel model,
	    Version targetVersion) {
	Migration migration = null;
	MigrationState currentState = model.getState();
	Version nextVersion = currentState.getVersion();
	for (Migration nextMigration : currentState.getTransitions()) {
	    MigrationState nextTargetState = nextMigration.getTargetState();
	    Version nextTargetVersion = nextTargetState.getVersion();
	    if (nextTargetVersion.compareTo(targetVersion) > 0) {
		/*
		 * This migration goes too far and cannot be taken into account.
		 */
		continue;
	    }
	    if (nextVersion.compareTo(nextTargetVersion) < 0) {
		nextVersion = nextTargetVersion;
		migration = nextMigration;
	    }
	}
	return migration;
    }

    private void setModelToCurrentState(MigrationModel model,
	    TransformationMetadata lastTransformation) {
	if (lastTransformation == null) {
	    /*
	     * There was no former transformation. So we keep the current start
	     * state set as current state (default after model creation) .
	     */
	    return;
	}
	Version currentVersion = lastTransformation.getVersion();
	SequenceMetadata lastSequenceMetadata = lastTransformation
		.getSequenceMetadata();
	for (MigrationState state : model.getVertices()) {
	    if (state.getVersion().compareTo(currentVersion) >= 0) {
		/*
		 * We look for a transition which leads to current version. A
		 * state with the same version or a later is of no interest.
		 */
		continue;
	    }
	    for (Migration migration : state.getTransitions()) {
		TransformationSequence sequence = migration.getSequence();
		if (!sequence.getMetadata().equals(lastSequenceMetadata)) {
		    /*
		     * The current version is not in the provided range. So we
		     * can proceed with the next sequence.
		     */
		    continue;
		}
		model.setState(state);
		return;
	    }
	}
	throw new IllegalStateException(
		"There was not state found which fit to the last transformation.");
    }

    private void runSequence(TransformationSequence sequence)
	    throws TransformationException {
	logInfo("Check and run sequence " + sequence);
	for (TransformationStep transformation : sequence.getTransformations()) {
	    TransformationMetadata metadata = transformation.getMetadata();
	    if (!tracker.wasMigrated(machine.getHostAddress(),
		    metadata.getComponent(), metadata.getVersion(),
		    metadata.getCommand())) {
		logMigrationStart(metadata);
		transformation.transform();
		tracker.trackMigration(machine.getHostAddress(),
			metadata.getComponent(), metadata);
	    } else {
		logMigrationSkip(metadata);
	    }
	}
    }

    private void logMigrationStart(TransformationMetadata metadata) {
	logInfo("Start migration by " + metadata.getDeveloper() + ": '"
		+ metadata.getCommand() + "' in component "
		+ metadata.getComponent() + " " + metadata.getVersion()
		+ " (comment: " + metadata.getComment() + ")");
    }

    private void logMigrationSkip(TransformationMetadata metadata) {
	logInfo("Skip migration by " + metadata.getDeveloper() + ": '"
		+ metadata.getCommand() + "' " + metadata.getComponent() + " "
		+ metadata.getVersion());
    }

    private void logInfo(String message) {
	log(Severity.INFO, message, null);
    }

    private void logWarning(String message) {
	log(Severity.WARN, message, null);
    }

    private void logError(String message) {
	log(Severity.ERROR, message, null);
    }

    private void log(Severity severity, String message, Throwable cause) {
	tracker.log(new Date(), severity, machine, Thread.currentThread(),
		message, cause);
    }
}
