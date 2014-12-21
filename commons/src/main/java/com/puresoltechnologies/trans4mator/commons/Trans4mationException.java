package com.puresoltechnologies.trans4mator.commons;

/**
 * This exception is thrown in cases of issues during transformations. This
 * exception is designed to force for a message.
 * 
 * @author Rick-Rainer Ludwig
 */
public class Trans4mationException extends Exception {

    private static final long serialVersionUID = 6969605288011586706L;

    public Trans4mationException(String message, Throwable cause) {
	super(message, cause);
    }

    public Trans4mationException(String message) {
	super(message);
    }

}
