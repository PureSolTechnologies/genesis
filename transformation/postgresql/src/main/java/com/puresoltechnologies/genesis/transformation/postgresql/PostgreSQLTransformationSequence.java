package com.puresoltechnologies.genesis.transformation.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.postgresql.PostgreSQLUtils;
import com.puresoltechnologies.genesis.transformation.jdbc.AbstractJDBCTransformationSequence;

public class PostgreSQLTransformationSequence extends AbstractJDBCTransformationSequence {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLTransformationSequence.class);

    private Connection connection;

    public PostgreSQLTransformationSequence(SequenceMetadata metadata) {
	super(metadata);
    }

    @Override
    public Connection getConnection() {
	return connection;
    }

    @Override
    public void open(Properties configuration) throws TransformationException {
	try {
	    connection = PostgreSQLUtils.connect(configuration);
	} catch (SQLException e) {
	    throw new TransformationException("Could not open connection to PostgreSQL.", e);
	}
    }

    @Override
    public void close() {
	try {
	    connection.close();
	} catch (SQLException e) {
	    logger.warn("Could not cleanly close PostgreSQL conection.", e);
	}
    }
}
