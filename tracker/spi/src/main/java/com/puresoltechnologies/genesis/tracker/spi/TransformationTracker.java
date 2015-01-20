package com.puresoltechnologies.genesis.tracker.spi;

import java.net.InetAddress;
import java.util.Date;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.versioning.Version;

/**
 * This interface is used to implement the migration tracker which is used by
 * GenesisController to log and look up migration steps.
 * 
 * @author Rick-Rainer Ludwig
 */
public interface TransformationTracker extends AutoCloseable {

    /**
     * This method is called to open the connection to the tracker. The open
     * method also performs all checks and preparations of the store.
     * 
     * @throws TransformationException
     *             in case the data store could not be opened and prepared.
     */
    public void open() throws TransformationException;

    @Override
    public void close();

    /**
     * This write information into the tracker for later look up of a migration
     * step to avoid double appliance.
     * 
     * @param machine
     *            is the machine the on which the transformation took place.
     * @param component
     *            is the name of the component to which the migration step is
     *            assigned.
     * @param metadata
     *            is a {@link TransformationMetadata} object to be stored as
     *            run.
     * @throws TransformationException
     *             is thrown in case of an issue during the actual migration
     *             process.
     */
    public void trackMigration(String machine, String component,
	    TransformationMetadata metadata) throws TransformationException;

    /**
     * This method checks whether a migration step was performed or not. It
     * should be safe here to check for the machine, the component, the target
     * version and the command.
     * 
     * @param machine
     *            is the machine the on which the transformation took place.
     * @param component
     *            is the name of the component.
     * @param version
     *            is the {@link Version} for which the transformation step was
     *            run.
     * @param command
     *            is the command to check for execution.
     * @return <code>true</code> is returned in case the migration step was
     *         performed. Otherwise <code>false</code> is returned.
     */
    public boolean wasMigrated(String machine, String component,
	    Version version, String command);

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
     * @param message
     *            is the message to be written.
     * @param cause
     *            is the stack trace to be logged for failure analysis if it is
     *            available. The message of the cause and the stack trace should
     *            be logged.
     */
    public void log(Date time, Severity severity, InetAddress host,
	    Thread thread, String message, Throwable cause);

    /**
     * This method looks into the tracker storage to identify the last
     * transformation which was run. This is used to identify the starting point
     * for the next migration steps.
     * 
     * @param machine
     *            is the name of the machine to look up for the last sequence
     *            which was run there for the component defined later.
     * @param component
     *            is the name of the component to be checked for the last run
     *            sequence.
     * @return A {@link TransformationMetadata} object is returned containing
     *         the last transformation meta data. The result may be
     *         <code>null</code> here if not data is available at all.
     */
    public TransformationMetadata getLastTransformationMetadata(String machine,
	    String component);

}
