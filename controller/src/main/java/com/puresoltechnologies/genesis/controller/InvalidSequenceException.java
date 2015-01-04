package com.puresoltechnologies.genesis.controller;

public class InvalidSequenceException extends Exception {

    private static final long serialVersionUID = 7044250136999450816L;

    public InvalidSequenceException(String message, Throwable cause) {
	super(message, cause);
    }

    public InvalidSequenceException(String message) {
	super(message);
    }

}
