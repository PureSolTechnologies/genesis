package com.puresoltechnologies.genesis.transformation.cassandra;

import java.util.Properties;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.transformation.spi.AbstractTransformationSequence;

public class CassandraTransformationSequence extends AbstractTransformationSequence {

    private Cluster cluster;
    private Session session;

    private final String host;
    private final int port;
    private final String keyspace;

    public CassandraTransformationSequence(String host, int port, SequenceMetadata metadata) {
	super(metadata);
	this.host = host;
	this.port = port;
	this.keyspace = null;
    }

    public CassandraTransformationSequence(String host, int port, String keyspace, SequenceMetadata metadata) {
	super(metadata);
	this.host = host;
	this.port = port;
	this.keyspace = keyspace;
    }

    @Override
    public final void open(Properties configuration) {
	cluster = Cluster.builder().addContactPoint(host).withPort(port).build();
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
	    session = null;
	} finally {
	    cluster.close();
	    cluster = null;
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
}
