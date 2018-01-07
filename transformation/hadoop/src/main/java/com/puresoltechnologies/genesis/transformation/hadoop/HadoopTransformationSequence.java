package com.puresoltechnologies.genesis.transformation.hadoop;

import java.io.IOException;
import java.util.Properties;

import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.commons.hadoop.HadoopClientHelper;
import com.puresoltechnologies.genesis.transformation.spi.AbstractTransformationSequence;

public class HadoopTransformationSequence extends AbstractTransformationSequence {

    private static final Logger logger = LoggerFactory.getLogger(HadoopTransformationSequence.class);

    private FileSystem fileSystem = null;

    public HadoopTransformationSequence(SequenceMetadata metadata) {
	super(metadata);
    }

    public FileSystem getFileSystem() {
	return fileSystem;
    }

    @Override
    public final void open(Properties configuration) {
	try {
	    fileSystem = HadoopClientHelper.connect(configuration);
	} catch (IOException e) {
	    throw new RuntimeException("Could not connect to Hadoop.", e);
	}
    }

    @Override
    public final void close() {
	try {
	    fileSystem.close();
	} catch (IOException e) {
	    logger.warn("Could not cleanly close connection to Haddop.", e);
	} finally {
	    fileSystem = null;
	}
    }
}
