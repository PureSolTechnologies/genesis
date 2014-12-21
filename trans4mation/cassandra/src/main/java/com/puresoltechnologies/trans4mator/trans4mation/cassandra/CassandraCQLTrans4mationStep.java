package com.puresoltechnologies.trans4mator.trans4mation.cassandra;

import com.datastax.driver.core.Session;
import com.puresoltechnologies.commons.versioning.Version;
import com.puresoltechnologies.trans4mator.commons.Trans4mationException;
import com.puresoltechnologies.trans4mator.commons.Trans4mationMetadata;

public class CassandraCQLTrans4mationStep extends
		AbstractCassandraTrans4mationStep {

	private final Trans4mationMetadata metadata;

	public CassandraCQLTrans4mationStep(Session session, Version version,
			String developer, String component, String cqlCommand,
			String comment) {
		super(session);
		metadata = new Trans4mationMetadata(version, developer, component,
				cqlCommand, comment);
	}

	@Override
	public Trans4mationMetadata getMetadata() {
		return metadata;
	}

	@Override
	public void transform() throws Trans4mationException {
		getSession().execute(metadata.getCommand());
	}

}
