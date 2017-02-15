package com.puresoltechnologies.genesis.transformation.jdbc;

import java.sql.Connection;

import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.transformation.spi.AbstractTransformationSequence;

public abstract class AbstractJDBCTransformationSequence extends AbstractTransformationSequence {

    public AbstractJDBCTransformationSequence(SequenceMetadata metadata) {
	super(metadata);
    }

    public abstract Connection getConnection();

    @Override
    public String toString() {
	SequenceMetadata metadata = getMetadata();
	return metadata.getComponentName() + " " + metadata.getStartVersion() + " -> "
		+ metadata.getProvidedVersionRange().getMinimum();
    }
}
