package com.puresoltechnologies.genesis.commons;

import com.puresoltechnologies.versioning.Version;

/**
 * This value class is used to transport meta information about a single
 * transformation step. It is used to transport information to logging, tracking
 * and UI.
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

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((command == null) ? 0 : command.hashCode());
	result = prime * result + ((comment == null) ? 0 : comment.hashCode());
	result = prime * result
		+ ((component == null) ? 0 : component.hashCode());
	result = prime * result
		+ ((developer == null) ? 0 : developer.hashCode());
	result = prime
		* result
		+ ((sequenceMetadata == null) ? 0 : sequenceMetadata.hashCode());
	result = prime * result + ((version == null) ? 0 : version.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	TransformationMetadata other = (TransformationMetadata) obj;
	if (command == null) {
	    if (other.command != null)
		return false;
	} else if (!command.equals(other.command))
	    return false;
	if (comment == null) {
	    if (other.comment != null)
		return false;
	} else if (!comment.equals(other.comment))
	    return false;
	if (component == null) {
	    if (other.component != null)
		return false;
	} else if (!component.equals(other.component))
	    return false;
	if (developer == null) {
	    if (other.developer != null)
		return false;
	} else if (!developer.equals(other.developer))
	    return false;
	if (sequenceMetadata == null) {
	    if (other.sequenceMetadata != null)
		return false;
	} else if (!sequenceMetadata.equals(other.sequenceMetadata))
	    return false;
	if (version == null) {
	    if (other.version != null)
		return false;
	} else if (!version.equals(other.version))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "Transformation " + component + " " + version + ": " + command
		+ "(" + comment + ") by " + developer;
    }
}
