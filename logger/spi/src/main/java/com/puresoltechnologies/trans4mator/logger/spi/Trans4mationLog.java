package com.puresoltechnologies.trans4mator.logger.spi;

import java.net.InetAddress;
import java.util.Date;

/**
 * This is the interface for the implementation if a log for Trans4mation. For
 * the implementation it is suggested to write not only the transformation log
 * to the transformation data store, but also to Slf4j facade for stdout output.
 * 
 * @author Rick-Rainer Ludwig
 */
public interface Trans4mationLog extends AutoCloseable {

    /**
     * This method is called before the logger is used the first time to
     * initialize itself, to check the environment and to open connection if
     * needed.
     */
    public void open();

    @Override
    public void close();

    /**
     * Logs a piece of information.
     * 
     * @param time
     *            is the time of the logger call. The time comes in UTC and
     *            should be logged that way. It is possible to log the time
     *            zone, too, if needed.
     * @param severity
     *            is the {@link Severity} of the logged event.
     * @param host
     *            is the host where the log call was invoked. The IP address or
     *            name of the host should be logged if present. May be null, if
     *            no host IP address was available.
     * @param thread
     *            is the thread of the log call. The thread's name should be
     *            logged. A stack trace nay be logged, but is not necessary in
     *            most cases.
     * @param clazz
     *            is the logging class. The fully qualified name of the class
     *            should be logged.
     * @param line
     *            is the line number.
     * @param message
     *            is the message to be written.
     * @param cause
     *            is the stack trace to be logged for failure analysis if it is
     *            available. The message of the cause and the stack trace should
     *            be logged.
     */
    public void log(Date time, Severity severity, InetAddress host,
	    Thread thread, Class<?> clazz, int line, String message,
	    Throwable cause);

}
