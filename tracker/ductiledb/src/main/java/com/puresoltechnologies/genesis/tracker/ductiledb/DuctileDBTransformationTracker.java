package com.puresoltechnologies.genesis.tracker.ductiledb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.ductiledb.core.DuctileDB;
import com.puresoltechnologies.ductiledb.core.DuctileDBBootstrap;
import com.puresoltechnologies.ductiledb.core.DuctileDBConfiguration;
import com.puresoltechnologies.ductiledb.engine.DatabaseEngine;
import com.puresoltechnologies.ductiledb.engine.Namespace;
import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.tracker.spi.Severity;
import com.puresoltechnologies.genesis.tracker.spi.TransformationTracker;
import com.puresoltechnologies.versioning.Version;

/**
 * This is the default migration tracker for Purifinity which puts everything
 * into DuctileDB.
 * 
 * @author Rick-Rainer Ludwig
 */
public class DuctileDBTransformationTracker implements TransformationTracker {

    private static final Logger logger = LoggerFactory.getLogger(DuctileDBTransformationTracker.class);

    public static final String NAMESPACE_NAME = "genesis";

    public static final String CHANGELOG_TABLE = "changelog";
    public static final String MIGRATIONLOG_TABLE = "migrationlog";
    public static final String LAST_TRANSFORMATIONS_TABLE = "last_transformations";

    private File configFile = null;
    private DuctileDB ductileDB;

    @Override
    public void open(Properties configuration) throws TransformationException {
	loadConfiguration();
	connect();
	try {
	    prepareKeyspace();
	} catch (IOException e) {
	    throw new TransformationException("Could not prepare Keyspace.", e);
	}
    }

    private void loadConfiguration() {
	configFile = new File(System.getProperty("genesis.tracker.ductiledb.config"));
    }

    private void connect() throws TransformationException {
	try (FileInputStream inputStream = new FileInputStream(configFile)) {
	    DuctileDBConfiguration configuration = DuctileDBBootstrap.readConfiguration(inputStream);
	    DuctileDBBootstrap.start(configuration);
	    ductileDB = DuctileDBBootstrap.getInstance();
	} catch (IOException e) {
	    throw new TransformationException("Could not connect to DuctileDB.", e);
	}
    }

    private void prepareKeyspace() throws TransformationException, IOException {
	DatabaseEngine databaseEngine = ductileDB.getBigTableStore();
	Namespace namespace = databaseEngine.getNamespace(NAMESPACE_NAME);
	if (namespace == null) {
	    logger.info("Keyspace for DuctileDB migration is missing. Needs to be created...");
	    namespace = databaseEngine.addNamespace(NAMESPACE_NAME);
	    logger.info("Namespace for DuctileDB migration tracker created.");
	}
	if (!namespace.hasTable(CHANGELOG_TABLE)) {
	    logger.info("ChangeLog table for DuctileDB migration is missing. Needs to be created...");
	    namespace.addTable(CHANGELOG_TABLE, "Contains the changelog of Genesis.");
	    logger.info("ChangeLog table for DuctileDB migration created.");
	}
	if (!namespace.hasTable(MIGRATIONLOG_TABLE)) {
	    logger.info("MigrationLog table for DuctileDB migration is missing. Needs to be created...");
	    namespace.addTable(MIGRATIONLOG_TABLE,
		    "Contains the migration log of Genesis which is a collection of the logs of the steps.");
	    logger.info("MigrationLog table for DuctileDB migration created.");
	}
	if (!namespace.hasTable(LAST_TRANSFORMATIONS_TABLE)) {
	    logger.info("LastTransformations table for DuctileDB migration is missing. Needs to be created...");
	    namespace.addTable(LAST_TRANSFORMATIONS_TABLE, "Contains the last transformation of Genesis.");
	    logger.info("LastTransformations table for DuctileDB migration created.");
	}
    }

    @Override
    public void close() {
	try {
	    ductileDB.close();
	} catch (IOException e) {
	    logger.warn("Could not close DuctileDB cleanly.", e);
	}
    }

    @Override
    public void trackMigration(InetAddress machine, TransformationMetadata metadata) throws TransformationException {
	// try {
	// // Tracking...
	// DatabaseEngine databaseEngine = ductileDB.getBigTableStore();
	// Namespace namespace = databaseEngine.getNamespace(NAMESPACE_NAME);
	//
	// HashId hashId = HashUtilities.createHashId(metadata.getCommand());
	// BigTable migrationTable = namespace.getTable(MIGRATIONLOG_TABLE);
	// BoundStatement boundStatement =
	// preparedInsertStatement.bind(Instant.now(),
	// metadata.getComponentName(),
	// machine.getHostAddress(), metadata.getTargetVersion().toString(),
	// metadata.getCommand(),
	// metadata.getDeveloper(), metadata.getComment(), hashId.toString());
	// boundStatement.execute();
	// // Last Transformations...
	// String nextVersionString = metadata.getNextVersion() != null ?
	// metadata.getNextVersion().toString() : "";
	// boundStatement =
	// preparedInsertLastTransformationStatement.bind(Instant.now(),
	// metadata.getComponentName(),
	// machine.getHostAddress(), metadata.getStartVersion().toString(),
	// metadata.getTargetVersion().toString(), nextVersionString,
	// metadata.getCommand(),
	// metadata.getDeveloper(), metadata.getComment(), hashId.toString());
	// boundStatement.execute();
	// } catch (IOException | ExecutionException e) {
	// throw new TransformationException("Could not track migration step.",
	// e);
	// }
    }

    @Override
    public boolean wasMigrated(String component, InetAddress machine, Version version, String command)
	    throws TransformationException {
	// try {
	// DatabaseEngine databaseEngine = ductileDB.getBigTableStore();
	// Namespace namespace = databaseEngine.getNamespace(NAMESPACE_NAME);
	//
	// BoundStatement boundStatement =
	// preparedSelectStatement.bind(component, machine.getHostAddress(),
	// version.toString(), command);
	// TableRowIterable result;
	// result = boundStatement.execute();
	// return result.iterator().hasNext();
	// } catch (ExecutionException e) {
	// throw new TransformationException("Could not check whether a
	// migration took place.", e);
	// }
	return false;
    }

    @Override
    public void dropComponentHistory(String component, InetAddress machine) throws TransformationException {
	// try {
	// DatabaseEngine databaseEngine = ductileDB.getBigTableStore();
	// Namespace namespace = databaseEngine.getNamespace(NAMESPACE_NAME);
	//
	// BoundStatement boundStatement =
	// preparedDropComponentStatement.bind(component,
	// machine.getHostAddress());
	// boundStatement.execute();
	// boundStatement =
	// preparedDropComponentLastTransformtaionStatement.bind(component,
	// machine.getHostAddress());
	// boundStatement.execute();
	// } catch (ExecutionException e) {
	// throw new TransformationException("Could not drop component
	// history.", e);
	// }
    }

    @Override
    public void log(Instant time, Severity severity, InetAddress machine, Thread thread, String message,
	    Throwable cause) throws TransformationException {
	// try {
	// DatabaseEngine databaseEngine = ductileDB.getBigTableStore();
	// Namespace namespace = databaseEngine.getNamespace(NAMESPACE_NAME);
	//
	// if (cause == null) {
	// BoundStatement boundStatement = preparedLoggingStatement.bind(time,
	// severity.name(),
	// machine.getHostAddress().toString(), thread.getName(), message, "",
	// "", "");
	// boundStatement.execute();
	// } else {
	// BoundStatement boundStatement = preparedLoggingStatement.bind(time,
	// severity.name(),
	// machine.getHostAddress().toString(), thread.getName(), message,
	// cause.getClass().getName(),
	// cause.getMessage(), cause.toString());
	// boundStatement.execute();
	// }
	// } catch (ExecutionException e) {
	// throw new TransformationException("Could not log migration.", e);
	// }
    }

    @Override
    public TransformationMetadata getLastTransformationMetadata(String component, InetAddress machine)
	    throws TransformationException {
	// DatabaseEngine databaseEngine = ductileDB.getBigTableStore();
	// Namespace namespace = databaseEngine.getNamespace(NAMESPACE_NAME);
	//
	// BoundStatement boundStatement =
	// preparedSelectLastTransformationStatement.bind(component,
	// machine.getHostAddress());
	// try (TableRowIterable resultSet = boundStatement.execute()) {
	// TableRow next = resultSet.iterator().next();
	// if (next == null) {
	// return null;
	// }
	// Version startVersion =
	// Version.valueOf(next.getString("start_version"));
	// Version targetVersion =
	// Version.valueOf(next.getString("target_version"));
	// String nextVersionString = next.getString("next_version");
	// Version nextVersion;
	// if ((nextVersionString != null) && (!nextVersionString.isEmpty())) {
	// nextVersion = Version.valueOf(nextVersionString);
	// } else {
	// nextVersion = null;
	// }
	// String developer = next.getString("developer");
	// String command = next.getString("command");
	// String comment = next.getString("comment");
	// SequenceMetadata sequenceMetadata = new SequenceMetadata(component,
	// startVersion,
	// new ProvidedVersionRange(targetVersion, nextVersion));
	// return new TransformationMetadata(sequenceMetadata, developer,
	// command, comment);
	// } catch (IOException | ExecutionException e) {
	// throw new TransformationException("Could not retrieve last
	// transformation data.", e);
	// }
	return null;
    }
}
