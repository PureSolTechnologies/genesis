package com.puresoltechnologies.trans4mator.tracker.spi;

import com.puresoltechnologies.commons.versioning.Version;
import com.puresoltechnologies.trans4mator.commons.Trans4mationException;

/**
 * This interface is used to implement the migration tracker which is used by
 * {@link UniversalMigrator} to log and look up migration steps.
 * 
 * @author Rick-Rainer Ludwig
 */
public interface Trans4mationTracker extends AutoCloseable {

    /**
     * This method is called to open the connection to the tracker.
     * 
     * @throws MigrationException
     *             is thrown in case of an issue.
     */
    public void open() throws Trans4mationException;

    @Override
    public void close();

    /**
     * This write information into the tracker for later look up of a migration
     * step to avoid double appliance.
     * 
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
    public void trackMigration(Version version, String developer,
	    String component, String command, String comment)
	    throws Trans4mationException;

    /**
     * This method checks whether a migration step was performed or not.
     * 
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
    public boolean wasMigrated(Version version, String component, String command);

}
