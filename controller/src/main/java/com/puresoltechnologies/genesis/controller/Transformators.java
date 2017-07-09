package com.puresoltechnologies.genesis.controller;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import com.puresoltechnologies.genesis.transformation.spi.ComponentTransformator;

/**
 * This is a central service for all Trans4mation sequences.
 * 
 * @author Rick-Rainer Ludwig
 *
 */
class Transformators {

    private static final Set<ComponentTransformator> transformators = new HashSet<>();

    public static void loadAll() {
	ServiceLoader<ComponentTransformator> loader = ServiceLoader.load(ComponentTransformator.class);
	synchronized (transformators) {
	    for (ComponentTransformator loadedSequence : loader) {
		if (!transformators.contains(loadedSequence)) {
		    transformators.add(loadedSequence);
		}
	    }
	}
    }

    public static void unloadAll() {
	synchronized (transformators) {
	    transformators.clear();
	}
    }

    public static Set<ComponentTransformator> getAll() {
	synchronized (transformators) {
	    return new HashSet<>(transformators);
	}
    }

    static void addTransformator(ComponentTransformator transformator) {
	synchronized (transformators) {
	    transformators.add(transformator);
	}
    }
}
