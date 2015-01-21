package com.puresoltechnologies.genesis.transformation.cassandra;

import com.datastax.driver.core.Session;
import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;

public class CassandraCQLTransformationStep extends
		AbstractCassandraTransformationStep {

	private final TransformationMetadata metadata;

	public CassandraCQLTransformationStep(Session session,
			SequenceMetadata sequenceMetadata, String developer,
			String cqlCommand, String comment) {
		super(session);
		metadata = new TransformationMetadata(sequenceMetadata, developer,
				cqlCommand, comment);
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
