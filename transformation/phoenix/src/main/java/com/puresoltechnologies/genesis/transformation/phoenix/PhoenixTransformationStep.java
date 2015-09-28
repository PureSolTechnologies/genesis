package com.puresoltechnologies.genesis.transformation.phoenix;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;

public class PhoenixTransformationStep implements TransformationStep {

    private static final Logger logger = LoggerFactory.getLogger(PhoenixTransformationStep.class);

    private final PhoenixTransformationSequence sequence;
    private final TransformationMetadata transformationMetadata;
    private final String command;

    public PhoenixTransformationStep(PhoenixTransformationSequence sequence, String developer, String command,
	    String comment) {
	this.sequence = sequence;
	this.command = command;
	transformationMetadata = new TransformationMetadata(sequence.getMetadata(), developer, command, comment);
    }

    @Override
    public TransformationMetadata getMetadata() {
	return transformationMetadata;
    }

    @Override
    public void transform() throws TransformationException {
	Connection connection = sequence.getConnection();
	try (Statement statement = connection.createStatement();) {
	    statement.execute(command);
	    connection.commit();
	} catch (SQLException e) {
	    try {
		connection.rollback();
		throw new TransformationException("Could not execute command '" + command + "'.", e);
	    } catch (SQLException e1) {
		logger.warn("Could not rollback statement.", e1);
	    }
	}
    }

}
