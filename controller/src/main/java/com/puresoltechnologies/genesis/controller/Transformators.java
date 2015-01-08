package com.puresoltechnologies.genesis.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import com.puresoltechnologies.genesis.tracker.spi.TransformationTracker;
import com.puresoltechnologies.genesis.transformation.spi.ComponentTransformator;

/**
 * This is a central service for all Trans4mation sequences.
 * 
 * @author Rick-Rainer Ludwig
 *
 */
class Transformators {

    private static final Map<Class<? extends ComponentTransformator>, ComponentTransformator> sequences = new HashMap<>();

    public static void loadAll() {
	ServiceLoader<ComponentTransformator> loader = ServiceLoader
		.load(ComponentTransformator.class);
	synchronized (sequences) {
	    for (ComponentTransformator loadedSequence : loader) {
		Class<? extends ComponentTransformator> loadedLogClass = loadedSequence
			.getClass();
		if (!sequences.keySet().contains(loadedLogClass)) {
		    sequences.put(loadedLogClass, loadedSequence);
		}
	    }
	}
    }

    public static void unloadAll() {
	synchronized (sequences) {
	    sequences.clear();
	}
    }

    public static Set<ComponentTransformator> getAll() {
	synchronized (sequences) {
	    return new HashSet<>(sequences.values());
	}
    }

    public static void verifySequences(TransformationTracker tracker) {
	for (ComponentTransformator transformator : getAll()) {
	    verifyTransformator(transformator);
	}
    }

    private static void verifyTransformator(ComponentTransformator transformator) {
	// TODO
    }
}
