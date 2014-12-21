package com.puresoltechnologies.trans4mator.trans4mation.cassandra;

import com.datastax.driver.core.Session;
import com.puresoltechnologies.trans4mator.commons.Trans4mationStep;

public abstract class AbstractCassandraTrans4mationStep implements
		Trans4mationStep {

	private final Session session;

	public AbstractCassandraTrans4mationStep(Session session) {
		super();
		this.session = session;
	}

	protected Session getSession() {
		return session;
	}

}
