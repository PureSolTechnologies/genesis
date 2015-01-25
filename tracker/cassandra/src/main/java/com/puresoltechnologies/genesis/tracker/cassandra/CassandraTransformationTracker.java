package com.puresoltechnologies.genesis.tracker.cassandra;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.puresoltechnologies.commons.misc.hash.HashId;
import com.puresoltechnologies.commons.misc.hash.HashUtilities;
import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.commons.cassandra.CassandraUtils;
import com.puresoltechnologies.genesis.commons.cassandra.ReplicationStrategy;
import com.puresoltechnologies.genesis.tracker.spi.Severity;
import com.puresoltechnologies.genesis.tracker.spi.TransformationTracker;
import com.puresoltechnologies.versioning.Version;

/**
 * This is the default migration tracker for Purifinity which puts everything
 * into Cassandra.
 * 
 * @author Rick-Rainer Ludwig
 */
public class CassandraTransformationTracker implements TransformationTracker {

	private static final Logger logger = LoggerFactory
			.getLogger(CassandraTransformationTracker.class);

	public static final String DEFAULT_CASSANDRA_HOST_NAME = "localhost";
	public static final int DEFAULT_CASSANDRA_PORT = 9042;
	public static final String DEFAULT_KEYSPACE_NAME = "genesis";
	public static final String CHANGELOG_TABLE = "changelog";
	public static final String MIGRATIONLOG_TABLE = "migrationlog";
	public static final String LAST_TRANSFORMATIONS_TABLE = "last_transformations";

	private PreparedStatement preparedInsertStatement = null;
	private PreparedStatement preparedSelectStatement = null;
	private PreparedStatement preparedDropComponentStatement = null;
	private PreparedStatement preparedLoggingStatement = null;
	private PreparedStatement preparedInsertLastTransformationStatement = null;
	private PreparedStatement preparedSelectLastTransformationStatement = null;
	private PreparedStatement preparedDropComponentLastTransformtaionStatement = null;

	private Cluster cluster = null;
	private Session session = null;

	private String host = DEFAULT_CASSANDRA_HOST_NAME;
	private int port = DEFAULT_CASSANDRA_PORT;
	private String keyspace = DEFAULT_KEYSPACE_NAME;

	@Override
	public void open() throws TransformationException {
		loadAlternateConfigIfPresent();
		connect();
		prepareKeyspace();
	}

	private void loadAlternateConfigIfPresent() {
		URL configuration = getClass().getResource(
				"/genesis.tracker.cassandra.properties");
		if (configuration != null) {
			try (InputStream stream = configuration.openStream()) {
				Properties properties = new Properties();
				properties.load(stream);
				host = properties.getProperty("host",
						DEFAULT_CASSANDRA_HOST_NAME);
				keyspace = properties.getProperty("keyspace",
						DEFAULT_KEYSPACE_NAME);
				String portString = properties.getProperty("port",
						Integer.toString(DEFAULT_CASSANDRA_PORT));
				port = Integer.parseInt(portString);
			} catch (IOException e) {
				System.err
						.println("Warning: A configuration file for Cassandra Tracker was found, but could not be opened.");
			}
		}
	}

	private void connect() {
		if (cluster != null) {
			throw new IllegalStateException(
					"Cluster for migration tracker was already connected.");
		}
		cluster = CassandraUtils.connectCluster(host, port);
		if (session != null) {
			throw new IllegalStateException(
					"Session for migration tracker was already opened.");
		}
		session = CassandraUtils.connectWithoutKeyspace(cluster);
	}

	private void prepareKeyspace() throws TransformationException {
		Metadata clusterMetadata = cluster.getMetadata();
		KeyspaceMetadata keyspaceMetadata = clusterMetadata
				.getKeyspace(keyspace);
		if (keyspaceMetadata == null) {
			logger.info("Keyspace for Cassandra migration is missing. Needs to be created...");
			session.execute("CREATE KEYSPACE " + keyspace
					+ " WITH replication " + "= {'class':'"
					+ ReplicationStrategy.SIMPLE_STRATEGY.getStrategyName()
					+ "', 'replication_factor':3};");
			keyspaceMetadata = clusterMetadata.getKeyspace(keyspace);
			if (keyspaceMetadata == null) {
				throw new TransformationException("Could not create keyspace '"
						+ keyspace + "'.");
			}
			logger.info("Keyspace for Cassandra migration created.");
		}
		if (keyspaceMetadata
				.getTable(CassandraTransformationTracker.CHANGELOG_TABLE) == null) {
			logger.info("ChangeLog table for Cassandra migration is missing. Needs to be created...");
			session.execute("CREATE TABLE " + keyspace + "."
					+ CassandraTransformationTracker.CHANGELOG_TABLE + " ("
					+ "time timestamp, " + "component varchar, "
					+ "machine varchar, " + "version varchar, "
					+ "command varchar, " + "developer varchar, "
					+ "comment varchar, " + "hashid varchar, "
					+ "PRIMARY KEY(component, machine, version, command));");
			logger.info("ChangeLog table for Cassandra migration created.");
		}
		if (keyspaceMetadata
				.getTable(CassandraTransformationTracker.MIGRATIONLOG_TABLE) == null) {
			logger.info("MigrationLog table for Cassandra migration is missing. Needs to be created...");
			session.execute("CREATE TABLE " + keyspace + "."
					+ CassandraTransformationTracker.MIGRATIONLOG_TABLE + " ("
					+ "time timestamp, " + "severity varchar, "
					+ "machine varchar, " + "thread varchar, "
					+ "message varchar, " + "exception_type varchar, "
					+ "exception_message varchar, " + "stacktrace varchar, "
					+ "PRIMARY KEY(time, machine, thread, message));");
			logger.info("MigrationLog table for Cassandra migration created.");
		}
		if (keyspaceMetadata
				.getTable(CassandraTransformationTracker.LAST_TRANSFORMATIONS_TABLE) == null) {
			logger.info("LastTransformations table for Cassandra migration is missing. Needs to be created...");
			session.execute("CREATE TABLE " + keyspace + "."
					+ CassandraTransformationTracker.LAST_TRANSFORMATIONS_TABLE
					+ " (" + "time timestamp, " + "component varchar, "
					+ "machine varchar, " + "version varchar, "
					+ "command varchar, " + "developer varchar, "
					+ "comment varchar, " + "hashid varchar, "
					+ "PRIMARY KEY(component, machine));");
			logger.info("LastTransformations table for Cassandra migration created.");
		}
	}

	private void createPreparedStatements(Session session) {
		if (preparedInsertStatement == null) {
			preparedInsertStatement = session
					.prepare("INSERT INTO "
							+ keyspace
							+ "."
							+ CHANGELOG_TABLE
							+ " (time, component, machine, version, command, developer, comment, hashid)"
							+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
		}
		if (preparedSelectStatement == null) {
			preparedSelectStatement = session.prepare("SELECT * FROM "
					+ keyspace + "." + CHANGELOG_TABLE + " WHERE component=?"
					+ " AND machine=?" + " AND version=?" + " AND command=?"
					+ ";");
		}
		if (preparedDropComponentStatement == null) {
			preparedDropComponentStatement = session.prepare("DELETE FROM "
					+ keyspace + "." + CHANGELOG_TABLE
					+ " WHERE component=? AND machine=?;");
		}
		if (preparedLoggingStatement == null) {
			preparedLoggingStatement = session
					.prepare("INSERT INTO "
							+ keyspace
							+ "."
							+ MIGRATIONLOG_TABLE
							+ " (time, severity, machine, thread, message, exception_type, exception_message, stacktrace)"
							+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
		}
		if (preparedInsertLastTransformationStatement == null) {
			preparedInsertLastTransformationStatement = session
					.prepare("INSERT INTO "
							+ keyspace
							+ "."
							+ LAST_TRANSFORMATIONS_TABLE
							+ " (time, component, machine, version, command, developer, comment, hashid)"
							+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
		}
		if (preparedSelectLastTransformationStatement == null) {
			preparedSelectLastTransformationStatement = session
					.prepare("SELECT * FROM " + keyspace + "."
							+ LAST_TRANSFORMATIONS_TABLE + " WHERE component=?"
							+ " AND machine=?" + ";");
		}
		if (preparedDropComponentLastTransformtaionStatement == null) {
			preparedDropComponentLastTransformtaionStatement = session
					.prepare("DELETE FROM " + keyspace + "."
							+ LAST_TRANSFORMATIONS_TABLE
							+ " WHERE component=? AND machine=?;");
		}
	}

	@Override
	public void close() {
		preparedInsertStatement = null;
		preparedSelectStatement = null;
		preparedDropComponentStatement = null;
		try {
			session.close();
			session = null;
		} finally {
			cluster.close();
			cluster = null;
		}
	}

	@Override
	public void trackMigration(InetAddress machine,
			TransformationMetadata metadata) throws TransformationException {
		if ((preparedInsertStatement == null)
				|| (preparedInsertLastTransformationStatement == null)) {
			createPreparedStatements(session);
		}
		try {
			// Tracking...
			HashId hashId = HashUtilities.createHashId(metadata.getCommand());
			BoundStatement boundStatement = preparedInsertStatement.bind(
					new Date(), metadata.getComponentName(), machine
							.getHostAddress(), metadata.getTargetVersion()
							.toString(), metadata.getCommand(), metadata
							.getDeveloper(), metadata.getComment(), hashId
							.toString());
			session.execute(boundStatement);
			// Last Transformations...
			boundStatement = preparedInsertLastTransformationStatement.bind(
					new Date(), metadata.getComponentName(), machine
							.getHostAddress(), metadata.getTargetVersion()
							.toString(), metadata.getCommand(), metadata
							.getDeveloper(), metadata.getComment(), hashId
							.toString());
			session.execute(boundStatement);
		} catch (IOException e) {
			throw new TransformationException(
					"Could not track migration step.", e);
		}
	}

	@Override
	public boolean wasMigrated(String component, InetAddress machine,
			Version version, String command) {
		if (preparedSelectStatement == null) {
			createPreparedStatements(session);
		}
		BoundStatement boundStatement = preparedSelectStatement.bind(component,
				machine.getHostAddress(), version.toString(), command);
		ResultSet result = session.execute(boundStatement);
		return result.iterator().hasNext();
	}

	@Override
	public void dropComponentHistory(String component, InetAddress machine) {
		if (preparedDropComponentStatement == null) {
			createPreparedStatements(session);
		}
		BoundStatement boundStatement = preparedDropComponentStatement.bind(
				component, machine.getHostAddress());
		session.execute(boundStatement);
	}

	@Override
	public void log(Date time, Severity severity, InetAddress machine,
			Thread thread, String message, Throwable cause) {
		if (preparedLoggingStatement == null) {
			createPreparedStatements(session);
		}
		if (cause == null) {
			BoundStatement boundStatement = preparedLoggingStatement.bind(time,
					severity.name(), machine.getHostAddress().toString(),
					thread.getName(), message, "", "", "");
			session.execute(boundStatement);
		} else {
			BoundStatement boundStatement = preparedLoggingStatement.bind(time,
					severity.name(), machine.getHostAddress().toString(),
					thread.getName(), message, cause.getClass().getName(),
					cause.getMessage(), cause.toString());
			session.execute(boundStatement);
		}
	}

	@Override
	public TransformationMetadata getLastTransformationMetadata(
			String component, InetAddress machine) {
		if (preparedSelectLastTransformationStatement == null) {
			createPreparedStatements(session);
		}
		BoundStatement boundStatement = preparedSelectLastTransformationStatement
				.bind(component, machine.getHostAddress());
		ResultSet resultSet = session.execute(boundStatement);
		Row next = resultSet.iterator().next();
		if (next == null) {
			return null;
		}
		// FIXME
		return null;
	}
}
