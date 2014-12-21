package com.puresoltechnologies.trans4mator.logger.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.trans4mator.logger.spi.Severity;
import com.puresoltechnologies.trans4mator.logger.spi.Trans4mationLog;

public class Trans4mationLogger {

	private static final Logger logger = LoggerFactory
			.getLogger(Trans4mationLogger.class);

	public static Trans4mationLogger getLogger(Class<?> clazz) {
		return new Trans4mationLogger(clazz);
	}

	private final Class<?> clazz;
	private final String className;
	private final InetAddress host;

	private Trans4mationLogger(Class<?> clazz) {
		this.clazz = clazz;
		className = clazz.getName();
		host = determineHost();
	}

	private InetAddress determineHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			logger.warn("Could not determin host.", e);
			return null;
		}
	}

	public void logInfo(String message) {
		log(Severity.INFO, message, null);
	}

	public void logWarn(String message) {
		log(Severity.WARN, message, null);
	}

	public void logWarn(String message, Throwable cause) {
		log(Severity.WARN, message, cause);
	}

	public void logError(String message) {
		log(Severity.ERROR, message, null);
	}

	public void logError(String message, Throwable cause) {
		log(Severity.ERROR, message, cause);
	}

	private void log(Severity severity, String message, Throwable cause) {
		Date time = new Date();
		Thread currentThread = Thread.currentThread();
		int line = determineLineNumber(currentThread);
		logToSlf4j(time, severity, currentThread, line, message, cause);
		for (Trans4mationLog log : Trans4mationLogs.getAll()) {
			log.log(time, severity, host, currentThread, clazz, line, message,
					cause);
		}
	}

	private int determineLineNumber(Thread currentThread) {
		StackTraceElement[] stackTraceElements = currentThread.getStackTrace();
		for (StackTraceElement element : stackTraceElements) {
			if (element.getClassName().equals(className)) {
				return element.getLineNumber();
			}
		}
		return -1;
	}

	private void logToSlf4j(Date time, Severity severity, Thread thread,
			int line, String message, Throwable cause) {
		StringBuilder builder = new StringBuilder();
		builder.append("time=" + time);
		builder.append("; severity=" + severity.name());
		builder.append("; host=" + host);
		builder.append("; tread=" + thread.getName());
		builder.append("; location=" + clazz.getName() + ":" + line);
		builder.append("; message=" + message);
		if (cause != null) {
			builder.append("; cause=" + cause.getMessage());
		}
		switch (severity) {
		case INFO:
			logger.info(builder.toString());
			break;
		case WARN:
			logger.warn(builder.toString());
			break;
		case ERROR:
			logger.error(builder.toString());
			break;
		}
	}
}