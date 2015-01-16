package com.puresoltechnologies.genesis.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
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
     */
    public void transform(Version targetVersion) throws TransformationException {
	for (ComponentTransformator transformator : Transformators.getAll()) {
	    List<TransformationSequence> sequences = calculateNeededSequences(
		    transformator, targetVersion);
	    runTransformations(sequences);
	}
    }

    public void transform() throws TransformationException {
	for (ComponentTransformator transformator : Transformators.getAll()) {
	    Version targetVersion = calculateMaximumVersion(transformator);
	    List<TransformationSequence> sequences = calculateNeededSequences(
		    transformator, targetVersion);
	    runTransformations(sequences);
	}
    }

    private Version calculateMaximumVersion(ComponentTransformator transformator) {
	// FIXME
	return null;
    }

    private void runTransformations(List<TransformationSequence> sequences)
	    throws TransformationException {
	for (TransformationSequence sequence : sequences) {
	    for (TransformationStep transformation : sequence
		    .getTransformations()) {
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
    }

    private List<TransformationSequence> calculateNeededSequences(
	    ComponentTransformator transformator, Version version) {
	Set<TransformationSequence> sequences = transformator.getSequences();
	// FIXME
	return new ArrayList<>(sequences);
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
