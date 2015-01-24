package com.puresoltechnologies.genesis.transformation.titan;

import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;
import com.thinkaurelius.titan.core.TitanGraph;

public abstract class AbstractTitanTransformationStep implements
	TransformationStep {

    private final TitanTransformationSequence sequence;
    private final TransformationMetadata metadata;

    public AbstractTitanTransformationStep(
	    TitanTransformationSequence sequence, String developer,
	    String comment) {
	super();
	this.sequence = sequence;
	metadata = new TransformationMetadata(sequence.getMetadata(),
		developer, getClass().getName(), comment);

    }

    protected TitanGraph getTitanGraph() {
	return sequence.getTitanGraph();
    }

    @Override
    public TransformationMetadata getMetadata() {
	return metadata;
    }

}
