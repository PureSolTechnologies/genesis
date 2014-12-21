package com.puresoltechnologies.trans4mator.controller;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.puresoltechnologies.trans4mator.commons.Trans4mationException;
import com.puresoltechnologies.trans4mator.commons.Trans4mationMetadata;
import com.puresoltechnologies.trans4mator.commons.Trans4mationStep;
import com.puresoltechnologies.trans4mator.logger.api.Trans4mationLogger;
import com.puresoltechnologies.trans4mator.logger.api.Trans4mationLogs;
import com.puresoltechnologies.trans4mator.tracker.spi.Trans4mationTracker;
import com.puresoltechnologies.trans4mator.trans4mation.spi.Trans4mationSequence;

/**
 * This is the central class to start environmental transformations and
 * migrations.
 * 
 * <b>Attention:</b> Due to its nature of a migrator to change states, this
 * {@link Trans4mator} is <b>not</b> thread-safe and even worse: There should be
 * no multiple instance running at the same time!
 * 
 * @author Rick-Rainer Ludwig
 */
public class Trans4mator implements AutoCloseable {

    private static final Trans4mationLogger logger = Trans4mationLogger
	    .getLogger(Trans4mator.class);

    private final Trans4mationTracker tracker;

    public Trans4mator() throws Trans4mationException {
	super();
	Trans4mationLogs.loadAndOpenAll();
	Trans4mationSequences.loadAll();
	tracker = loadTracker();
	tracker.open();
    }

    private Trans4mationTracker loadTracker() {
	ServiceLoader<Trans4mationTracker> trackerServices = ServiceLoader
		.load(Trans4mationTracker.class);
	Iterator<Trans4mationTracker> iterator = trackerServices.iterator();
	if (!iterator.hasNext()) {
	    throw new IllegalStateException("No tracker for Trans4mator found.");
	}
	Trans4mationTracker tracker = iterator.next();
	logger.logInfo("Found migration tracker '"
		+ tracker.getClass().getName() + "'.");
	if (iterator.hasNext()) {
	    logger.logError("Found another migration tracker '"
		    + tracker.getClass().getName() + "'!");
	    throw new IllegalStateException(
		    "Multiple trackers for Trans4mator found.");
	}
	return tracker;
    }

    @Override
    public void close() throws Exception {
	tracker.close();
	Trans4mationSequences.unloadAll();
	Trans4mationLogs.closeAndUnloadAll();
    }

    public void transform() throws Trans4mationException {
	for (Trans4mationSequence sequence : Trans4mationSequences.getAll()) {
	    for (Trans4mationStep transformation : sequence
		    .getTransformations()) {
		Trans4mationMetadata metadata = transformation.getMetadata();
		logMigrationStart(metadata);
		if (!tracker.wasMigrated(metadata.getVersion(),
			metadata.getComponent(), metadata.getCommand())) {
		    transformation.transform();
		    tracker.trackMigration(metadata.getVersion(),
			    metadata.getDeveloper(), metadata.getComponent(),
			    metadata.getCommand(), metadata.getComment());
		} else {
		    logMigrationSkip(metadata);
		}

	    }
	}
    }

    private void logMigrationStart(Trans4mationMetadata metadata) {
	logger.logInfo("Start migration by " + metadata.getDeveloper() + ": '"
		+ metadata.getCommand() + "' in component "
		+ metadata.getComponent() + " " + metadata.getVersion()
		+ " (comment: " + metadata.getComment() + ")");
    }

    private void logMigrationSkip(Trans4mationMetadata metadata) {
	logger.logInfo("Skip migration by " + metadata.getDeveloper() + ": '"
		+ metadata.getCommand() + "' " + metadata.getComponent() + " "
		+ metadata.getVersion());
    }
}
