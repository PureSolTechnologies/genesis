package com.puresoltechnologies.trans4mator.logger.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import com.puresoltechnologies.trans4mator.logger.spi.Trans4mationLog;

/**
 * This is a central service for all Trans4mation loggers. It is needed to
 * implement the lifecycle for the loggers.
 * 
 * The handling of the lifecycle is done by the Trans4mator controller. And the
 * {@link Trans4mationLogger} uses the {@link #getAll()} method to retrieve the
 * loaded and opened loggers.
 * 
 * @author Rick-Rainer Ludwig
 *
 */
public class Trans4mationLogs {

    private static final Map<Class<? extends Trans4mationLog>, Trans4mationLog> logs = new HashMap<>();

    public static void loadAndOpenAll() {
	ServiceLoader<Trans4mationLog> loader = ServiceLoader
		.load(Trans4mationLog.class);
	synchronized (logs) {
	    for (Trans4mationLog loadedLog : loader) {
		Class<? extends Trans4mationLog> loadedLogClass = loadedLog
			.getClass();
		if (!logs.keySet().contains(loadedLogClass)) {
		    logs.put(loadedLogClass, loadedLog);
		    loadedLog.open();
		}
	    }
	}
    }

    public static void closeAndUnloadAll() {
	synchronized (logs) {
	    for (Trans4mationLog log : logs.values()) {
		log.close();
	    }
	    logs.clear();
	}
    }

    public static Set<Trans4mationLog> getAll() {
	synchronized (logs) {
	    return new HashSet<>(logs.values());
	}
    }
}
