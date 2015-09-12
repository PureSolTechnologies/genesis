package com.puresoltechnologies.genesis.transformation.hadoop;

import java.io.IOException;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;

public class HadoopCreateDirectoryStep extends AbstractHadoopTransformationStep {

    private final TransformationMetadata transformationMetadata;
    private final Path directory;
    private final FsPermission permission;

    public HadoopCreateDirectoryStep(HadoopTransformationSequence sequence, Path directory, String developer,
	    String comment) {
	this(sequence, directory, null, developer, comment);
    }

    public HadoopCreateDirectoryStep(HadoopTransformationSequence sequence, Path directory, FsPermission permission,
	    String developer, String comment) {
	super(sequence);
	this.directory = directory;
	this.permission = permission;
	String command = "MakeDirectory " + directory + " " + (permission == null ? "default permission" : permission);
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
			    "Could not create directory '" + directory + "'. Path exists, but is not a directory.");
		}
	    } else {
		if (permission == null) {
		    fileSystem.mkdirs(directory);
		} else {
		    fileSystem.mkdirs(directory, permission);
		}
	    }
	} catch (IOException e) {
	    throw new TransformationException("Could not create directory '" + directory + "'.", e);
	}
    }

}
