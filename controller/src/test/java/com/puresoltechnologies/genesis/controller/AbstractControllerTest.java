package com.puresoltechnologies.genesis.controller;

import org.junit.Before;

public class AbstractControllerTest {

    private final TestTransformationTracker tracker = new TestTransformationTracker();

    @Before
    public void clearTracker() {
	TestTransformationTracker.clear();
    }

    protected TestTransformationTracker getTracker() {
	return tracker;
    }

}
