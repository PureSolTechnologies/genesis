package com.puresoltechnologies.genesis.transformation.cassandra;

import com.datastax.driver.core.Session;
import com.puresoltechnologies.genesis.commons.cassandra.ReplicationStrategy;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;
import com.puresoltechnologies.versioning.Version;

public class CassandraMigration {

    public static TransformationStep createKeyspace(Session session,
	    String component, String keyspace, Version version,
	    String developer, String comment,
	    ReplicationStrategy replicationStrategy, int replicationFactor) {
	String command = "CREATE KEYSPACE " + keyspace + " WITH replication "
		+ "= {'class':'" + replicationStrategy.getStrategyName()
		+ "', 'replication_factor':" + replicationFactor + "};";
	return new CassandraCQLTransformationStep(session, version, developer,
		component, command, comment);
    }

    public static TransformationStep createTable(Session session,
	    final String component, final Version version,
	    final String developer, final String comment,
	    final String creationStatement) {
	return new CassandraCQLTransformationStep(session, version, developer,
		component, creationStatement, comment);
    }

    public static TransformationStep createIndex(Session session,
	    final String component, final Version version,
	    final String developer, final String comment, final String table,
	    final String column) {
	String command = "CREATE INDEX idx_" + table + "_" + column + " ON "
		+ table + " (" + column + ");";
	return new CassandraCQLTransformationStep(session, version, developer,
		component, command, comment);
    }

}
