package com.puresoltechnologies.genesis.tracker.cassandra;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.controller.GenesisController;
import com.puresoltechnologies.genesis.controller.InvalidSequenceException;
import com.puresoltechnologies.genesis.controller.NoTrackerFoundException;
import com.puresoltechnologies.versioning.Version;
import com.puresoltechnologies.versioning.VersionRange;

public class CassandraTransformationTrackerIT {

	/**
	 * Simple big bang test to check whether
	 * {@link CassandraTransformationTracker} can be loaded via SPI.
	 * 
	 * @throws NoTrackerFoundException
	 *             is thrown if no tracker was found.
	 * @throws TransformationException
	 *             if transformation fails.
	 * @throws InvalidSequenceException
	 *             if sequences is invalid.
	 */
	@Test
	public void testSPILoading() throws NoTrackerFoundException,
			TransformationException, InvalidSequenceException {
		try (GenesisController controller = new GenesisController()) {
			controller.transform();
		}
	}

	@Test
	public void testTracking() throws TransformationException,
			UnknownHostException {
		CassandraTransformationTracker tracker = new CassandraTransformationTracker();
		tracker.open();
		try {
			Version startVersion = new Version(1, 0, 0);
			Version targetVersion = new Version(2, 0, 0);
			Version nextVersion = new Version(3, 0, 0);
			VersionRange providedVersionRange = new VersionRange(targetVersion,
					true, nextVersion, false);
			String componentName = "testComponent1";
			SequenceMetadata sequenceMetadata = new SequenceMetadata(
					componentName, startVersion, providedVersionRange);
			String command = "command1";
			TransformationMetadata metadata = new TransformationMetadata(
					sequenceMetadata, "RRL", command, "comment1");

			InetAddress machine = InetAddress.getByName("localhost");
			tracker.trackMigration(machine, metadata);

			assertTrue(tracker.wasMigrated(componentName, machine,
					targetVersion, command));
		} finally {
			tracker.close();
		}
	}
}
