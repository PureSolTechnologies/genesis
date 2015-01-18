package com.puresoltechnologies.genesis.transformation.cassandra;

import com.datastax.driver.core.Session;
import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.commons.cassandra.ReplicationStrategy;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;
import com.puresoltechnologies.versioning.Version;

public class CassandraMigration {

    public static TransformationStep createKeyspace(Session session,
	    SequenceMetadata sequenceMetadata, String component,
	    String keyspace, Version version, String developer, String comment,
	    ReplicationStrategy replicationStrategy, int replicationFactor) {
	String command = "CREATE KEYSPACE " + keyspace + " WITH replication "
		+ "= {'class':'" + replicationStrategy.getStrategyName()
		+ "', 'replication_factor':" + replicationFactor + "};";
	return new CassandraCQLTransformationStep(session, sequenceMetadata,
		version, developer, component, command, comment);
    }

    public static TransformationStep createTable(Session session,
	    SequenceMetadata sequenceMetadata, String component,
	    Version version, String developer, String comment,
	    String creationStatement) {
	return new CassandraCQLTransformationStep(session, sequenceMetadata,
		version, developer, component, creationStatement, comment);
    }

    public static TransformationStep createIndex(Session session,
	    SequenceMetadata sequenceMetadata, String component,
	    Version version, String developer, String comment, String table,
	    String column) {
	String command = "CREATE INDEX idx_" + table + "_" + column + " ON "
		+ table + " (" + column + ");";
	return new CassandraCQLTransformationStep(session, sequenceMetadata,
		version, developer, component, command, comment);
    }

}
