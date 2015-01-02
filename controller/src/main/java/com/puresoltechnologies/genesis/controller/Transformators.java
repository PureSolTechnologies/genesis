package com.puresoltechnologies.genesis.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import com.puresoltechnologies.genesis.tracker.spi.TransformationTracker;
import com.puresoltechnologies.genesis.transformation.spi.Transformator;

/**
 * This is a central service for all Trans4mation sequences.
 * 
 * @author Rick-Rainer Ludwig
 *
 */
class Transformators {

    private static final Map<Class<? extends Transformator>, Transformator> sequences = new HashMap<>();

    public static void loadAll() {
	ServiceLoader<Transformator> loader = ServiceLoader
		.load(Transformator.class);
	synchronized (sequences) {
	    for (Transformator loadedSequence : loader) {
		Class<? extends Transformator> loadedLogClass = loadedSequence
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

    public static Set<Transformator> getAll() {
	synchronized (sequences) {
	    return new HashSet<>(sequences.values());
	}
    }

    public static void verifySequences(TransformationTracker tracker) {
	for (Transformator transformator : getAll()) {
	    verifyTransformator(transformator);
	}
    }

    private static void verifyTransformator(Transformator transformator) {
	// TODO
    }
}
