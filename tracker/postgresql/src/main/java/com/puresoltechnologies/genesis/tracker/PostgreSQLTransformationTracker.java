package com.puresoltechnologies.genesis.tracker;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.commons.postgresql.PostgreSQLUtils;
import com.puresoltechnologies.genesis.tracker.spi.Severity;
import com.puresoltechnologies.genesis.tracker.spi.TransformationTracker;
import com.puresoltechnologies.versioning.Version;

public class PostgreSQLTransformationTracker implements TransformationTracker {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLTransformationTracker.class);

    private static final String DEFAULT_DATABASE_NAME = "genesis";

    public static final String CHANGELOG_TABLE = "genesis_changelog";
    public static final String MIGRATIONLOG_TABLE = "genesis_migrationlog";
    public static final String LAST_TRANSFORMATIONS_TABLE = "genesis_last_transformations";

    private String database = DEFAULT_DATABASE_NAME;
    private Connection connection;

    private final Properties configuration = new Properties();

    @Override
    public void open(Properties configuration) throws TransformationException {
	this.configuration.putAll(configuration);
	loadAlternateConfigIfPresent();
	connect();
	prepareDatabase();
    }

    private void loadAlternateConfigIfPresent() {
	database = configuration.getProperty("database", DEFAULT_DATABASE_NAME);
    }

    private void connect() throws TransformationException {
	try {
	    connection = PostgreSQLUtils.connect(configuration);
	} catch (SQLException e) {
	    throw new TransformationException("Could not open connection to PostgreSQL.", e);
	}
    }

    private void prepareDatabase() throws TransformationException {
	try (Statement statement = connection.createStatement()) {
	    DatabaseMetaData metaData = connection.getMetaData();
	    try (ResultSet tables = metaData.getTables(database, null, CHANGELOG_TABLE, new String[] { "TABLE" })) {
		if (!tables.next()) {
		    logger.info("ChangeLog table for PostgreSQL migration is missing. Needs to be created...");
		    statement.execute("CREATE TABLE " + CHANGELOG_TABLE + " (" //
			    + "time timestamp, " //
			    + "component varchar, " //
			    + "machine varchar, " //
			    + "version varchar, " //
			    + "command varchar, " //
			    + "developer varchar, " //
			    + "comment varchar, " //
			    + "hashid varchar, " + "PRIMARY KEY(component, machine, version, command));");
		    logger.info("ChangeLog table for PostgreSQL migration created.");
		}
	    }
	    try (ResultSet tables = metaData.getTables(database, null, MIGRATIONLOG_TABLE, new String[] { "TABLE" })) {
		if (!tables.next()) {
		    logger.info("MigrationLog table for PostgreSQL migration is missing. Needs to be created...");
		    statement.execute("CREATE TABLE " + MIGRATIONLOG_TABLE + " (" //
			    + "time timestamp, " //
			    + "severity varchar, " //
			    + "machine varchar, " //
			    + "thread varchar, " //
			    + "message varchar, " //
			    + "exception_type varchar, " //
			    + "exception_message varchar, " //
			    + "stacktrace varchar, " //
			    + "PRIMARY KEY(time, machine, thread, message));");
		    logger.info("MigrationLog table for PostgreSQL migration created.");
		}
	    }
	    try (ResultSet tables = metaData.getTables(database, null, LAST_TRANSFORMATIONS_TABLE,
		    new String[] { "TABLE" })) {
		if (!tables.next()) {
		    logger.info(
			    "LastTransformations table for PostgreSQL migration is missing. Needs to be created...");
		    statement.execute("CREATE TABLE " + LAST_TRANSFORMATIONS_TABLE + " (" //
			    + "time timestamp, " //
			    + "component varchar, " //
			    + "machine varchar, " //
			    + "start_version varchar, " //
			    + "target_version varchar, " //
			    + "next_version varchar, " //
			    + "command varchar, " //
			    + "developer varchar, " //
			    + "comment varchar, " //
			    + "hashid varchar, " //
			    + "PRIMARY KEY(component, machine));");
		    logger.info("LastTransformations table for PostgreSQL migration created.");
		}
	    }
	} catch (SQLException e) {
	    throw new TransformationException("Could not create genesis tables.", e);
	}
    }

    @Override
    public void close() {
	try {
	    connection.close();
	} catch (SQLException e) {
	    logger.warn("Could not cleanly close connection to PostgreSQL.", e);
	}
    }

    @Override
    public void trackMigration(InetAddress machine, TransformationMetadata metadata) throws TransformationException {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean wasMigrated(String component, InetAddress machine, Version version, String command)
	    throws TransformationException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void dropComponentHistory(String component, InetAddress machine) throws TransformationException {
	// TODO Auto-generated method stub

    }

    @Override
    public void log(Instant time, Severity severity, InetAddress host, Thread thread, String message, Throwable cause)
	    throws TransformationException {
	// TODO Auto-generated method stub

    }

    @Override
    public TransformationMetadata getLastTransformationMetadata(String component, InetAddress machine)
	    throws TransformationException {
	// TODO Auto-generated method stub
	return null;
    }

}
