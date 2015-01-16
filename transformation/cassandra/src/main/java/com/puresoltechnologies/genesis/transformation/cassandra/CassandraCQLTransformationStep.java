package com.puresoltechnologies.genesis.transformation.cassandra;

import com.datastax.driver.core.Session;
import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.versioning.Version;

public class CassandraCQLTransformationStep extends
	AbstractCassandraTransformationStep {

    private final TransformationMetadata metadata;

    public CassandraCQLTransformationStep(Session session, Version version,
	    String developer, String component, String cqlCommand,
	    String comment) {
	super(session);
	metadata = new TransformationMetadata(version, developer, component,
		cqlCommand, comment);
    }

    @Override
    public TransformationMetadata getMetadata() {
	return metadata;
    }

    @Override
    public void transform() throws TransformationException {
	getSession().execute(metadata.getCommand());
    }

}
