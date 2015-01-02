package com.puresoltechnologies.genesis.transformation.spi;

import java.util.Set;

/**
 * This is class provides a single transformator for a single target. This
 * transform
 * 
 * @author Rick-Rainer Ludwig
 *
 */
public interface TargetTransformator {

	/**
	 * This method returns the name of the transformation target. This target
	 * name is used to distinguish different sequences from each other.
	 */
	public String getTargetName();

	/**
	 * This method returns the sequences which are bound to this target.
	 * 
	 * @return A {@link Set} of {@link TransformationSequence} is returned.
	 */
	public Set<TransformationSequence> getSequences();
}
