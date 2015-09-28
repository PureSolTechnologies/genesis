package com.puresoltechnologies.genesis.transformation.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.transformation.spi.AbstractTransformationSequence;

public class PhoenixTransformationSequence extends AbstractTransformationSequence {

    private static final Logger logger = LoggerFactory.getLogger(PhoenixTransformationSequence.class);

    private Connection connection;
    private final String host;

    public PhoenixTransformationSequence(SequenceMetadata metadata, String host) {
	super(metadata);
	this.host = host;
    }

    public Connection getConnection() {
	return connection;
    }

    @Override
    public final void open() {
	try {
	    connection = DriverManager.getConnection("jdbc:phoenix:" + host);
	} catch (SQLException e) {
	    throw new RuntimeException("Could not connect to database.", e);
	}
    }

    @Override
    public final void close() {
	try {
	    connection.close();
	} catch (SQLException e) {
	    logger.warn("Could not cleanly close connection to Phoenix.", e);
	} finally {
	    connection = null;
	}
    }

}
