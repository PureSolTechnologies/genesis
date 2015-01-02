package com.puresoltechnologies.genesis.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import com.puresoltechnologies.genesis.transformation.spi.TransformationSequence;

/**
 * This is a central service for all Trans4mation sequences.
 * 
 * @author Rick-Rainer Ludwig
 *
 */
class TransformationSequences {

    private static final Map<Class<? extends TransformationSequence>, TransformationSequence> sequences = new HashMap<>();

    public static void loadAll() {
	ServiceLoader<TransformationSequence> loader = ServiceLoader
		.load(TransformationSequence.class);
	synchronized (sequences) {
	    for (TransformationSequence loadedSequence : loader) {
		Class<? extends TransformationSequence> loadedLogClass = loadedSequence
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

    public static Set<TransformationSequence> getAll() {
	synchronized (sequences) {
	    return new HashSet<>(sequences.values());
	}
    }
}
