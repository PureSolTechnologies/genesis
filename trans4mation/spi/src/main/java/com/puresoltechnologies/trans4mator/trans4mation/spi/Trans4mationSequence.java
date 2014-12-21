package com.puresoltechnologies.trans4mator.trans4mation.spi;

import java.util.List;

import com.puresoltechnologies.trans4mator.commons.Trans4mationStep;

/**
 * This interface is used to implement a single transformation sequence.
 * 
 * @author Rick-Rainer Ludwig
 */
public interface Trans4mationSequence {

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
     * This method returns an ordered(!) list of transformations.
     * 
     * @return A {@link List} is returned with the {@link Trans4mationStep}s to
     *         be processed in this very order.
     */
    public List<Trans4mationStep> getTransformations();

}
