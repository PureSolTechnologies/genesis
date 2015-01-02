package com.puresoltechnologies.genesis.commons.cassandra;

import java.util.Date;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

public final class CassandraUtils {

	/**
	 * Timeout for connection establishment in milliseconds.
	 */
	private static final long TIMEOUT = 15000;
	private static final long WAIT_TIME = 1000;

	public static Cluster connectCluster() {
		Builder clusterBuilder = Cluster.builder();
		return clusterBuilder.addContactPoint("localhost").withPort(9042)
				.build();
	}

	public static Session connectWithoutKeyspace(Cluster cluster) {
		return connectKeyspace(cluster, null);
	}

	public static Session connectKeyspace(Cluster cluster, String keyspace) {
		Date start = new Date();
		while (new Date().getTime() - start.getTime() < TIMEOUT) {
			try {
				return connect(cluster, keyspace);
			} catch (NoHostAvailableException e) {
				try {
					Thread.sleep(WAIT_TIME);
				} catch (InterruptedException e1) {
					break;
				}
			}
		}
		return connect(cluster, keyspace);
	}

	private static Session connect(Cluster cluster, String keyspace) {
		if (keyspace != null) {
			return cluster.connect(keyspace);
		} else {
			return cluster.connect();
		}
	}

	/**
	 * Private constructor to avoid instantiation.
	 */
	private CassandraUtils() {
	}

}
