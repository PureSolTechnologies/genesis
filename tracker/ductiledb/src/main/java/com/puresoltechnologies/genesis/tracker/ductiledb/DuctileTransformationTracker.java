package com.puresoltechnologies.genesis.tracker.ductiledb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.commons.misc.hash.HashId;
import com.puresoltechnologies.commons.misc.hash.HashUtilities;
import com.puresoltechnologies.ductiledb.core.DuctileDB;
import com.puresoltechnologies.ductiledb.core.DuctileDBBootstrap;
import com.puresoltechnologies.ductiledb.core.DuctileDBConfiguration;
import com.puresoltechnologies.ductiledb.core.tables.ExecutionException;
import com.puresoltechnologies.ductiledb.core.tables.TableStore;
import com.puresoltechnologies.ductiledb.core.tables.columns.ColumnType;
import com.puresoltechnologies.ductiledb.core.tables.ddl.CreateNamespace;
import com.puresoltechnologies.ductiledb.core.tables.ddl.CreateTable;
import com.puresoltechnologies.ductiledb.core.tables.ddl.DataDefinitionLanguage;
import com.puresoltechnologies.ductiledb.core.tables.dml.BoundStatement;
import com.puresoltechnologies.ductiledb.core.tables.dml.CompareOperator;
import com.puresoltechnologies.ductiledb.core.tables.dml.DataManipulationLanguage;
import com.puresoltechnologies.ductiledb.core.tables.dml.PreparedDelete;
import com.puresoltechnologies.ductiledb.core.tables.dml.PreparedInsert;
import com.puresoltechnologies.ductiledb.core.tables.dml.PreparedSelect;
import com.puresoltechnologies.ductiledb.core.tables.dml.TableRow;
import com.puresoltechnologies.ductiledb.core.tables.dml.TableRowIterable;
import com.puresoltechnologies.genesis.commons.ProvidedVersionRange;
import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.tracker.spi.Severity;
import com.puresoltechnologies.genesis.tracker.spi.TransformationTracker;
import com.puresoltechnologies.versioning.Version;

/**
 * This is the default migration tracker for Purifinity which puts everything
 * into Cassandra.
 * 
 * @author Rick-Rainer Ludwig
 */
public class DuctileTransformationTracker implements TransformationTracker {

    private static final Logger logger = LoggerFactory.getLogger(DuctileTransformationTracker.class);

    public static final String NAMESPACE_NAME = "genesis";

    public static final String CHANGELOG_TABLE = "changelog";
    public static final String MIGRATIONLOG_TABLE = "migrationlog";
    public static final String LAST_TRANSFORMATIONS_TABLE = "last_transformations";

    private PreparedInsert preparedInsertStatement = null;
    private PreparedSelect preparedSelectStatement = null;
    private PreparedDelete preparedDropComponentStatement = null;
    private PreparedInsert preparedLoggingStatement = null;
    private PreparedInsert preparedInsertLastTransformationStatement = null;
    private PreparedSelect preparedSelectLastTransformationStatement = null;
    private PreparedDelete preparedDropComponentLastTransformtaionStatement = null;

    private File configFile = null;
    private DuctileDB ductileDB;

    @Override
    public void open() throws TransformationException {
	loadConfiguration();
	connect();
	prepareKeyspace();
    }

    private void loadConfiguration() {
	URL configuration = getClass().getResource("/genesis.tracker.ductiledb.properties");
	if (configuration != null) {
	    try (InputStream stream = configuration.openStream()) {
		Properties properties = new Properties();
		properties.load(stream);
		configFile = new File(properties.getProperty("genesis.tracker.ductiledb.configfile"));
	    } catch (IOException e) {
		System.err.println(
			"Warning: A configuration file for DuctileDB Tracker was found, but could not be opened.");
	    }
	}
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

    private void prepareKeyspace() throws TransformationException {
	try {
	    com.puresoltechnologies.ductiledb.core.tables.TableStore tableStore = ductileDB.getTableStore();
	    DataDefinitionLanguage dataDefinitionLanguage = tableStore.getDataDefinitionLanguage();
	    if (dataDefinitionLanguage.getNamespace(NAMESPACE_NAME) == null) {
		logger.info("Keyspace for Cassandra migration is missing. Needs to be created...");
		CreateNamespace createNamespace = dataDefinitionLanguage.createCreateNamespace(NAMESPACE_NAME);
		createNamespace.execute();
		if (dataDefinitionLanguage.getNamespace(NAMESPACE_NAME) == null) {
		    throw new TransformationException("Could not create namespace '" + NAMESPACE_NAME + "'.");
		}
		logger.info("Namespace for DuctileDB migration tracker created.");
	    }
	    if (dataDefinitionLanguage.getTable(NAMESPACE_NAME, CHANGELOG_TABLE) == null) {
		logger.info("ChangeLog table for Cassandra migration is missing. Needs to be created...");
		CreateTable createTable = dataDefinitionLanguage.createCreateTable(NAMESPACE_NAME, CHANGELOG_TABLE);
		createTable.addColumn("changelog", "time", ColumnType.DATETIME);
		createTable.addColumn("changelog", "component", ColumnType.VARCHAR);
		createTable.addColumn("changelog", "machine", ColumnType.VARCHAR);
		createTable.addColumn("changelog", "version", ColumnType.VARCHAR);
		createTable.addColumn("changelog", "command", ColumnType.VARCHAR);
		createTable.addColumn("changelog", "developer", ColumnType.VARCHAR);
		createTable.addColumn("changelog", "comment", ColumnType.VARCHAR);
		createTable.addColumn("changelog", "hashid", ColumnType.VARCHAR);
		createTable.setPrimaryKey("component", "machine", "version", "command");
		logger.info("ChangeLog table for Cassandra migration created.");
	    }
	    if (dataDefinitionLanguage.getTable(NAMESPACE_NAME, MIGRATIONLOG_TABLE) == null) {
		logger.info("MigrationLog table for Cassandra migration is missing. Needs to be created...");
		CreateTable table = dataDefinitionLanguage.createCreateTable(NAMESPACE_NAME, MIGRATIONLOG_TABLE);
		table.addColumn("migrationlog", "time", ColumnType.DATETIME);
		table.addColumn("migrationlog", "severity", ColumnType.VARCHAR);
		table.addColumn("migrationlog", "machine", ColumnType.VARCHAR);
		table.addColumn("migrationlog", "thread", ColumnType.VARCHAR);
		table.addColumn("migrationlog", "message", ColumnType.VARCHAR);
		table.addColumn("migrationlog", "exception_type", ColumnType.VARCHAR);
		table.addColumn("migrationlog", "exception_message", ColumnType.VARCHAR);
		table.addColumn("migrationlog", "stacktrace", ColumnType.VARCHAR);
		table.setPrimaryKey("time", "machine", "thread", "message");
		logger.info("MigrationLog table for Cassandra migration created.");
	    }
	    if (dataDefinitionLanguage.getTable(NAMESPACE_NAME, LAST_TRANSFORMATIONS_TABLE) == null) {
		logger.info("LastTransformations table for Cassandra migration is missing. Needs to be created...");
		CreateTable table = dataDefinitionLanguage.createCreateTable(NAMESPACE_NAME,
			LAST_TRANSFORMATIONS_TABLE);
		table.addColumn("transformations", "time", ColumnType.DATETIME);
		table.addColumn("transformations", "component", ColumnType.VARCHAR);
		table.addColumn("transformations", "machine", ColumnType.VARCHAR);
		table.addColumn("transformations", "start_version", ColumnType.VARCHAR);
		table.addColumn("transformations", "target_version", ColumnType.VARCHAR);
		table.addColumn("transformations", "next_version", ColumnType.VARCHAR);
		table.addColumn("transformations", "command", ColumnType.VARCHAR);
		table.addColumn("transformations", "developer", ColumnType.VARCHAR);
		table.addColumn("transformations", "comment", ColumnType.VARCHAR);
		table.addColumn("transformations", "hashid", ColumnType.VARCHAR);
		table.setPrimaryKey("component", "machine");
		logger.info("LastTransformations table for Cassandra migration created.");
	    }
	} catch (ExecutionException e) {
	    throw new TransformationException("Could not prepare namespace.", e);
	}
    }

    private void createPreparedStatements() {
	TableStore tableStore = ductileDB.getTableStore();
	DataManipulationLanguage dml = tableStore.getDataManipulationLanguage();
	if (preparedInsertStatement == null) {
	    preparedInsertStatement = dml.prepareInsert(NAMESPACE_NAME, CHANGELOG_TABLE);
	    preparedInsertStatement.addPlaceholder("changelog", "time", 1);
	    preparedInsertStatement.addPlaceholder("changelog", "component", 2);
	    preparedInsertStatement.addPlaceholder("changelog", "machine", 3);
	    preparedInsertStatement.addPlaceholder("changelog", "version", 4);
	    preparedInsertStatement.addPlaceholder("changelog", "command", 5);
	    preparedInsertStatement.addPlaceholder("changelog", "developer", 6);
	    preparedInsertStatement.addPlaceholder("changelog", "comment", 7);
	    preparedInsertStatement.addPlaceholder("changelog", "hashid", 8);
	}
	if (preparedSelectStatement == null) {
	    preparedSelectStatement = dml.prepareSelect(NAMESPACE_NAME, CHANGELOG_TABLE);
	    preparedSelectStatement.addWherePlaceholder("changelog", "component", CompareOperator.EQUALS, 1);
	    preparedSelectStatement.addWherePlaceholder("changelog", "machine", CompareOperator.EQUALS, 2);
	    preparedSelectStatement.addWherePlaceholder("changelog", "version", CompareOperator.EQUALS, 3);
	    preparedSelectStatement.addWherePlaceholder("changelog", "command", CompareOperator.EQUALS, 4);
	}
	if (preparedDropComponentStatement == null) {
	    preparedDropComponentStatement = dml.prepareDelete(NAMESPACE_NAME, CHANGELOG_TABLE);
	    preparedDropComponentStatement.addWherePlaceholder("changelog", "component", CompareOperator.EQUALS, 1);
	    preparedDropComponentStatement.addWherePlaceholder("changelog", "machine", CompareOperator.EQUALS, 2);
	}
	if (preparedLoggingStatement == null) {
	    preparedLoggingStatement = dml.prepareInsert(NAMESPACE_NAME, MIGRATIONLOG_TABLE);
	    preparedLoggingStatement.addPlaceholder("migrationlog", "time", 1);
	    preparedLoggingStatement.addPlaceholder("migrationlog", "severity", 2);
	    preparedLoggingStatement.addPlaceholder("migrationlog", "machine", 3);
	    preparedLoggingStatement.addPlaceholder("migrationlog", "thread", 4);
	    preparedLoggingStatement.addPlaceholder("migrationlog", "message", 5);
	    preparedLoggingStatement.addPlaceholder("migrationlog", "exception_type", 6);
	    preparedLoggingStatement.addPlaceholder("migrationlog", "exception_message", 7);
	    preparedLoggingStatement.addPlaceholder("migrationlog", "stacktrace", 8);
	}
	if (preparedInsertLastTransformationStatement == null) {
	    preparedInsertLastTransformationStatement = dml.prepareInsert(NAMESPACE_NAME, LAST_TRANSFORMATIONS_TABLE);
	    preparedInsertLastTransformationStatement.addPlaceholder("transformations", "time", 1);
	    preparedInsertLastTransformationStatement.addPlaceholder("transformations", "component", 2);
	    preparedInsertLastTransformationStatement.addPlaceholder("transformations", "machine", 3);
	    preparedInsertLastTransformationStatement.addPlaceholder("transformations", "start_version", 4);
	    preparedInsertLastTransformationStatement.addPlaceholder("transformations", "target_version", 5);
	    preparedInsertLastTransformationStatement.addPlaceholder("transformations", "next_version", 6);
	    preparedInsertLastTransformationStatement.addPlaceholder("transformations", "command", 7);
	    preparedInsertLastTransformationStatement.addPlaceholder("transformations", "developer", 8);
	    preparedInsertLastTransformationStatement.addPlaceholder("transformations", "commend", 9);
	    preparedInsertLastTransformationStatement.addPlaceholder("transformations", "hashid", 10);
	}
	if (preparedSelectLastTransformationStatement == null) {
	    preparedSelectLastTransformationStatement = dml.prepareSelect(NAMESPACE_NAME, LAST_TRANSFORMATIONS_TABLE);
	    preparedSelectLastTransformationStatement.addWherePlaceholder("transformations", "component",
		    CompareOperator.EQUALS, 1);
	    preparedSelectLastTransformationStatement.addWherePlaceholder("transformations", "machine",
		    CompareOperator.EQUALS, 2);
	}
	if (preparedDropComponentLastTransformtaionStatement == null) {
	    preparedDropComponentLastTransformtaionStatement = dml.prepareDelete(NAMESPACE_NAME,
		    LAST_TRANSFORMATIONS_TABLE);
	    preparedDropComponentLastTransformtaionStatement.addWherePlaceholder("transformations", "component",
		    CompareOperator.EQUALS, 1);
	    preparedDropComponentLastTransformtaionStatement.addWherePlaceholder("transformations", "machine",
		    CompareOperator.EQUALS, 2);
	}
    }

    @Override
    public void close() {
	preparedInsertStatement = null;
	preparedSelectStatement = null;
	preparedDropComponentStatement = null;
	preparedLoggingStatement = null;
	preparedInsertLastTransformationStatement = null;
	preparedSelectLastTransformationStatement = null;
	preparedDropComponentLastTransformtaionStatement = null;
	try {
	    ductileDB.close();
	} catch (IOException e) {
	    logger.warn("Could not close DuctileDB cleanly.", e);
	}
    }

    @Override
    public void trackMigration(InetAddress machine, TransformationMetadata metadata) throws TransformationException {
	if ((preparedInsertStatement == null) || (preparedInsertLastTransformationStatement == null)) {
	    createPreparedStatements();
	}
	try {
	    // Tracking...
	    HashId hashId = HashUtilities.createHashId(metadata.getCommand());
	    BoundStatement boundStatement = preparedInsertStatement.bind(new Date(), metadata.getComponentName(),
		    machine.getHostAddress(), metadata.getTargetVersion().toString(), metadata.getCommand(),
		    metadata.getDeveloper(), metadata.getComment(), hashId.toString());
	    boundStatement.execute();
	    // Last Transformations...
	    String nextVersionString = metadata.getNextVersion() != null ? metadata.getNextVersion().toString() : "";
	    boundStatement = preparedInsertLastTransformationStatement.bind(new Date(), metadata.getComponentName(),
		    machine.getHostAddress(), metadata.getStartVersion().toString(),
		    metadata.getTargetVersion().toString(), nextVersionString, metadata.getCommand(),
		    metadata.getDeveloper(), metadata.getComment(), hashId.toString());
	    boundStatement.execute();
	} catch (IOException | ExecutionException e) {
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
	    BoundStatement boundStatement = preparedSelectStatement.bind(component, machine.getHostAddress(),
		    version.toString(), command);
	    TableRowIterable result;
	    result = boundStatement.execute();
	    return result.iterator().hasNext();
	} catch (ExecutionException e) {
	    throw new TransformationException("Could not check whether a migration took place.", e);
	}
    }

    @Override
    public void dropComponentHistory(String component, InetAddress machine) throws TransformationException {
	try {
	    if ((preparedDropComponentStatement == null)
		    || (preparedDropComponentLastTransformtaionStatement == null)) {
		createPreparedStatements();
	    }
	    BoundStatement boundStatement = preparedDropComponentStatement.bind(component, machine.getHostAddress());
	    boundStatement.execute();
	    boundStatement = preparedDropComponentLastTransformtaionStatement.bind(component, machine.getHostAddress());
	    boundStatement.execute();
	} catch (ExecutionException e) {
	    throw new TransformationException("Could not drop component history.", e);
	}
    }

    @Override
    public void log(Date time, Severity severity, InetAddress machine, Thread thread, String message, Throwable cause)
	    throws TransformationException {
	try {
	    if (preparedLoggingStatement == null) {
		createPreparedStatements();
	    }
	    if (cause == null) {
		BoundStatement boundStatement = preparedLoggingStatement.bind(time, severity.name(),
			machine.getHostAddress().toString(), thread.getName(), message, "", "", "");
		boundStatement.execute();
	    } else {
		BoundStatement boundStatement = preparedLoggingStatement.bind(time, severity.name(),
			machine.getHostAddress().toString(), thread.getName(), message, cause.getClass().getName(),
			cause.getMessage(), cause.toString());
		boundStatement.execute();
	    }
	} catch (ExecutionException e) {
	    throw new TransformationException("Could not log migration.", e);
	}
    }

    @Override
    public TransformationMetadata getLastTransformationMetadata(String component, InetAddress machine)
	    throws TransformationException {
	if (preparedSelectLastTransformationStatement == null) {
	    createPreparedStatements();
	}
	BoundStatement boundStatement = preparedSelectLastTransformationStatement.bind(component,
		machine.getHostAddress());
	try (TableRowIterable resultSet = boundStatement.execute()) {
	    TableRow next = resultSet.iterator().next();
	    if (next == null) {
		return null;
	    }
	    Version startVersion = Version.valueOf(next.getString("start_version"));
	    Version targetVersion = Version.valueOf(next.getString("target_version"));
	    String nextVersionString = next.getString("next_version");
	    Version nextVersion;
	    if ((nextVersionString != null) && (!nextVersionString.isEmpty())) {
		nextVersion = Version.valueOf(nextVersionString);
	    } else {
		nextVersion = null;
	    }
	    String developer = next.getString("developer");
	    String command = next.getString("command");
	    String comment = next.getString("comment");
	    SequenceMetadata sequenceMetadata = new SequenceMetadata(component, startVersion,
		    new ProvidedVersionRange(targetVersion, nextVersion));
	    return new TransformationMetadata(sequenceMetadata, developer, command, comment);
	} catch (IOException | ExecutionException e) {
	    throw new TransformationException("Could not retrieve last transformation data.", e);
	}
    }
}
