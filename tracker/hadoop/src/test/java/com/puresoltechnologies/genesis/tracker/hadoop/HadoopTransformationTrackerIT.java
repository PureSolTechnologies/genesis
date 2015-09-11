package com.puresoltechnologies.genesis.tracker.hadoop;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import com.puresoltechnologies.genesis.commons.ProvidedVersionRange;
import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.tracker.test.AbstractTransformationTrackerTest;
import com.puresoltechnologies.versioning.Version;

public class HadoopTransformationTrackerIT extends AbstractTransformationTrackerTest {

    @Test
    public void testTracking() throws TransformationException, UnknownHostException {
	HadoopTransformationTracker tracker = new HadoopTransformationTracker();
	tracker.open();
	try {
	    Version startVersion = new Version(1, 0, 0);
	    Version targetVersion = new Version(2, 0, 0);
	    Version nextVersion = new Version(3, 0, 0);
	    ProvidedVersionRange providedVersionRange = new ProvidedVersionRange(targetVersion, nextVersion);
	    String componentName = "testComponent1";
	    SequenceMetadata sequenceMetadata = new SequenceMetadata(componentName, startVersion, providedVersionRange);
	    String command = "command1";
	    TransformationMetadata metadata = new TransformationMetadata(sequenceMetadata, "RRL", command, "comment1");

	    InetAddress machine = InetAddress.getByName("localhost");
	    tracker.trackMigration(machine, metadata);

	    assertTrue(tracker.wasMigrated(componentName, machine, targetVersion, command));
	} finally {
	    tracker.close();
	}
    }
}
