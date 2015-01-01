package com.puresoltechnologies.trans4mator.trans4mation.cassandra;

import com.datastax.driver.core.Session;
import com.puresoltechnologies.commons.versioning.Version;
import com.puresoltechnologies.trans4mator.commons.cassandra.ReplicationStrategy;
import com.puresoltechnologies.trans4mator.trans4mation.spi.Trans4mationStep;

public class CassandraMigration {

	public static Trans4mationStep createKeyspace(Session session,
			String component, String keyspace, Version version,
			String developer, String comment,
			ReplicationStrategy replicationStrategy, int replicationFactor) {
		String command = "CREATE KEYSPACE " + keyspace + " WITH replication "
				+ "= {'class':'" + replicationStrategy.getStrategyName()
				+ "', 'replication_factor':" + replicationFactor + "};";
		return new CassandraCQLTrans4mationStep(session, version, developer,
				component, command, comment);
	}

	public static Trans4mationStep createTable(Session session,
			final String component, final Version version,
			final String developer, final String comment,
			final String creationStatement) {
		return new CassandraCQLTrans4mationStep(session, version, developer,
				component, creationStatement, comment);
	}

	public static Trans4mationStep createIndex(Session session,
			final String component, final Version version,
			final String developer, final String comment, final String table,
			final String column) {
		String command = "CREATE INDEX idx_" + table + "_" + column + " ON "
				+ table + " (" + column + ");";
		return new CassandraCQLTrans4mationStep(session, version, developer,
				component, command, comment);
	}

}
