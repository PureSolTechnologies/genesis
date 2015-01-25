package com.puresoltechnologies.genesis.transformation.jdbc;

import java.sql.Connection;

import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;

public abstract class AbstractJDBCTransformationStep implements
		TransformationStep {

	private final AbstractJDBCTransformationSequence sequence;

	public AbstractJDBCTransformationStep(
			AbstractJDBCTransformationSequence sequence) {
		super();
		this.sequence = sequence;
	}

	protected Connection getConnection() {
		return sequence.getConnection();
	}

}
