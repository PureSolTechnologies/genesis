package com.puresoltechnologies.genesis.transformation.spi;

import java.util.List;

import com.puresoltechnologies.versioning.Version;
import com.puresoltechnologies.versioning.VersionRange;

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
     * This method returns the version from which the migration can be taken up.
     * If this sequence is a start sequence which starts the installation, the
     * version 0.0.0 should be provided.
     * 
     * @return A {@link Version} object is returned containing the start version
     *         which should be present for the sequence to be able to start.
     */
    public Version getStartVersion();

    /**
     * This method returns a {@link VersionRange} which is the range of versions
     * this sequence is responsible of. The minimum version does not need to be
     * the start version provided by {@link #getStartVersion()}. It is possible,
     * that a sequence of a later version was consolidated and the start version
     * for instance is 0.0.0, but the first version transformed to is version
     * 1.0.0 skipping all development versions before.
     * 
     * @return A {@link VersionRange} is returned providing the range of
     *         versions this sequence can handle.
     */
    public VersionRange getProvidedVersionRange();

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
     * @return A {@link List} is returned with the {@link TransformationStep}s
     *         to be processed in this very order.
     */
    public List<TransformationStep> getTransformations();

}
