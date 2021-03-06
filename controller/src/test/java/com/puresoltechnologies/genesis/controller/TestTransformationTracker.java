package com.puresoltechnologies.genesis.controller;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.tracker.spi.Severity;
import com.puresoltechnologies.genesis.tracker.spi.TransformationTracker;
import com.puresoltechnologies.versioning.Version;

public class TestTransformationTracker implements TransformationTracker {

    private static boolean isOpen = false;
    private static final Map<String, Map<InetAddress, List<TransformationMetadata>>> store = new ConcurrentHashMap<>();

    public static synchronized void clear() {
	store.clear();
    }

    @Override
    public void open(Properties configuration) {
	if (isOpen) {
	    throw new IllegalStateException("Tracker shall not be open before opening.");
	}
	isOpen = true;
    }

    @Override
    public void close() {
	if (!isOpen) {
	    throw new IllegalStateException("Tracker shall be open before closing.");
	}
	isOpen = false;
    }

    @Override
    public synchronized void trackMigration(InetAddress machine, TransformationMetadata metadata)
	    throws TransformationException {
	String componentName = metadata.getComponentName();
	Map<InetAddress, List<TransformationMetadata>> componentStore = store.get(componentName);
	if (componentStore == null) {
	    componentStore = new ConcurrentHashMap<>();
	    store.put(componentName, componentStore);
	}
	List<TransformationMetadata> machineStore = componentStore.get(machine);
	if (machineStore == null) {
	    machineStore = new ArrayList<>();
	    componentStore.put(machine, machineStore);
	}
	if (machineStore.contains(metadata)) {
	    throw new IllegalStateException("Metadata '" + metadata + "' already present!");
	}
	machineStore.add(metadata);
    }

    @Override
    public synchronized boolean wasMigrated(String component, InetAddress machine, Version version, String command) {
	int count = getRunCount(component, machine, version, command);
	if (count == 0) {
	    return false;
	} else if (count > 1) {
	    throw new IllegalStateException(
		    "The cound of a command for a certain machine, component and version was greater than one.");
	} else {
	    return true;
	}
    }

    public synchronized int getRunCount(String component, InetAddress machine, Version version, String command) {
	Map<InetAddress, List<TransformationMetadata>> componentStore = store.get(component);
	if (componentStore == null) {
	    return 0;
	}
	List<TransformationMetadata> machineStore = componentStore.get(machine);
	if (machineStore == null) {
	    return 0;
	}
	int count = 0;
	for (TransformationMetadata metadata : machineStore) {
	    if ((metadata.getTargetVersion().compareTo(version) == 0) && (metadata.getCommand().equals(command))) {
		count++;
	    }
	}
	return count;
    }

    @Override
    public void dropComponentHistory(String component, InetAddress machine) {
	Map<InetAddress, List<TransformationMetadata>> componentStore = store.get(component);
	if (componentStore == null) {
	    return;
	}
	componentStore.remove(machine);
    }

    @Override
    public void log(Instant time, Severity severity, InetAddress host, Thread thread, String message, Throwable cause) {
	System.out.println("date=" + time.toString() + "; severity=" + severity + "; host=" + host + "; thread="
		+ thread + "; message=" + message);
	if (cause != null) {
	    cause.printStackTrace(System.err);
	}
    }

    @Override
    public TransformationMetadata getLastTransformationMetadata(String component, InetAddress machine) {
	Map<InetAddress, List<TransformationMetadata>> componentStore = store.get(component);
	if (componentStore == null) {
	    return null;
	}
	List<TransformationMetadata> machineStore = componentStore.get(machine);
	if ((machineStore == null) || (machineStore.isEmpty())) {
	    return null;
	}
	return machineStore.get(machineStore.size() - 1);
    }

    public synchronized int getStepCount(String component, InetAddress machine) {
	Map<InetAddress, List<TransformationMetadata>> componentStore = store.get(component);
	if (componentStore == null) {
	    return 0;
	}
	List<TransformationMetadata> machineStore = componentStore.get(machine);
	if ((machineStore == null) || (machineStore.isEmpty())) {
	    return 0;
	}
	return machineStore.size();
    }

    public synchronized boolean isEmpty() {
	return store.isEmpty();
    }
}
