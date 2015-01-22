package com.puresoltechnologies.genesis.controller;

/**
 * This exception is thrown in cases not tracker was found.
 * 
 * @author Rick-Rainer Ludwig
 */
public class NoTrackerFoundException extends Exception {

	private static final long serialVersionUID = 5126945992365049285L;

	public NoTrackerFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoTrackerFoundException(String message) {
		super(message);
	}

}
