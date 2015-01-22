package com.puresoltechnologies.genesis.transformation.cassandra;

import com.puresoltechnologies.genesis.commons.cassandra.ReplicationStrategy;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;

public class CassandraStandardMigrations {

	public static TransformationStep createKeyspace(
			CassandraTransformationSequence sequence, String keyspace,
			String developer, String comment,
			ReplicationStrategy replicationStrategy, int replicationFactor) {
		String command = "CREATE KEYSPACE " + keyspace + " WITH replication "
				+ "= {'class':'" + replicationStrategy.getStrategyName()
				+ "', 'replication_factor':" + replicationFactor + "};";
		return new CassandraCQLTransformationStep(sequence, developer, command,
				comment);
	}

	public static TransformationStep createIndex(
			CassandraTransformationSequence sequence, String developer,
			String comment, String table, String column) {
		String command = "CREATE INDEX idx_" + table + "_" + column + " ON "
				+ table + " (" + column + ");";
		return new CassandraCQLTransformationStep(sequence, developer, command,
				comment);
	}

}
