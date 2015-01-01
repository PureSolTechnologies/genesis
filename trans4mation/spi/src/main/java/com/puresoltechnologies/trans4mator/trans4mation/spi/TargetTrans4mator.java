package com.puresoltechnologies.trans4mator.trans4mation.spi;

import java.util.Set;

/**
 * This is class provides a single transformator for a single target. This
 * transform
 * 
 * @author Rick-Rainer Ludwig
 *
 */
public interface TargetTrans4mator {

	/**
	 * This method returns the name of the transformation target. This target
	 * name is used to distinguish different sequences from each other.
	 */
	public String getTargetName();

	/**
	 * This method returns the sequences which are bound to this target.
	 * 
	 * @return A {@link Set} of {@link Trans4mationSequence} is returned.
	 */
	public Set<Trans4mationSequence> getSequences();
}
