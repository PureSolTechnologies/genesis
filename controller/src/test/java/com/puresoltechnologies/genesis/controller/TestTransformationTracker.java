package com.puresoltechnologies.genesis.controller;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.tracker.spi.Severity;
import com.puresoltechnologies.genesis.tracker.spi.TransformationTracker;
import com.puresoltechnologies.versioning.Version;

public class TestTransformationTracker implements TransformationTracker {

	private static boolean isOpen = false;
	private static final Map<String, Map<String, List<TransformationMetadata>>> store = new ConcurrentHashMap<>();

	public static synchronized void clear() {
		store.clear();
	}

	@Override
	public void open() {
		if (isOpen) {
			throw new IllegalStateException(
					"Tracker shall not be open before opening.");
		}
		isOpen = true;
	}

	@Override
	public void close() {
		if (!isOpen) {
			throw new IllegalStateException(
					"Tracker shall be open before closing.");
		}
		isOpen = false;
	}

	@Override
	public synchronized void trackMigration(String machine,
			TransformationMetadata metadata) throws TransformationException {
		Map<String, List<TransformationMetadata>> machineStore = store
				.get(machine);
		if (machineStore == null) {
			machineStore = new ConcurrentHashMap<>();
			store.put(machine, machineStore);
		}
		String componentName = metadata.getComponentName();
		List<TransformationMetadata> componentStore = machineStore
				.get(componentName);
		if (componentStore == null) {
			componentStore = new ArrayList<>();
			machineStore.put(componentName, componentStore);
		}
		if (componentStore.contains(metadata)) {
			throw new IllegalStateException("Metadata '" + metadata
					+ "' already present!");
		}
		componentStore.add(metadata);
	}

	@Override
	public synchronized boolean wasMigrated(String machine, String component,
			Version version, String command) {
		int count = getRunCount(machine, component, version, command);
		if (count == 0) {
			return false;
		} else if (count > 1) {
			throw new IllegalStateException(
					"The cound of a command for a certain machine, component and version was greater than one.");
		} else {
			return true;
		}
	}

	public synchronized int getRunCount(String machine, String component,
			Version version, String command) {
		Map<String, List<TransformationMetadata>> machineStore = store
				.get(machine);
		if (machineStore == null) {
			return 0;
		}
		List<TransformationMetadata> componentStore = machineStore
				.get(component);
		if (componentStore == null) {
			return 0;
		}
		int count = 0;
		for (TransformationMetadata metadata : componentStore) {
			if ((metadata.getTargetVersion().compareTo(version) == 0)
					&& (metadata.getCommand().equals(command))) {
				count++;
			}
		}
		return count;
	}

	@Override
	public void log(Date time, Severity severity, InetAddress host,
			Thread thread, String message, Throwable cause) {
		System.out.println("date=" + time.toString() + "; severity=" + severity
				+ "; host=" + host + "; thread=" + thread + "; message="
				+ message);
		if (cause != null) {
			cause.printStackTrace(System.err);
		}
	}

	@Override
	public TransformationMetadata getLastTransformationMetadata(String machine,
			String component) {
		Map<String, List<TransformationMetadata>> machineStore = store
				.get(machine);
		if (machineStore == null) {
			return null;
		}
		List<TransformationMetadata> componentStore = machineStore
				.get(component);
		if ((componentStore == null) || (componentStore.isEmpty())) {
			return null;
		}
		return componentStore.get(componentStore.size() - 1);
	}

	public synchronized int getStepCount(String machine, String component) {
		Map<String, List<TransformationMetadata>> machineStore = store
				.get(machine);
		if (machineStore == null) {
			return 0;
		}
		List<TransformationMetadata> componentStore = machineStore
				.get(component);
		if ((componentStore == null) || (componentStore.isEmpty())) {
			return 0;
		}
		return componentStore.size();
	}

	public synchronized boolean isEmpty() {
		return store.isEmpty();
	}
}
