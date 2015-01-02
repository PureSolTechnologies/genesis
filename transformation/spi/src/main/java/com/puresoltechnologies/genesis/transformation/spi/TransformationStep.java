package com.puresoltechnologies.genesis.transformation.spi;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;

/**
 * This interface is used for an implementation of a single migration step.
 * 
 * @author Rick-Rainer Ludwigs
 */
public interface TransformationStep {

	/**
	 * This method provides the meta information about the transformation step.
	 * This information is used for logging.
	 * 
	 * @return A {@link TransformationMetadata} object is returned.
	 */
	public TransformationMetadata getMetadata();

	/**
	 * This method runs the actual Migration.
	 * 
	 * @param tracker
	 *            is the {@link UniversalMigratorTracker} to be used for
	 *            tracking migration steps.
	 * @param connector
	 *            is the connector to be used for migration connection.
	 * @throws TransformationException
	 *             is thrown if there is something wrong with the migration
	 *             itself.
	 */
	public void transform() throws TransformationException;
}
