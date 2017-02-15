package com.puresoltechnologies.genesis.controller;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.transformation.spi.ComponentTransformator;
import com.puresoltechnologies.versioning.Version;

public class GenesisControllerTest extends AbstractControllerTest {

    @Test
    public void testWithoutSequences()
	    throws TransformationException, InvalidSequenceException, NoTrackerFoundException {
	try (GenesisController controller = new GenesisController()) {
	    controller.migrate();
	}
    }

    @Test
    public void testWithSingleComponentAndNoSequence()
	    throws TransformationException, InvalidSequenceException, NoTrackerFoundException {
	try (GenesisController controller = new GenesisController()) {
	    Transformators.addTransformator(new TestTransformator("component", true));
	    controller.migrate();
	}
    }

    @Test
    public void testWithSingleComponentAndOneSequenceNoTransformation()
	    throws TransformationException, InvalidSequenceException, NoTrackerFoundException {
	try (GenesisController controller = new GenesisController()) {
	    TestTransformator transformator = new TestTransformator("component", true);
	    TestSequence sequence = new TestSequence(transformator.getComponentName(), new Version(0, 0, 0),
		    new Version(1, 0, 0), null);
	    transformator.addSequence(sequence);
	    Transformators.addTransformator(transformator);
	    controller.migrate();
	}
    }

    @Test
    public void testWithSingleComponentAndOneSequenceOneTransformation()
	    throws TransformationException, InvalidSequenceException, NoTrackerFoundException {
	TestTransformationTracker tracker = getTracker();
	try (GenesisController controller = new GenesisController()) {
	    TestTransformator transformator = new TestTransformator("component", true);
	    TestSequence sequence1 = new TestSequence(transformator.getComponentName(), new Version(0, 0, 0),
		    new Version(1, 0, 0), null);
	    TestTransformationStep step1 = new TestTransformationStep(sequence1.getMetadata(), "Rick-Rainer Ludwig",
		    "command", "comment");
	    sequence1.addTransformation(step1);
	    transformator.addSequence(sequence1);
	    Transformators.addTransformator(transformator);
	    TransformationMetadata metadata1 = step1.getMetadata();
	    assertFalse(tracker.wasMigrated(transformator.getComponentName(), controller.getHost(),
		    metadata1.getTargetVersion(), metadata1.getCommand()));
	    assertEquals(0, tracker.getRunCount(transformator.getComponentName(), controller.getHost(),
		    metadata1.getTargetVersion(), metadata1.getCommand()));

	    controller.migrate();

	    assertTrue(tracker.wasMigrated(transformator.getComponentName(), controller.getHost(),
		    metadata1.getTargetVersion(), metadata1.getCommand()));
	    assertEquals(1, tracker.getRunCount(transformator.getComponentName(), controller.getHost(),
		    metadata1.getTargetVersion(), metadata1.getCommand()));

	    controller.migrate();

	    assertTrue(tracker.wasMigrated(transformator.getComponentName(), controller.getHost(),
		    metadata1.getTargetVersion(), metadata1.getCommand()));
	    assertEquals(1, tracker.getRunCount(transformator.getComponentName(), controller.getHost(),
		    metadata1.getTargetVersion(), metadata1.getCommand()));
	}
    }

    @Test
    public void testWithTwoComponentsAndOneSequenceOneTransformationEach()
	    throws TransformationException, InvalidSequenceException, NoTrackerFoundException {
	TestTransformationTracker tracker = getTracker();
	try (GenesisController controller = new GenesisController()) {
	    // component1
	    TestTransformator transformator1 = new TestTransformator("component1", true);
	    TestSequence sequence11 = new TestSequence(transformator1.getComponentName(), new Version(0, 0, 0),
		    new Version(1, 0, 0), null);
	    TestTransformationStep step11 = new TestTransformationStep(sequence11.getMetadata(), "Rick-Rainer Ludwig",
		    "command11", "comment11");
	    sequence11.addTransformation(step11);
	    transformator1.addSequence(sequence11);

	    // component2
	    TestTransformator transformator2 = new TestTransformator("component2", true);
	    TestSequence sequence21 = new TestSequence(transformator2.getComponentName(), new Version(0, 0, 0),
		    new Version(1, 0, 0), null);
	    TestTransformationStep step21 = new TestTransformationStep(sequence21.getMetadata(), "Rick-Rainer Ludwig",
		    "command21", "comment21");
	    sequence21.addTransformation(step21);
	    transformator2.addSequence(sequence21);

	    Transformators.addTransformator(transformator1);
	    Transformators.addTransformator(transformator2);

	    TransformationMetadata metadata11 = step11.getMetadata();
	    TransformationMetadata metadata21 = step21.getMetadata();
	    assertFalse(tracker.wasMigrated(transformator1.getComponentName(), controller.getHost(),
		    metadata11.getTargetVersion(), metadata11.getCommand()));
	    assertFalse(tracker.wasMigrated(transformator2.getComponentName(), controller.getHost(),
		    metadata21.getTargetVersion(), metadata21.getCommand()));
	    assertEquals(0, tracker.getRunCount(transformator1.getComponentName(), controller.getHost(),
		    metadata11.getTargetVersion(), metadata11.getCommand()));
	    assertEquals(0, tracker.getRunCount(transformator2.getComponentName(), controller.getHost(),
		    metadata21.getTargetVersion(), metadata21.getCommand()));

	    controller.migrate();

	    assertTrue(tracker.wasMigrated(transformator1.getComponentName(), controller.getHost(),
		    metadata11.getTargetVersion(), metadata11.getCommand()));
	    assertTrue(tracker.wasMigrated(transformator2.getComponentName(), controller.getHost(),
		    metadata21.getTargetVersion(), metadata21.getCommand()));
	    assertEquals(1, tracker.getRunCount(transformator1.getComponentName(), controller.getHost(),
		    metadata11.getTargetVersion(), metadata11.getCommand()));
	    assertEquals(1, tracker.getRunCount(transformator2.getComponentName(), controller.getHost(),
		    metadata21.getTargetVersion(), metadata21.getCommand()));

	    controller.migrate();

	    assertTrue(tracker.wasMigrated(transformator1.getComponentName(), controller.getHost(),
		    metadata11.getTargetVersion(), metadata11.getCommand()));
	    assertTrue(tracker.wasMigrated(transformator2.getComponentName(), controller.getHost(),
		    metadata21.getTargetVersion(), metadata21.getCommand()));
	    assertEquals(1, tracker.getRunCount(transformator1.getComponentName(), controller.getHost(),
		    metadata11.getTargetVersion(), metadata11.getCommand()));
	    assertEquals(1, tracker.getRunCount(transformator2.getComponentName(), controller.getHost(),
		    metadata21.getTargetVersion(), metadata21.getCommand()));
	}
    }

    @Test
    public void testDependencySorting() {
	ComponentTransformator transformatorA = mock(ComponentTransformator.class);
	when(transformatorA.getComponentName()).thenReturn("A");
	when(transformatorA.getDependencies()).thenReturn(new HashSet<>(asList(new String[] { "B", "C" })));
	ComponentTransformator transformatorB = mock(ComponentTransformator.class);
	when(transformatorB.getComponentName()).thenReturn("B");
	ComponentTransformator transformatorC = mock(ComponentTransformator.class);
	when(transformatorC.getComponentName()).thenReturn("C");
	when(transformatorC.getDependencies()).thenReturn(new HashSet<>(asList(new String[] { "B" })));
	ComponentTransformator transformatorD = mock(ComponentTransformator.class);
	when(transformatorD.getComponentName()).thenReturn("D");
	when(transformatorD.getDependencies()).thenReturn(new HashSet<>(asList(new String[] { "C" })));
	List<ComponentTransformator> transformators = new ArrayList<>();
	transformators.add(transformatorA);
	transformators.add(transformatorB);
	transformators.add(transformatorC);
	transformators.add(transformatorD);
	GenesisController.sortTransformatorsByDependencies(transformators);
	assertEquals(4, transformators.size());
	assertEquals("B", transformators.get(0).getComponentName());
	assertEquals("C", transformators.get(1).getComponentName());
	assertEquals("A", transformators.get(2).getComponentName());
	assertEquals("D", transformators.get(3).getComponentName());
    }

}
