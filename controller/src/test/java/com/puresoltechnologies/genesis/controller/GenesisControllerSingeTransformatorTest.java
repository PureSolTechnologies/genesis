package com.puresoltechnologies.genesis.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.transformation.spi.ComponentTransformator;
import com.puresoltechnologies.versioning.Version;

public class GenesisControllerSingeTransformatorTest extends
		AbstractControllerTest {

	public static ComponentTransformator createComplexTransformator(
			String componentPrefix, boolean hostBased) {
		TestTransformator transformator = new TestTransformator(componentPrefix
				+ "Component", hostBased);
		TestSequence sequence01 = new TestSequence(
				transformator.getComponentName(), new Version(0, 0, 0),
				new Version(0, 1, 0), new Version(0, 2, 0));
		sequence01.addTransformation(new TestTransformationStep(sequence01
				.getMetadata(), "RRL", "command01", "comment01"));
		TestSequence sequence02 = new TestSequence(
				transformator.getComponentName(), new Version(0, 1, 0),
				new Version(0, 2, 0), new Version(0, 4, 0));
		sequence02.addTransformation(new TestTransformationStep(sequence02
				.getMetadata(), "RRL", "command02", "comment02"));
		TestSequence sequence04 = new TestSequence(
				transformator.getComponentName(), new Version(0, 2, 0),
				new Version(0, 4, 0), new Version(1, 0, 0));
		sequence04.addTransformation(new TestTransformationStep(sequence04
				.getMetadata(), "RRL", "command04", "comment04"));
		TestSequence sequence10 = new TestSequence(
				transformator.getComponentName(), new Version(0, 4, 0),
				new Version(1, 0, 0), new Version(1, 1, 0));
		sequence10.addTransformation(new TestTransformationStep(sequence10
				.getMetadata(), "RRL", "command10", "comment10"));
		TestSequence sequence10_2 = new TestSequence(
				transformator.getComponentName(), new Version(0, 0, 0),
				new Version(1, 0, 0), new Version(1, 1, 0));
		sequence10_2.addTransformation(new TestTransformationStep(sequence10_2
				.getMetadata(), "RRL", "command10_2", "comment10_2"));
		TestSequence sequence11 = new TestSequence(
				transformator.getComponentName(), new Version(1, 0, 0),
				new Version(1, 1, 0), new Version(1, 1, 0));
		sequence11.addTransformation(new TestTransformationStep(sequence11
				.getMetadata(), "RRL", "command11", "comment11"));
		TestSequence sequence12 = new TestSequence(
				transformator.getComponentName(), new Version(1, 1, 0),
				new Version(1, 2, 0), new Version(2, 0, 0));
		sequence12.addTransformation(new TestTransformationStep(sequence12
				.getMetadata(), "RRL", "command12", "comment12"));
		TestSequence sequence20 = new TestSequence(
				transformator.getComponentName(), new Version(1, 2, 0),
				new Version(2, 0, 0), null);
		sequence20.addTransformation(new TestTransformationStep(sequence20
				.getMetadata(), "RRL", "command20", "comment20"));
		TestSequence sequence20_2 = new TestSequence(
				transformator.getComponentName(), new Version(1, 0, 0),
				new Version(2, 0, 0), null);
		sequence20_2.addTransformation(new TestTransformationStep(sequence20_2
				.getMetadata(), "RRL", "command20_2", "comment20_2"));

		transformator.addSequence(sequence01);
		transformator.addSequence(sequence02);
		transformator.addSequence(sequence04);
		transformator.addSequence(sequence10);
		transformator.addSequence(sequence10_2);
		transformator.addSequence(sequence11);
		transformator.addSequence(sequence12);
		transformator.addSequence(sequence20);
		transformator.addSequence(sequence20_2);

		return transformator;
	}

	@Test
	public void testFullMigration() throws InvalidSequenceException,
			TransformationException, NoTrackerFoundException {
		TestTransformationTracker tracker = getTracker();
		assertTrue(tracker.isEmpty());
		ComponentTransformator transformator = createComplexTransformator(
				"Test1", true);
		Transformators.addTransformator(transformator);
		try (GenesisController controller = new GenesisController()) {
			controller.transform();
			assertEquals(2, tracker.getStepCount(
					transformator.getComponentName(), controller.getHost()));
			assertTrue(tracker.wasMigrated(transformator.getComponentName(),
					controller.getHost(), new Version(1, 0, 0), "command10_2"));
			assertTrue(tracker.wasMigrated(transformator.getComponentName(),
					controller.getHost(), new Version(2, 0, 0), "command20_2"));
		}
	}

	@Test
	public void testStepWiseMigration() throws InvalidSequenceException,
			TransformationException, NoTrackerFoundException {
		TestTransformationTracker tracker = getTracker();
		assertTrue(tracker.isEmpty());
		ComponentTransformator transformator = createComplexTransformator(
				"Test1", true);
		Transformators.addTransformator(transformator);
		try (GenesisController controller = new GenesisController()) {
			// Version 0.1.0
			controller.transform(new Version(0, 1, 0));
			assertEquals(1, tracker.getStepCount(
					transformator.getComponentName(), controller.getHost()));
			assertTrue(tracker.wasMigrated(transformator.getComponentName(),
					controller.getHost(), new Version(0, 1, 0), "command01"));
			// Version 0.2.0
			controller.transform(new Version(0, 2, 0));
			assertEquals(2, tracker.getStepCount(
					transformator.getComponentName(), controller.getHost()));
			assertTrue(tracker.wasMigrated(transformator.getComponentName(),
					controller.getHost(), new Version(0, 2, 0), "command02"));
			// Version 0.1.0 (nothing to be done...)
			controller.transform(new Version(0, 1, 0));
			assertEquals(2, tracker.getStepCount(
					transformator.getComponentName(), controller.getHost()));
			// Version 0.3.0 (nothing to be done...)
			controller.transform(new Version(0, 3, 0));
			assertEquals(2, tracker.getStepCount(
					transformator.getComponentName(), controller.getHost()));
			assertTrue(tracker.wasMigrated(transformator.getComponentName(),
					controller.getHost(), new Version(0, 1, 0), "command01"));
			// Version 0.4.0
			controller.transform(new Version(0, 4, 0));
			assertEquals(3, tracker.getStepCount(
					transformator.getComponentName(), controller.getHost()));
			assertTrue(tracker.wasMigrated(transformator.getComponentName(),
					controller.getHost(), new Version(0, 4, 0), "command04"));
			// Version 1.0.0
			controller.transform(new Version(1, 0, 0));
			assertEquals(4, tracker.getStepCount(
					transformator.getComponentName(), controller.getHost()));
			assertTrue(tracker.wasMigrated(transformator.getComponentName(),
					controller.getHost(), new Version(1, 0, 0), "command10"));
			// Version 1.1.0
			controller.transform(new Version(1, 1, 0));
			assertEquals(5, tracker.getStepCount(
					transformator.getComponentName(), controller.getHost()));
			assertTrue(tracker.wasMigrated(transformator.getComponentName(),
					controller.getHost(), new Version(1, 1, 0), "command11"));
			// Version 1.2.0
			controller.transform(new Version(1, 2, 0));
			assertEquals(6, tracker.getStepCount(
					transformator.getComponentName(), controller.getHost()));
			assertTrue(tracker.wasMigrated(transformator.getComponentName(),
					controller.getHost(), new Version(1, 2, 0), "command12"));
			// Version 2.0.0
			controller.transform(new Version(2, 0, 0));
			assertEquals(7, tracker.getStepCount(
					transformator.getComponentName(), controller.getHost()));
			assertTrue(tracker.wasMigrated(transformator.getComponentName(),
					controller.getHost(), new Version(2, 0, 0), "command20"));
		}
	}
}
