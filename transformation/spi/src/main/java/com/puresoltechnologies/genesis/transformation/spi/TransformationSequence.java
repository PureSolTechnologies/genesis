package com.puresoltechnologies.genesis.transformation.spi;

import java.util.List;

import com.puresoltechnologies.genesis.commons.SequenceMetadata;

/**
 * This interface is used to implement a single transformation sequence.
 * 
 * @author Rick-Rainer Ludwig
 */
public interface TransformationSequence extends AutoCloseable {

    /**
     * This method is called before {@link #getTransformations()} is called.
     * This is the place to open database connections.
     */
    public void open();

    /**
     * This method is called after the
     */
    @Override
    public void close();

    /**
     * This method returns the metadata of the sequence.
     * 
     * @return A {@link SequenceMetadata} object is returned.
     */
    public SequenceMetadata getMetadata();

    /**
     * This method returns an ordered(!) list of transformations.
     * 
     * @return A {@link List} is returned with the {@link TransformationStep}s
     *         to be processed in this very order.
     */
    public List<TransformationStep> getTransformations();

}
