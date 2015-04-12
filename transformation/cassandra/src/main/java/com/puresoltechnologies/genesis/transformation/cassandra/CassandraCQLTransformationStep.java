package com.puresoltechnologies.genesis.transformation.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
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
		PreparedStatement preparedStatement = session.prepare(metadata
				.getCommand());
		preparedStatement.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
		BoundStatement boundStatement = preparedStatement.bind();
		session.execute(boundStatement);
	}
}
