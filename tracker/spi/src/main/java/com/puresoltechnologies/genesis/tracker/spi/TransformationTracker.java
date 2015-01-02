package com.puresoltechnologies.genesis.tracker.spi;

import java.net.InetAddress;
import java.util.Date;

import com.puresoltechnologies.commons.versioning.Version;
import com.puresoltechnologies.genesis.commons.TransformationException;

/**
 * This interface is used to implement the migration tracker which is used by
 * {@link UniversalMigrator} to log and look up migration steps.
 * 
 * @author Rick-Rainer Ludwig
 */
public interface TransformationTracker extends AutoCloseable {

    /**
     * This method is called to open the connection to the tracker.
     * 
     * @throws MigrationException
     *             is thrown in case of an issue.
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
     * @param version
     *            is a {@link Version} object which specifies the version of the
     *            software to which the migration step is assigned to.
     * @param developer
     *            is the name of the developer.
     * @param component
     *            is the name of the component to which the migration step is
     *            assigned.
     * @param command
     *            is the command which was applied or a synonym which is used to
     *            identify the migration procedure.
     * @param comment
     *            is a human readable comment about what the migration step was
     *            used to do.
     * @throws MigrationException
     *             is thrown in case of an issue.
     */
    public void trackMigration(String machine, Version version,
	    String developer, String component, String command, String comment)
	    throws TransformationException;

    /**
     * This method checks whether a migration step was performed or not.
     * 
     * @param machine
     *            is the machine the on which the transformation took place.
     * @param version
     *            is a {@link Version} object which specifies the version to be
     *            looked up.
     * @param component
     *            is the name of the component.
     * @param command
     *            is the command or the synonym to lookup the migration.
     * @return <code>true</code> is returned in case the migration step was
     *         performed. Otherwise <code>false</code> is returned.
     */
    public boolean wasMigrated(String machine, Version version,
	    String component, String command);

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
}
