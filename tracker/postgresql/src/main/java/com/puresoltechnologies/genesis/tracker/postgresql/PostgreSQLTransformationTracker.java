package com.puresoltechnologies.genesis.tracker.postgresql;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.commons.misc.hash.HashId;
import com.puresoltechnologies.commons.misc.hash.HashUtilities;
import com.puresoltechnologies.genesis.commons.ProvidedVersionRange;
import com.puresoltechnologies.genesis.commons.SequenceMetadata;
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

    private PreparedStatement preparedInsertStatement = null;
    private PreparedStatement preparedSelectStatement = null;
    private PreparedStatement preparedDropComponentStatement = null;
    private PreparedStatement preparedLoggingStatement = null;
    private PreparedStatement preparedInsertLastTransformationStatement = null;
    private PreparedStatement preparedSelectLastTransformationStatement = null;
    private PreparedStatement preparedDropComponentLastTransformtaionStatement = null;

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
	    preparedInsertStatement = null;
	    preparedSelectStatement = null;
	    preparedDropComponentStatement = null;
	    preparedLoggingStatement = null;
	    preparedInsertLastTransformationStatement = null;
	    preparedSelectLastTransformationStatement = null;
	    preparedDropComponentLastTransformtaionStatement = null;
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
	    connection.commit();
	} catch (SQLException e) {
	    try {
		connection.rollback();
	    } catch (SQLException e1) {
		logger.error("Could not rollback transaction.", e);
	    }
	    throw new TransformationException("Could not create genesis tables.", e);
	}
    }

    private void createPreparedStatements() throws SQLException {
	if (preparedInsertStatement == null) {
	    preparedInsertStatement = connection.prepareStatement("INSERT INTO " + CHANGELOG_TABLE
		    + " (time, component, machine, version, command, developer, comment, hashid)"
		    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
	}
	if (preparedSelectStatement == null) {
	    preparedSelectStatement = connection.prepareStatement("SELECT * FROM " + CHANGELOG_TABLE
		    + " WHERE component=?" + " AND machine=?" + " AND version=?" + " AND command=?" + ";");
	}
	if (preparedDropComponentStatement == null) {
	    preparedDropComponentStatement = connection
		    .prepareStatement("DELETE FROM " + CHANGELOG_TABLE + " WHERE component=? AND machine=?;");
	}
	if (preparedLoggingStatement == null) {
	    preparedLoggingStatement = connection.prepareStatement("INSERT INTO " + MIGRATIONLOG_TABLE
		    + " (time, severity, machine, thread, message, exception_type, exception_message, stacktrace)"
		    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
	}
	if (preparedInsertLastTransformationStatement == null) {
	    preparedInsertLastTransformationStatement = connection.prepareStatement("INSERT INTO "
		    + LAST_TRANSFORMATIONS_TABLE
		    + " (time, component, machine, start_version, target_version, next_version, command, developer, comment, hashid)"
		    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)  " + //
		    "ON CONFLICT (component, machine) DO UPDATE SET time=? , start_version=?, target_version=?, next_version=?, command=?, developer=?, comment=?, hashid=?;");
	}
	if (preparedSelectLastTransformationStatement == null) {
	    preparedSelectLastTransformationStatement = connection.prepareStatement(
		    "SELECT * FROM " + LAST_TRANSFORMATIONS_TABLE + " WHERE component=?" + " AND machine=?" + ";");
	}
	if (preparedDropComponentLastTransformtaionStatement == null) {
	    preparedDropComponentLastTransformtaionStatement = connection.prepareStatement(
		    "DELETE FROM " + LAST_TRANSFORMATIONS_TABLE + " WHERE component=? AND machine=?;");
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
	try {
	    if ((preparedInsertStatement == null) || (preparedInsertLastTransformationStatement == null)) {
		createPreparedStatements();
	    }
	    LocalDateTime now = LocalDateTime.now();
	    HashId hashId = HashUtilities.createHashId(metadata.getCommand());
	    synchronized (preparedInsertStatement) {
		// Tracking...
		preparedInsertStatement.setTimestamp(1, Timestamp.valueOf(now));
		preparedInsertStatement.setString(2, metadata.getComponentName());
		preparedInsertStatement.setString(3, machine.getHostAddress());
		preparedInsertStatement.setString(4, metadata.getTargetVersion().toString());
		preparedInsertStatement.setString(5, metadata.getCommand());
		preparedInsertStatement.setString(6, metadata.getDeveloper());
		preparedInsertStatement.setString(7, metadata.getComment());
		preparedInsertStatement.setString(8, hashId.toString());
		preparedInsertStatement.execute();
	    }
	    synchronized (preparedInsertLastTransformationStatement) {
		// Last Transformations...
		String nextVersionString = metadata.getNextVersion() != null ? metadata.getNextVersion().toString()
			: "";
		preparedInsertLastTransformationStatement.setTimestamp(1, Timestamp.valueOf(now));
		preparedInsertLastTransformationStatement.setString(2, metadata.getComponentName());
		preparedInsertLastTransformationStatement.setString(3, machine.getHostAddress());
		preparedInsertLastTransformationStatement.setString(4, metadata.getStartVersion().toString());
		preparedInsertLastTransformationStatement.setString(5, metadata.getTargetVersion().toString());
		preparedInsertLastTransformationStatement.setString(6, nextVersionString);
		preparedInsertLastTransformationStatement.setString(7, metadata.getCommand());
		preparedInsertLastTransformationStatement.setString(8, metadata.getDeveloper());
		preparedInsertLastTransformationStatement.setString(9, metadata.getComment());
		preparedInsertLastTransformationStatement.setString(10, hashId.toString());

		preparedInsertLastTransformationStatement.setTimestamp(11, Timestamp.valueOf(now));
		preparedInsertLastTransformationStatement.setString(12, metadata.getStartVersion().toString());
		preparedInsertLastTransformationStatement.setString(13, metadata.getTargetVersion().toString());
		preparedInsertLastTransformationStatement.setString(14, nextVersionString);
		preparedInsertLastTransformationStatement.setString(15, metadata.getCommand());
		preparedInsertLastTransformationStatement.setString(16, metadata.getDeveloper());
		preparedInsertLastTransformationStatement.setString(17, metadata.getComment());
		preparedInsertLastTransformationStatement.setString(18, hashId.toString());
		preparedInsertLastTransformationStatement.execute();
	    }
	    connection.commit();
	} catch (IOException | SQLException e) {
	    try {
		connection.rollback();
	    } catch (SQLException e1) {
		logger.error("Could not rollback transaction.", e);
	    }
	    throw new TransformationException("Could not track migration step.", e);
	}
    }

    @Override
    public boolean wasMigrated(String component, InetAddress machine, Version version, String command)
	    throws TransformationException {
	try {
	    if (preparedSelectStatement == null) {
		createPreparedStatements();
	    }
	    synchronized (preparedSelectStatement) {
		preparedSelectStatement.setString(1, component);
		preparedSelectStatement.setString(2, machine.getHostAddress());
		preparedSelectStatement.setString(3, version.toString());
		preparedSelectStatement.setString(4, command);
		try (ResultSet resultSet = preparedSelectStatement.executeQuery()) {
		    return resultSet.next();
		}
	    }
	} catch (SQLException e) {
	    throw new TransformationException("Could not track migration step.", e);
	}
    }

    @Override
    public void dropComponentHistory(String component, InetAddress machine) throws TransformationException {
	try {
	    if ((preparedDropComponentStatement == null)
		    || (preparedDropComponentLastTransformtaionStatement == null)) {
		createPreparedStatements();
	    }
	    synchronized (preparedDropComponentStatement) {
		preparedDropComponentStatement.setString(1, component);
		preparedDropComponentStatement.setString(2, machine.getHostAddress());
		preparedDropComponentStatement.execute();
	    }
	    synchronized (preparedDropComponentLastTransformtaionStatement) {
		preparedDropComponentLastTransformtaionStatement.setString(1, component);
		preparedDropComponentLastTransformtaionStatement.setString(2, machine.getHostAddress());
		preparedDropComponentLastTransformtaionStatement.execute();
	    }
	    connection.commit();
	} catch (SQLException e) {
	    try {
		connection.rollback();
	    } catch (SQLException e1) {
		logger.error("Could not rollback transaction.", e);
	    }
	    throw new TransformationException("Could not track migration step.", e);
	}
    }

    @Override
    public void log(Instant time, Severity severity, InetAddress host, Thread thread, String message, Throwable cause)
	    throws TransformationException {
	try {
	    if (preparedLoggingStatement == null) {
		createPreparedStatements();
	    }
	    LocalDateTime timestamp = LocalDateTime.ofInstant(time, ZoneId.systemDefault());
	    synchronized (preparedLoggingStatement) {
		preparedLoggingStatement.setTimestamp(1, Timestamp.valueOf(timestamp));
		preparedLoggingStatement.setString(2, severity.name());
		preparedLoggingStatement.setString(3, host.getHostAddress().toString());
		preparedLoggingStatement.setString(4, thread.getName());
		preparedLoggingStatement.setString(5, message);
		if (cause == null) {
		    preparedLoggingStatement.setString(6, "");
		    preparedLoggingStatement.setString(7, "");
		    preparedLoggingStatement.setString(8, "");
		} else {
		    preparedLoggingStatement.setString(6, cause.getClass().getName());
		    preparedLoggingStatement.setString(7, cause.getMessage());
		    preparedLoggingStatement.setString(8, cause.toString());
		}
		preparedLoggingStatement.execute();
		connection.commit();
	    }
	} catch (SQLException e) {
	    try {
		connection.rollback();
	    } catch (SQLException e1) {
		logger.error("Could not rollback transaction.", e);
	    }
	    throw new TransformationException("Could not track migration step.", e);
	}
    }

    @Override
    public TransformationMetadata getLastTransformationMetadata(String component, InetAddress machine)
	    throws TransformationException {
	try {
	    if (preparedSelectLastTransformationStatement == null) {
		createPreparedStatements();
	    }
	    synchronized (preparedSelectLastTransformationStatement) {
		preparedSelectLastTransformationStatement.setString(1, component);
		preparedSelectLastTransformationStatement.setString(2, machine.getHostAddress());
		try (ResultSet resultSet = preparedSelectLastTransformationStatement.executeQuery()) {
		    if (!resultSet.next()) {
			return null;
		    }
		    Version startVersion = Version.valueOf(resultSet.getString("start_version"));
		    Version targetVersion = Version.valueOf(resultSet.getString("target_version"));
		    String nextVersionString = resultSet.getString("next_version");
		    Version nextVersion;
		    if ((nextVersionString != null) && (!nextVersionString.isEmpty())) {
			nextVersion = Version.valueOf(nextVersionString);
		    } else {
			nextVersion = null;
		    }
		    String developer = resultSet.getString("developer");
		    String command = resultSet.getString("command");
		    String comment = resultSet.getString("comment");
		    SequenceMetadata sequenceMetadata = new SequenceMetadata(component, startVersion,
			    new ProvidedVersionRange(targetVersion, nextVersion));
		    return new TransformationMetadata(sequenceMetadata, developer, command, comment);
		}
	    }
	} catch (SQLException e) {
	    throw new TransformationException("Could not track migration step.", e);
	}
    }

}
