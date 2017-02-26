package com.puresoltechnologies.genesis.transformation.spi;

import java.util.Properties;
import java.util.Set;

/**
 * This is class provides a single transformator for a single target. This
 * transform
 * 
 * @author Rick-Rainer Ludwig
 *
 */
public interface ComponentTransformator {

    /**
     * This method returns the name of the transformation target. This target
     * name is used to distinguish different sequences from each other.
     * 
     * @return A {@link String} is returned containing the name of the
     *         component.
     */
    public String getComponentName();

    /**
     * This method returns a {@link Set} of components names on which this
     * transformator depends on or in other words: Dependencies are to be
     * migrated before this transformator can be run.
     * 
     * @return A {@link Set} of components names are returned.
     */
    public Set<String> getDependencies();

    /**
     * This method provides information about whether this sequence is host
     * based or not. Host based transformation are tracked per host and are
     * meant to run on each host again (like altering local configurations or
     * migrating local data). If a sequence is not host based, there is only one
     * run from one of the transformers. This is needed for instance for
     * database changes.
     * 
     * @return <code>true</code> is returned if the sequence needs to run on
     *         each host individually. <code>false</code> is returned otherwise.
     */
    public boolean isHostBased();

    /**
     * This method returns the sequences which are bound to this target.
     * 
     * @return A {@link Set} of {@link TransformationSequence} is returned.
     */
    public Set<TransformationSequence> getSequences();

    /**
     * This method implements a drop all mechanism which removes all(!) changes
     * made. We do not support rollbacks aka. down migrations, but it may be
     * needed to remove a component.
     */
    public void dropAll(Properties configuration);

}
