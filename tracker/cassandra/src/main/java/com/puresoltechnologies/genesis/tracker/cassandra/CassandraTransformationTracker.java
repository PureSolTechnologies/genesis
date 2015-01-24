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
import com.datastax.driver.core.Session;
import com.puresoltechnologies.commons.misc.hash.HashId;
import com.puresoltechnologies.commons.misc.hash.HashUtilities;
import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.commons.cassandra.CassandraUtils;
import com.puresoltechnologies.genesis.commons.cassandra.ReplicationStrategy;
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

	private PreparedStatement preparedInsertStatement = null;
	private PreparedStatement preparedSelectStatement = null;
	private PreparedStatement dropComponentStatement = null;

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
					+ "machine varchar, " + "component varchar, "
					+ "time timestamp, " + "version varchar, "
					+ "developer varchar, " + "command varchar, "
					+ "hashid varchar, " + "comment varchar, "
					+ "PRIMARY KEY(component, machine, version, command));");
			logger.info("ChangeLog table for Cassandra migration created.");
		}
	}

	private void createPreparedStatements(Session session) {
		if (preparedInsertStatement == null) {
			preparedInsertStatement = session
					.prepare("INSERT INTO "
							+ keyspace
							+ "."
							+ CHANGELOG_TABLE
							+ " (time, version, developer, machine, component, command, hashid, comment)"
							+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
		}
		if (preparedSelectStatement == null) {
			preparedSelectStatement = session
					.prepare("SELECT machine, version, component, command FROM "
							+ keyspace
							+ "."
							+ CHANGELOG_TABLE
							+ " WHERE machine=? AND version=?"
							+ " AND component=?" + " AND command=?" + ";");
		}
		if (dropComponentStatement == null) {
			dropComponentStatement = session.prepare("DELETE FROM " + keyspace
					+ "." + CHANGELOG_TABLE + " WHERE component=?" + ";");
		}
	}

	@Override
	public void close() {
		if (session == null) {
			throw new IllegalStateException(
					"Session for migration tracker was not opened.");
		}
		session.close();
		session = null;
		if (cluster == null) {
			throw new IllegalStateException(
					"Cluster for migration tracker was not connected.");
		}
		cluster.close();
		cluster = null;
	}

	@Override
	public void trackMigration(String machine, TransformationMetadata metadata)
			throws TransformationException {
		if (preparedInsertStatement == null) {
			createPreparedStatements(session);
		}
		try {
			HashId hashId = HashUtilities.createHashId(metadata.getCommand());
			BoundStatement boundStatement = preparedInsertStatement.bind(
					new Date(), metadata.getTargetVersion().toString(),
					metadata.getDeveloper(), machine,
					metadata.getComponentName(), metadata.getCommand(),
					hashId.toString(), metadata.getComment());
			session.execute(boundStatement);
		} catch (IOException e) {
			throw new TransformationException(
					"Could not track migration step.", e);
		}
	}

	@Override
	public boolean wasMigrated(String machine, String component,
			Version version, String command) {
		if (preparedSelectStatement == null) {
			createPreparedStatements(session);
		}
		BoundStatement boundStatement = preparedSelectStatement.bind(machine,
				version.toString(), component, command);
		ResultSet result = session.execute(boundStatement);
		return result.iterator().hasNext();
	}

	@Override
	public void dropComponentHistory(String component) {
		if (dropComponentStatement == null) {
			createPreparedStatements(session);
		}
		BoundStatement boundStatement = dropComponentStatement.bind(component);
		session.execute(boundStatement);
	}

	@Override
	public void log(Date time,
			com.puresoltechnologies.genesis.tracker.spi.Severity severity,
			InetAddress machine, Thread thread, String message, Throwable cause) {
		// TODO Auto-generated method stub
	}

	@Override
	public TransformationMetadata getLastTransformationMetadata(String machine,
			String component) {
		// TODO Auto-generated method stub
		return null;
	}
}
