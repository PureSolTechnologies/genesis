package com.puresoltechnologies.genesis.controller;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestTransformationTrackerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testCloseWithoutOpen() {
	exception.expect(IllegalStateException.class);
	TestTransformationTracker tracker = new TestTransformationTracker();
	tracker.close();
    }

    @Test
    public void testOpenWithOpen() {
	try (TestTransformationTracker tracker = new TestTransformationTracker()) {
	    tracker.open();
	    exception.expect(IllegalStateException.class);
	    tracker.open();
	}
    }

}
