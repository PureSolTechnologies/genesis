package com.puresoltechnologies.genesis.controller.statemodel;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.puresoltechnologies.genesis.controller.InvalidSequenceException;
import com.puresoltechnologies.genesis.controller.TestSequence;
import com.puresoltechnologies.genesis.controller.TestTransformator;
import com.puresoltechnologies.versioning.Version;

public class MigrationModelTest {

    /**
     * It is only an automated big bang test here, but started manually, the
     * output can be checked.
     * 
     * @throws InvalidSequenceException
     *             in case of an invalid sequence.
     */
    @Test
    public void testPrint() throws InvalidSequenceException {
	TestTransformator transformator = new TestTransformator("component",
		true);
	transformator.addSequence(new TestSequence(transformator
		.getComponentName(), new Version(0, 0, 0),
		new Version(0, 1, 0), new Version(0, 2, 0)));
	transformator.addSequence(new TestSequence(transformator
		.getComponentName(), new Version(0, 1, 0),
		new Version(0, 2, 0), new Version(1, 0, 0)));
	transformator.addSequence(new TestSequence(transformator
		.getComponentName(), new Version(0, 1, 0),
		new Version(1, 0, 0), null));
	transformator.addSequence(new TestSequence(transformator
		.getComponentName(), new Version(0, 2, 0),
		new Version(1, 0, 0), null));
	transformator.addSequence(new TestSequence(transformator
		.getComponentName(), new Version(0, 0, 0),
		new Version(1, 0, 0), null));
	MigrationModel model = MigrationModel.create(transformator);
	assertNotNull(model);
	model.print(System.out);
    }

}
