package com.puresoltechnologies.genesis.commons;

import com.puresoltechnologies.versioning.Version;

/**
 * This value class is used to transport meta information about a trans4mation.
 * It is used to transport information to logging, tracking and UI.
 * 
 * @author Rick-Rainer Ludwig
 */
public class TransformationMetadata {

    private final SequenceMetadata sequenceMetadata;
    private final Version version;
    private final String developer;
    private final String component;
    private final String command;
    private final String comment;

    public TransformationMetadata(SequenceMetadata sequenceMetadata,
	    Version version, String developer, String component,
	    String command, String comment) {
	super();
	this.sequenceMetadata = sequenceMetadata;
	this.version = version;
	this.developer = developer;
	this.component = component;
	this.command = command;
	this.comment = comment;
    }

    public SequenceMetadata getSequenceMetadata() {
	return sequenceMetadata;
    }

    public Version getVersion() {
	return version;
    }

    public String getDeveloper() {
	return developer;
    }

    public String getComponent() {
	return component;
    }

    public String getCommand() {
	return command;
    }

    public String getComment() {
	return comment;
    }

}
