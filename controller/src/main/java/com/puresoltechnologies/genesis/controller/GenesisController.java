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

    public GenesisController() throws TransformationException {
	super();
	Transformators.loadAll();
	tracker = loadTracker();
	tracker.open();
	machine = determineHost();
	Transformators.verifySequences(tracker);
    }

    private TransformationTracker loadTracker() {
	ServiceLoader<TransformationTracker> trackerServices = ServiceLoader
		.load(TransformationTracker.class);
	Iterator<TransformationTracker> iterator = trackerServices.iterator();
	if (!iterator.hasNext()) {
	    throw new IllegalStateException("No tracker for Trans4mator found.");
	}
	TransformationTracker tracker = iterator.next();
	logInfo("Found migration tracker '" + tracker.getClass().getName()
		+ "'.");
	if (iterator.hasNext()) {
	    logError("Found another migration tracker '"
		    + tracker.getClass().getName() + "'!");
	    throw new IllegalStateException(
		    "Multiple trackers for Trans4mator found.");
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

    @Override
    public void close() throws Exception {
	tracker.close();
	Transformators.unloadAll();
    }

    /**
     * 
     * @param targetVersion
     *            is the version to which the transformations shall take place.
     * @throws TransformationException
     *             is thrown in case of transformation issues.
     * @throws InvalidSequenceException
     */
    public void transform(Version targetVersion)
	    throws TransformationException, InvalidSequenceException {
	for (ComponentTransformator transformator : Transformators.getAll()) {
	    MigrationModel model = MigrationModel.create(transformator);
	    runTransformations(transformator, model, targetVersion);
	}
    }

    public void transform() throws TransformationException,
	    InvalidSequenceException {
	for (ComponentTransformator transformator : Transformators.getAll()) {
	    MigrationModel model = MigrationModel.create(transformator);
	    runTransformations(transformator, model, model.getMaximumVersion());
	}
    }

    private void runTransformations(ComponentTransformator transformator,
	    MigrationModel model, Version targetVersion)
	    throws TransformationException {
	TransformationMetadata lastTransformation = tracker
		.getLastTransformationMetadata(machine.getHostAddress(),
			transformator.getComponentName());
	Migration migration = setModelToLastStateAndReturnLastMigration(model,
		lastTransformation);
	while (migration != null) {
	    runSequence(migration.getSequence());
	    model.performTransition(migration);
	    migration = null;
	    Version nextVersion = model.getState().getVersion();
	    for (Migration nextMigration : model.getState().getTransitions()) {
		MigrationState nextTargetState = nextMigration.getTargetState();
		Version nextTargetVersion = nextTargetState.getVersion();
		if (nextVersion.compareTo(nextTargetVersion) < 0) {
		    nextVersion = nextTargetVersion;
		    migration = nextMigration;
		}
	    }
	}
    }

    private Migration setModelToLastStateAndReturnLastMigration(
	    MigrationModel model, TransformationMetadata lastTransformation) {
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
		return migration;
	    }
	}
	return null;
    }

    private void runSequence(TransformationSequence sequence)
	    throws TransformationException {
	for (TransformationStep transformation : sequence.getTransformations()) {
	    TransformationMetadata metadata = transformation.getMetadata();
	    logMigrationStart(metadata);
	    if (!tracker.wasMigrated(machine.getHostAddress(),
		    metadata.getVersion(), metadata.getComponent(),
		    metadata.getCommand())) {
		transformation.transform();
		tracker.trackMigration(machine.getHostAddress(),
			metadata.getVersion(), metadata.getDeveloper(),
			metadata.getComponent(), metadata.getCommand(),
			metadata.getComment());
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
