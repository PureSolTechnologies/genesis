package com.puresoltechnologies.genesis.transformation.cassandra;

import com.puresoltechnologies.genesis.commons.TransformationMetadata;

public class CassandraCQLTransformationStep extends
		AbstractCassandraTransformationStep {

	private final TransformationMetadata metadata;

	public CassandraCQLTransformationStep(
			CassandraTransformationSequence sequence, String developer,
			String cqlCommand, String comment) {
		super(sequence.getSession());
		metadata = new TransformationMetadata(sequence.getMetadata(),
				developer, cqlCommand, comment);
	}

	@Override
	public TransformationMetadata getMetadata() {
		return metadata;
	}

	@Override
	public void transform() {
		getSession().execute(metadata.getCommand());
	}
}
