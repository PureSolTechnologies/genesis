package com.puresoltechnologies.genesis.transformation.cassandra;

import com.datastax.driver.core.Session;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;

public abstract class AbstractCassandraTransformationStep implements
	TransformationStep {

    private final Session session;

    public AbstractCassandraTransformationStep(Session session) {
	super();
	this.session = session;
    }

    protected Session getSession() {
	return session;
    }

}
