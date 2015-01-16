package com.puresoltechnologies.genesis.commons;

import com.puresoltechnologies.versioning.Version;

/**
 * This value class is used to transport meta information about a trans4mation.
 * It is used to transport information to logging, tracking and UI.
 * 
 * @author Rick-Rainer Ludwig
 */
public class TransformationMetadata {

    private Version version;
    private String developer;
    private String component;
    private String command;
    private String comment;

    public TransformationMetadata(Version version, String developer,
	    String component, String command, String comment) {
	super();
	this.version = version;
	this.developer = developer;
	this.component = component;
	this.command = command;
	this.comment = comment;
    }

    public Version getVersion() {
	return version;
    }

    public void setVersion(Version version) {
	this.version = version;
    }

    public String getDeveloper() {
	return developer;
    }

    public void setDeveloper(String developer) {
	this.developer = developer;
    }

    public String getComponent() {
	return component;
    }

    public void setComponent(String component) {
	this.component = component;
    }

    public String getCommand() {
	return command;
    }

    public void setCommand(String command) {
	this.command = command;
    }

    public String getComment() {
	return comment;
    }

    public void setComment(String comment) {
	this.comment = comment;
    }

}
