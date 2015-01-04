package com.puresoltechnologies.genesis.controller.verification;

import com.puresoltechnologies.genesis.controller.InvalidSequenceException;
import com.puresoltechnologies.genesis.transformation.spi.Transformator;

/**
 * This class provides a verification for {@link Transformator} implementations.
 * 
 * @author Rick-Rainer Ludwig
 */
public class TransformatorVerifier {

    /**
     * This method verfies the validity of the sequences. There are several
     * rules to check:
     * <ol>
     * <li>There is only one sequence allowed for each combination of initial
     * version and start version. This avoids ambiguities.</li>
     * <li>There is only one sequence allowed which has not a maximum version.
     * This is the current active sequence. There is no other allowed to avoid
     * ambiguity.</li>
     * <li>For all start versions there should be at least one path to migrate
     * to.</li>
     * <li>All provided paths lead to the maximum version available in sequences
     * </li>
     * </ol>
     * 
     * @throws InvalidSequenceException
     */
    public static void verifySequences(Transformator transformator)
	    throws InvalidSequenceException {

    }

}
