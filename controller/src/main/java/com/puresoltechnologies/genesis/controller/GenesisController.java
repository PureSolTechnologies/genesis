package com.puresoltechnologies.genesis.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.ServiceLoader;

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
			runTransform();
		}
	}

	public static boolean runTransform() {
		printRunHeader();
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		try (GenesisController controller = new GenesisController()) {
			controller.transform();
			stopWatch.stop();
			printRunFooter(true, stopWatch);
			return true;
		} catch (NoTrackerFoundException | TransformationException
				| InvalidSequenceException e) {
			e.printStackTrace(System.err);
			stopWatch.stop();
			printRunFooter(false, stopWatch);
			return false;
		}
	}

	public static boolean runTransform(Version targetVersion) {
		printRunHeader();
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		try (GenesisController controller = new GenesisController()) {
			controller.transform(targetVersion);
			stopWatch.stop();
			printRunFooter(true, stopWatch);
			return true;
		} catch (NoTrackerFoundException | TransformationException
				| InvalidSequenceException e) {
			e.printStackTrace(System.err);
			stopWatch.stop();
			printRunFooter(false, stopWatch);
			return false;
		}
	}

	public static boolean runDropAll() {
		printRunHeader();
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		try (GenesisController controller = new GenesisController()) {
			controller.dropAll();
			stopWatch.stop();
			printRunFooter(true, stopWatch);
			return true;
		} catch (NoTrackerFoundException | TransformationException e) {
			e.printStackTrace(System.err);
			stopWatch.stop();
			printRunFooter(false, stopWatch);
			return false;
		}
	}

	private static final void printRunHeader() {
		System.out
				.println("==================================================");
		System.out.println("Genesis " + BuildInformation.getVersion());
		System.out
				.println("==================================================");
	}

	private static final void printRunFooter(boolean success,
			StopWatch stopWatch) {
		System.out
				.println("==================================================");
		System.out.println("Genesis: " + (success ? "SUCCESS" : "FAILED"));
		System.out.println("Time:    " + stopWatch.getSeconds() + "s");
		System.out
				.println("==================================================");
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
		ServiceLoader<TransformationTracker> trackerServices = ServiceLoader
				.load(TransformationTracker.class);
		Iterator<TransformationTracker> iterator = trackerServices.iterator();
		if (!iterator.hasNext()) {
			throw new NoTrackerFoundException(
					"No tracker via SPI for service '"
							+ TransformationTracker.class + "' found.");
		}
		TransformationTracker tracker = iterator.next();
		if (iterator.hasNext()) {
			System.err.println("Found another migration tracker '"
					+ tracker.getClass().getName() + "'!");
			throw new NoTrackerFoundException(
					"Multiple trackers via SPI for service '"
							+ TransformationTracker.class + "' found.");
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
		Version currentVersion = lastTransformation.getTargetVersion();
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
		sequence.open();
		try {
			logInfo("Check and run sequence " + sequence);
			for (TransformationStep transformation : sequence
					.getTransformations()) {
				TransformationMetadata metadata = transformation.getMetadata();
				if (!tracker.wasMigrated(machine.getHostAddress(),
						metadata.getComponentName(),
						metadata.getTargetVersion(), metadata.getCommand())) {
					logMigrationStart(metadata);
					transformation.transform();
					tracker.trackMigration(machine.getHostAddress(), metadata);
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
				transformator.dropAll();
			}
		} finally {
			tracker.close();
		}
	}

	private void logMigrationStart(TransformationMetadata metadata) {
		logInfo("\n" + metadata.getComponentName() + " "
				+ metadata.getTargetVersion() + " by "
				+ metadata.getDeveloper() + " (" + metadata.getComment()
				+ "):\n\t" + metadata.getCommand());
	}

	private void logMigrationEnd(TransformationMetadata metadata) {
		logInfo("done.");
	}

	private void logMigrationSkip(TransformationMetadata metadata) {
		logInfo("\n(!)SKIPPED: " + metadata.getComponentName() + " "
				+ metadata.getTargetVersion() + " by "
				+ metadata.getDeveloper() + " (" + metadata.getComment() + ")");
	}

	private void logInfo(String message) {
		log(Severity.INFO, message, null);
		System.out.println(message);
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
