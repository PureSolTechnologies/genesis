package com.puresoltechnologies.genesis.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.tracker.spi.Severity;
import com.puresoltechnologies.genesis.tracker.spi.TransformationTracker;
import com.puresoltechnologies.genesis.transformation.spi.TransformationSequence;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;

/**
 * This is the central class to start environmental transformations and
 * migrations.
 * 
 * <b>Attention:</b> Due to its nature of a migrator to change states, this
 * {@link Transformator} is <b>not</b> thread-safe and even worse: There should
 * be no multiple instance running at the same time due to global state!
 * 
 * @author Rick-Rainer Ludwig
 */
public class Transformator implements AutoCloseable {

    private static final Logger logger = LoggerFactory
	    .getLogger(Transformator.class);

    private final TransformationTracker tracker;
    private final InetAddress machine;

    public Transformator() throws TransformationException {
	super();
	TransformationSequences.loadAll();
	tracker = loadTracker();
	tracker.open();
	machine = determineHost();
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
	TransformationSequences.unloadAll();
    }

    public void transform() throws TransformationException {
	for (TransformationSequence sequence : TransformationSequences.getAll()) {
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
