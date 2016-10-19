package com.puresoltechnologies.genesis.tracker.test;

import org.junit.Before;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.controller.GenesisController;
import com.puresoltechnologies.genesis.controller.InvalidSequenceException;
import com.puresoltechnologies.genesis.controller.NoTrackerFoundException;
import com.puresoltechnologies.genesis.tracker.spi.TransformationTracker;

public class AbstractTransformationTrackerTest {

    /**
     * Simple big bang test to check whether {@link TransformationTracker} can
     * be loaded via SPI.
     * 
     * @throws NoTrackerFoundException
     *             is thrown if no tracker was found.
     * @throws TransformationException
     *             if transformation fails.
     * @throws InvalidSequenceException
     *             if sequences is invalid.
     */
    @Before
    public void testSPILoading() throws NoTrackerFoundException, TransformationException, InvalidSequenceException {
	try (GenesisController controller = new GenesisController()) {
	    controller.transform();
	}
    }

}
