package com.puresoltechnologies.genesis.transformation.cassandra;

import com.datastax.driver.core.Session;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;

public abstract class AbstractCassandraTransformationStep implements
		TransformationStep {

	private final CassandraTransformationSequence sequence;

	public AbstractCassandraTransformationStep(
			CassandraTransformationSequence sequence) {
		super();
		this.sequence = sequence;
	}

	protected Session getSession() {
		return sequence.getSession();
	}

}
