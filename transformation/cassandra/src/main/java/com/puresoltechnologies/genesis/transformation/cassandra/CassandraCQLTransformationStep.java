package com.puresoltechnologies.genesis.transformation.cassandra;

import com.datastax.driver.core.Session;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;

public class CassandraCQLTransformationStep extends
		AbstractCassandraTransformationStep {

	private final TransformationMetadata metadata;

	public CassandraCQLTransformationStep(
			CassandraTransformationSequence sequence, String developer,
			String cqlCommand, String comment) {
		super(sequence);
		metadata = new TransformationMetadata(sequence.getMetadata(),
				developer, cqlCommand, comment);
	}

	@Override
	public TransformationMetadata getMetadata() {
		return metadata;
	}

	@Override
	public void transform() {
		Session session = getSession();
		session.execute(metadata.getCommand());
	}
}
