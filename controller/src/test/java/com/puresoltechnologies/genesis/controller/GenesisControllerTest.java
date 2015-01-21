package com.puresoltechnologies.genesis.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.versioning.Version;

public class GenesisControllerTest extends AbstractControllerTest {

    @Test
    public void testWithoutSequences() throws TransformationException,
	    InvalidSequenceException {
	try (GenesisController controller = new GenesisController()) {
	    controller.transform();
	}
    }

    @Test
    public void testWithSingleComponentAndNoSequence()
	    throws TransformationException, InvalidSequenceException {
	try (GenesisController controller = new GenesisController()) {
	    Transformators.addTransformator(new TestTransformator("component",
		    true));
	    controller.transform();
	}
    }

    @Test
    public void testWithSingleComponentAndOneSequenceNoTransformation()
	    throws TransformationException, InvalidSequenceException {
	try (GenesisController controller = new GenesisController()) {
	    TestTransformator transformator = new TestTransformator(
		    "component", true);
	    TestSequence sequence = new TestSequence(
		    transformator.getComponentName(), new Version(0, 0, 0),
		    new Version(1, 0, 0), null);
	    transformator.addSequence(sequence);
	    Transformators.addTransformator(transformator);
	    controller.transform();
	}
    }

    @Test
    public void testWithSingleComponentAndOneSequenceOneTransformation()
	    throws TransformationException, InvalidSequenceException {
	TestTransformationTracker tracker = getTracker();
	try (GenesisController controller = new GenesisController()) {
	    TestTransformator transformator = new TestTransformator(
		    "component", true);
	    TestSequence sequence1 = new TestSequence(
		    transformator.getComponentName(), new Version(0, 0, 0),
		    new Version(1, 0, 0), null);
	    TestTransformationStep step1 = new TestTransformationStep(
		    sequence1.getMetadata(), "Rick-Rainer Ludwig",
		    transformator.getComponentName(), "command", "comment");
	    sequence1.addTransformation(step1);
	    transformator.addSequence(sequence1);
	    Transformators.addTransformator(transformator);
	    TransformationMetadata metadata1 = step1.getMetadata();
	    assertFalse(tracker.wasMigrated(controller.getHost(),
		    transformator.getComponentName(), metadata1.getVersion(),
		    metadata1.getCommand()));
	    assertEquals(
		    0,
		    tracker.getRunCount(controller.getHost(),
			    transformator.getComponentName(),
			    metadata1.getVersion(), metadata1.getCommand()));

	    controller.transform();

	    assertTrue(tracker.wasMigrated(controller.getHost(),
		    transformator.getComponentName(), metadata1.getVersion(),
		    metadata1.getCommand()));
	    assertEquals(
		    1,
		    tracker.getRunCount(controller.getHost(),
			    transformator.getComponentName(),
			    metadata1.getVersion(), metadata1.getCommand()));

	    controller.transform();

	    assertTrue(tracker.wasMigrated(controller.getHost(),
		    transformator.getComponentName(), metadata1.getVersion(),
		    metadata1.getCommand()));
	    assertEquals(
		    1,
		    tracker.getRunCount(controller.getHost(),
			    transformator.getComponentName(),
			    metadata1.getVersion(), metadata1.getCommand()));
	}
    }

    @Test
    public void testWithTwoComponentsAndOneSequenceOneTransformationEach()
	    throws TransformationException, InvalidSequenceException {
	TestTransformationTracker tracker = getTracker();
	try (GenesisController controller = new GenesisController()) {
	    // component1
	    TestTransformator transformator1 = new TestTransformator(
		    "component1", true);
	    TestSequence sequence11 = new TestSequence(
		    transformator1.getComponentName(), new Version(0, 0, 0),
		    new Version(1, 0, 0), null);
	    TestTransformationStep step11 = new TestTransformationStep(
		    sequence11.getMetadata(), "Rick-Rainer Ludwig",
		    transformator1.getComponentName(), "command11", "comment11");
	    sequence11.addTransformation(step11);
	    transformator1.addSequence(sequence11);

	    // component2
	    TestTransformator transformator2 = new TestTransformator(
		    "component2", true);
	    TestSequence sequence21 = new TestSequence(
		    transformator2.getComponentName(), new Version(0, 0, 0),
		    new Version(1, 0, 0), null);
	    TestTransformationStep step21 = new TestTransformationStep(
		    sequence21.getMetadata(), "Rick-Rainer Ludwig",
		    transformator2.getComponentName(), "command21", "comment21");
	    sequence21.addTransformation(step21);
	    transformator2.addSequence(sequence21);

	    Transformators.addTransformator(transformator1);
	    Transformators.addTransformator(transformator2);

	    TransformationMetadata metadata11 = step11.getMetadata();
	    TransformationMetadata metadata21 = step21.getMetadata();
	    assertFalse(tracker.wasMigrated(controller.getHost(),
		    transformator1.getComponentName(), metadata11.getVersion(),
		    metadata11.getCommand()));
	    assertFalse(tracker.wasMigrated(controller.getHost(),
		    transformator2.getComponentName(), metadata21.getVersion(),
		    metadata21.getCommand()));
	    assertEquals(
		    0,
		    tracker.getRunCount(controller.getHost(),
			    transformator1.getComponentName(),
			    metadata11.getVersion(), metadata11.getCommand()));
	    assertEquals(
		    0,
		    tracker.getRunCount(controller.getHost(),
			    transformator2.getComponentName(),
			    metadata21.getVersion(), metadata21.getCommand()));

	    controller.transform();

	    assertTrue(tracker.wasMigrated(controller.getHost(),
		    transformator1.getComponentName(), metadata11.getVersion(),
		    metadata11.getCommand()));
	    assertTrue(tracker.wasMigrated(controller.getHost(),
		    transformator2.getComponentName(), metadata21.getVersion(),
		    metadata21.getCommand()));
	    assertEquals(
		    1,
		    tracker.getRunCount(controller.getHost(),
			    transformator1.getComponentName(),
			    metadata11.getVersion(), metadata11.getCommand()));
	    assertEquals(
		    1,
		    tracker.getRunCount(controller.getHost(),
			    transformator2.getComponentName(),
			    metadata21.getVersion(), metadata21.getCommand()));

	    controller.transform();

	    assertTrue(tracker.wasMigrated(controller.getHost(),
		    transformator1.getComponentName(), metadata11.getVersion(),
		    metadata11.getCommand()));
	    assertTrue(tracker.wasMigrated(controller.getHost(),
		    transformator2.getComponentName(), metadata21.getVersion(),
		    metadata21.getCommand()));
	    assertEquals(
		    1,
		    tracker.getRunCount(controller.getHost(),
			    transformator1.getComponentName(),
			    metadata11.getVersion(), metadata11.getCommand()));
	    assertEquals(
		    1,
		    tracker.getRunCount(controller.getHost(),
			    transformator2.getComponentName(),
			    metadata21.getVersion(), metadata21.getCommand()));
	}
    }
}
