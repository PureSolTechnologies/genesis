package com.puresoltechnologies.trans4mator.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import com.puresoltechnologies.trans4mator.trans4mation.spi.Trans4mationSequence;

/**
 * This is a central service for all Trans4mation sequences.
 * 
 * @author Rick-Rainer Ludwig
 *
 */
class Trans4mationSequences {

    private static final Map<Class<? extends Trans4mationSequence>, Trans4mationSequence> sequences = new HashMap<>();

    public static void loadAll() {
	ServiceLoader<Trans4mationSequence> loader = ServiceLoader
		.load(Trans4mationSequence.class);
	synchronized (sequences) {
	    for (Trans4mationSequence loadedSequence : loader) {
		Class<? extends Trans4mationSequence> loadedLogClass = loadedSequence
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

    public static Set<Trans4mationSequence> getAll() {
	synchronized (sequences) {
	    return new HashSet<>(sequences.values());
	}
    }
}
