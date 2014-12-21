package com.puresoltechnologies.trans4mator.commons;

/**
 * This interface is used for an implementation of a single migration step.
 * 
 * @author Rick-Rainer Ludwigs
 */
public interface Trans4mationStep {

    public Trans4mationMetadata getMetadata();

    /**
     * This method runs the actual Migration.
     * 
     * @param tracker
     *            is the {@link UniversalMigratorTracker} to be used for
     *            tracking migration steps.
     * @param connector
     *            is the connector to be used for migration connection.
     * @throws Trans4mationException
     *             is thrown if there is something wrong with the migration
     *             itself.
     */
    public void transform() throws Trans4mationException;

}
