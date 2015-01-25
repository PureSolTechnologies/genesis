package com.puresoltechnologies.genesis.transformation.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;

public class JDBCSQLTransformationStep extends AbstractJDBCTransformationStep {

	private final TransformationMetadata metadata;

	public JDBCSQLTransformationStep(
			AbstractJDBCTransformationSequence sequence, String developer,
			String sqlCommand, String comment) {
		super(sequence);
		metadata = new TransformationMetadata(sequence.getMetadata(),
				developer, sqlCommand, comment);
	}

	@Override
	public TransformationMetadata getMetadata() {
		return metadata;
	}

	@Override
	public void transform() throws TransformationException {
		Connection connection = getConnection();
		try (Statement statement = connection.createStatement()) {
			statement.execute(metadata.getCommand());
			connection.commit();
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new TransformationException(
						"Could not run SQL command and ROLLBACK ERRORED with message '"
								+ e1.getMessage() + "', too.", e);
			}
			throw new TransformationException("Could not run SQL command.", e);
		}
	}
}
