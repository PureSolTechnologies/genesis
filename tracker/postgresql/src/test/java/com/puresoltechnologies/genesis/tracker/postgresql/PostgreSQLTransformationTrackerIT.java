package com.puresoltechnologies.genesis.tracker.postgresql;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import com.puresoltechnologies.genesis.commons.ProvidedVersionRange;
import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.commons.postgresql.PostgreSQLUtils;
import com.puresoltechnologies.genesis.tracker.test.AbstractTransformationTrackerTest;
import com.puresoltechnologies.versioning.Version;

public class PostgreSQLTransformationTrackerIT extends AbstractTransformationTrackerTest {

    private static Properties configuration = new Properties();
    static {
	configuration.setProperty("host", "localhost");
	configuration.setProperty("port", "5432");
	configuration.setProperty("database", "test");
	configuration.setProperty("user", "test");
	configuration.setProperty("password", "test");
    }

    @BeforeClass
    public static void initialieze() throws NumberFormatException, SQLException {
	try (Connection connection = PostgreSQLUtils.connect(configuration)) {
	    try (Statement statement = connection.createStatement()) {
		statement.execute("DROP TABLE GENESIS_CHANGELOG;");
		statement.execute("DROP TABLE GENESIS_LAST_TRANSFORMATIONS;");
		statement.execute("DROP TABLE GENESIS_MIGRATIONLOG;");
	    }
	    connection.commit();
	}
    }

    @Test
    public void testTracking() throws TransformationException, UnknownHostException {
	PostgreSQLTransformationTracker tracker = new PostgreSQLTransformationTracker();

	tracker.open(configuration);
	try {
	    Version startVersion = new Version(1, 0, 0);
	    Version targetVersion = new Version(2, 0, 0);
	    Version nextVersion = new Version(3, 0, 0);
	    String componentName = "testComponent1";
	    ProvidedVersionRange providedVersionRange = new ProvidedVersionRange(targetVersion, nextVersion);
	    SequenceMetadata sequenceMetadata = new SequenceMetadata(componentName, startVersion, providedVersionRange);

	    String command = "command1";
	    TransformationMetadata metadata = new TransformationMetadata(sequenceMetadata, "RRL", command, "comment1");

	    InetAddress machine = InetAddress.getByName("localhost");
	    tracker.trackMigration(machine, metadata);

	    assertTrue(tracker.wasMigrated(componentName, machine, targetVersion, command));

	    providedVersionRange = new ProvidedVersionRange(nextVersion, null);
	    sequenceMetadata = new SequenceMetadata(componentName, targetVersion, providedVersionRange);
	    command = "command2";
	    metadata = new TransformationMetadata(sequenceMetadata, "RRL", command, "comment2");
	    tracker.trackMigration(machine, metadata);

	    assertTrue(tracker.wasMigrated(componentName, machine, nextVersion, command));

	} finally {
	    tracker.close();
	}
    }
}
