package com.puresoltechnologies.genesis.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.commons.misc.StopWatch;
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

    private static final Logger logger = LoggerFactory.getLogger(GenesisController.class);

    private static boolean migrate = false;
    private static boolean drop = false;

    private static void showUsage() {
	System.out.println("Usage: <DDL.jar> (--drop | --migrate)");
    }

    public static void main(String[] args) throws Exception {
	if (args.length == 0) {
	    showUsage();
	    return;
	}
	for (String arg : args) {
	    if ("--drop".equals(arg)) {
		drop = true;
	    } else if ("--migrate".equals(arg)) {
		migrate = true;
	    }
	}
	if (drop) {
	    runDropAll();
	}
	if (migrate) {
	    migrate();
	}
    }

    /**
     * This method runs the transformation to the last version-
     * 
     * @return <code>true</code> is returned in case of a successful migration.
     *         <code>false</code> is returned otherwise.
     */
    public static boolean migrate() {
	return migrate(null);
    }

    /**
     * Runs the migration to a specified version.
     * 
     * @param targetVersion
     *            is the version to which the migration is to run to.
     * @return <code>true</code> is returned in case of a successful migration.
     *         <code>false</code> is returned otherwise.
     */
    public static boolean migrate(Version targetVersion) {
	printRunHeader("MIGRATE");
	StopWatch stopWatch = new StopWatch();
	stopWatch.start();
	boolean success = false;
	try (GenesisController controller = new GenesisController()) {
	    controller.transform(targetVersion);
	    success = true;
	} catch (NoTrackerFoundException | TransformationException | InvalidSequenceException e) {
	    e.printStackTrace(System.err);
	}
	stopWatch.stop();
	printRunFooter(success, stopWatch);
	return success;
    }

    public static boolean runDropAll() {
	printRunHeader("DROP");
	StopWatch stopWatch = new StopWatch();
	stopWatch.start();
	boolean success = false;
	try (GenesisController controller = new GenesisController()) {
	    controller.dropAll();
	    success = true;
	} catch (NoTrackerFoundException | TransformationException e) {
	    e.printStackTrace(System.err);
	}
	stopWatch.stop();
	printRunFooter(success, stopWatch);
	return success;
    }

    private static final void printRunHeader(String command) {
	System.out.println("==================================================");
	System.out.println("Genesis " + BuildInformation.getVersion());
	System.out.println("==================================================");
	logger.info("==> Genesis " + BuildInformation.getVersion() + " started: " + command + " requested. <==");
    }

    private static final void printRunFooter(boolean success, StopWatch stopWatch) {
	String stateString = success ? "SUCCESS" : "FAILED";
	logger.info("==> Genesis " + BuildInformation.getVersion() + " finished with " + stateString + " <==");
	System.out.println("==================================================");
	System.out.println("Genesis: " + stateString);
	System.out.println("Time:    " + stopWatch.getSeconds() + "s");
	System.out.println("==================================================");
    }

    private final TransformationTracker tracker;
    private final InetAddress machine;

    public GenesisController() throws NoTrackerFoundException {
	super();
	Transformators.loadAll();
	tracker = loadTracker();
	machine = determineHost();
    }

    private TransformationTracker loadTracker() throws NoTrackerFoundException {
	ServiceLoader<TransformationTracker> trackerServices = ServiceLoader.load(TransformationTracker.class);
	Iterator<TransformationTracker> iterator = trackerServices.iterator();
	if (!iterator.hasNext()) {
	    throw new NoTrackerFoundException(
		    "No tracker via SPI for service '" + TransformationTracker.class + "' found.");
	}
	TransformationTracker tracker = iterator.next();
	if (iterator.hasNext()) {
	    System.err.println("Found another migration tracker '" + tracker.getClass().getName() + "'!");
	    throw new NoTrackerFoundException(
		    "Multiple trackers via SPI for service '" + TransformationTracker.class + "' found.");
	}
	return tracker;
    }

    private InetAddress determineHost() {
	try {
	    return InetAddress.getLocalHost();
	} catch (UnknownHostException e) {
	    throw new IllegalStateException("Could not determin hostname.", e);
	}
    }

    public InetAddress getHost() {
	return machine;
    }

    @Override
    public void close() {
	Transformators.unloadAll();
    }

    public void transform() throws TransformationException, InvalidSequenceException {
	transform(null);
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
    public void transform(Version targetVersion) throws TransformationException, InvalidSequenceException {
	tracker.open();
	try {
	    List<ComponentTransformator> allTransformators = new ArrayList<>(Transformators.getAll());
	    sortTransformatorsByDependencies(allTransformators);
	    logInfo("The following component transformators will be run in order:");
	    for (int i = 0; i < allTransformators.size(); ++i) {
		ComponentTransformator transformator = allTransformators.get(i);
		logInfo(MessageFormat.format("{0}) " + transformator.getComponentName(), i + 1));
	    }
	    for (ComponentTransformator transformator : allTransformators) {
		runTransformator(transformator, targetVersion);
	    }
	} finally {
	    tracker.close();
	}
    }

    static void sortTransformatorsByDependencies(List<ComponentTransformator> allTransformators) {
	Set<String> usedDependencies = new HashSet<>();
	List<ComponentTransformator> sortedTransformators = new ArrayList<>();
	while (allTransformators.size() > 0) {
	    int lastLength = sortedTransformators.size();
	    Iterator<ComponentTransformator> transformatorIterator = allTransformators.iterator();
	    while (transformatorIterator.hasNext()) {
		ComponentTransformator transformator = transformatorIterator.next();
		if (usedDependencies.containsAll(transformator.getDependencies())) {
		    usedDependencies.add(transformator.getComponentName());
		    sortedTransformators.add(transformator);
		    transformatorIterator.remove();
		    break;
		}
	    }
	    if (lastLength == sortedTransformators.size()) {
		throw new IllegalStateException("Dependencies for some transformators cannot be satisfied.");
	    }
	}
	allTransformators.clear();
	allTransformators.addAll(sortedTransformators);
    }

    private void runTransformator(ComponentTransformator transformator, Version targetVersion)
	    throws InvalidSequenceException, TransformationException {
	logInfo("Starting transformator for component '" + transformator.getComponentName() + "'...");
	MigrationModel model = MigrationModel.create(transformator);
	if (targetVersion == null) {
	    targetVersion = model.getMaximumVersion();
	}
	logInfo("Target version: " + targetVersion);
	runTransformations(transformator.getComponentName(), model, targetVersion);
	logInfo("Transformator for component '" + transformator.getComponentName() + "' stopped.");
    }

    private void runTransformations(String componentName, MigrationModel model, Version targetVersion)
	    throws TransformationException {
	TransformationMetadata lastTransformation = tracker.getLastTransformationMetadata(componentName, machine);
	setModelToCurrentState(model, lastTransformation);
	Migration migration = findNextMigration(model, targetVersion);
	while (migration != null) {
	    runSequence(migration.getSequence());
	    model.performTransition(migration);
	    migration = findNextMigration(model, targetVersion);
	}
    }

    private Migration findNextMigration(MigrationModel model, Version targetVersion) {
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

    private void setModelToCurrentState(MigrationModel model, TransformationMetadata lastTransformation) {
	if (lastTransformation == null) {
	    /*
	     * There was no former transformation. So we keep the current start
	     * state set as current state (default after model creation) .
	     */
	    return;
	}
	Version currentVersion = lastTransformation.getTargetVersion();
	SequenceMetadata lastSequenceMetadata = lastTransformation.getSequenceMetadata();
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
		if ((sequence.getMetadata().getStartVersion().equals(lastSequenceMetadata.getStartVersion()))
			&& (sequence.getMetadata().getProvidedVersionRange().getTargetVersion()
				.equals(lastSequenceMetadata.getProvidedVersionRange().getTargetVersion()))) {
		    model.setState(state);
		    return;
		}
		/*
		 * The current version is not in the provided range. So we can
		 * proceed with the next sequence.
		 */
	    }
	}
	throw new IllegalStateException("There was not state found which fit to the last transformation.");
    }

    private void runSequence(TransformationSequence sequence) throws TransformationException {
	sequence.open();
	try {
	    logInfo("Check and run sequence " + sequence);
	    for (TransformationStep transformation : sequence.getTransformations()) {
		TransformationMetadata metadata = transformation.getMetadata();
		if (!tracker.wasMigrated(metadata.getComponentName(), machine, metadata.getTargetVersion(),
			metadata.getCommand())) {
		    logMigrationStart(metadata);
		    transformation.transform();
		    tracker.trackMigration(machine, metadata);
		    logMigrationEnd(metadata);
		} else {
		    logMigrationSkip(metadata);
		}
	    }
	} finally {
	    sequence.close();
	}
    }

    public void dropAll() throws TransformationException {
	tracker.open();
	try {
	    for (ComponentTransformator transformator : Transformators.getAll()) {
		logger.info(
			"Dropping component '" + transformator.getComponentName() + "' and its history from Genesis.");
		transformator.dropAll();
		tracker.dropComponentHistory(transformator.getComponentName(), machine);
		logger.info("done.");
	    }
	} finally {
	    tracker.close();
	}
    }

    private void logMigrationStart(TransformationMetadata metadata) {
	logInfo(metadata.getComponentName() + " " + metadata.getTargetVersion() + " by " + metadata.getDeveloper()
		+ " (" + metadata.getComment() + "):\n\t" + metadata.getCommand());
    }

    private void logMigrationEnd(TransformationMetadata metadata) {
	logInfo("done.");
    }

    private void logMigrationSkip(TransformationMetadata metadata) {
	logInfo("(!) SKIPPED: " + metadata.getComponentName() + " " + metadata.getTargetVersion() + " by "
		+ metadata.getDeveloper() + " (" + metadata.getComment() + ")");
    }

    private void logInfo(String message) {
	log(Severity.INFO, message, null);
	logger.info(message);
    }

    private void logWarning(String message) {
	log(Severity.WARN, message, null);
	logger.warn(message);
    }

    private void logError(String message) {
	log(Severity.ERROR, message, null);
	logger.error(message);
    }

    private void log(Severity severity, String message, Throwable cause) {
	try {
	    tracker.log(Instant.now(), severity, machine, Thread.currentThread(), message, cause);
	} catch (TransformationException e) {
	    logger.error("Could not log.", e);
	}
    }
}
