package com.puresoltechnologies.genesis.transformation.hadoop;

import org.apache.hadoop.fs.FileSystem;

import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;

public abstract class AbstractHadoopTransformationStep implements TransformationStep {

    private final HadoopTransformationSequence sequence;

    public AbstractHadoopTransformationStep(HadoopTransformationSequence sequence) {
	super();
	this.sequence = sequence;
    }

    protected FileSystem getFileSystem() {
	return sequence.getFileSystem();
    }
}
