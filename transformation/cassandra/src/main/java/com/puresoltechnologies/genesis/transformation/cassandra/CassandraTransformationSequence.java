package com.puresoltechnologies.genesis.transformation.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.transformation.spi.AbstractTransformationSequence;

public class CassandraTransformationSequence extends
		AbstractTransformationSequence {

	private Cluster cluster;
	private Session session;

	private final String host;
	private final int port;
	private final String keyspace;

	public CassandraTransformationSequence(String host, int port,
			SequenceMetadata metadata) {
		super(metadata);
		this.host = host;
		this.port = port;
		this.keyspace = null;
	}

	public CassandraTransformationSequence(String host, int port,
			String keyspace, SequenceMetadata metadata) {
		super(metadata);
		this.host = host;
		this.port = port;
		this.keyspace = keyspace;
	}

	@Override
	public final void open() {
		cluster = Cluster.builder().addContactPoint(host).withPort(port)
				.build();
		if (keyspace == null) {
			session = cluster.connect();
		} else {
			session = cluster.connect(keyspace);
		}
	}

	@Override
	public final void close() {
		try {
			session.close();
		} finally {
			cluster.close();
		}
	}

	public final String getHost() {
		return host;
	}

	public final int getPort() {
		return port;
	}

	public final String getKeyspace() {
		return keyspace;
	}

	public Session getSession() {
		return session;
	}

	@Override
	public String toString() {
		SequenceMetadata metadata = getMetadata();
		return metadata.getComponentName() + " " + metadata.getStartVersion()
				+ " -> " + metadata.getProvidedVersionRange().getMinimum();
	}
}
