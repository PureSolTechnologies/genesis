package com.puresoltechnologies.genesis.commons;

/**
 * This exception is thrown in cases of issues during transformations. This
 * exception is designed to force for a message.
 * 
 * @author Rick-Rainer Ludwig
 */
public class TransformationException extends Exception {

    private static final long serialVersionUID = 6969605288011586706L;

    public TransformationException(String message, Throwable cause) {
	super(message, cause);
    }

    public TransformationException(String message) {
	super(message);
    }

}
