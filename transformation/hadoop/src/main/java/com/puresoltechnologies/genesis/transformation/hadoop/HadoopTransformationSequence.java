package com.puresoltechnologies.genesis.transformation.hadoop;

import java.io.IOException;
import java.util.Properties;

import org.apache.hadoop.fs.FileSystem;

import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.commons.hadoop.HadoopClientHelper;
import com.puresoltechnologies.genesis.transformation.spi.AbstractTransformationSequence;

public class HadoopTransformationSequence extends AbstractTransformationSequence {

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
	    System.err.println("Could not cleanly close connection to Haddop.");
	    e.printStackTrace(System.err);
	} finally {
	    fileSystem = null;
	}
    }
}
