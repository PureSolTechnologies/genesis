package com.puresoltechnologies.genesis.transformation.hadoop;

import java.io.IOException;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;

public class HadoopRemoveDirectoryStep extends AbstractHadoopTransformationStep {

    private final TransformationMetadata transformationMetadata;
    private final Path directory;
    private final boolean recursive;

    public HadoopRemoveDirectoryStep(HadoopTransformationSequence sequence, Path directory, boolean recursive,
	    String developer, String comment) {
	super(sequence);
	this.directory = directory;
	this.recursive = recursive;
	String command = "RemoveDirectory " + directory + (recursive ? " recursively" : "");
	transformationMetadata = new TransformationMetadata(sequence.getMetadata(), developer, command, comment);
    }

    @Override
    public TransformationMetadata getMetadata() {
	return transformationMetadata;
    }

    @Override
    public void transform() throws TransformationException {
	try {
	    FileSystem fileSystem = getFileSystem();
	    if (fileSystem.exists(directory)) {
		FileStatus fileStatus = fileSystem.getFileStatus(directory);
		if (!fileStatus.isDirectory()) {
		    throw new TransformationException(
			    "Could not remove directory '" + directory + "'. Path exists, but is not a directory.");
		}
		fileSystem.delete(directory, recursive);
	    }
	} catch (IOException e) {
	    throw new TransformationException("Could not remove directory '" + directory + "'.", e);
	}
    }

}
