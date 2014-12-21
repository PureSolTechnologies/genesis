package com.puresoltechnologies.trans4mator.logger.cassandra;

import java.net.InetAddress;
import java.util.Date;

import com.datastax.driver.core.Cluster;
import com.puresoltechnologies.trans4mator.commons.cassandra.CassandraUtils;
import com.puresoltechnologies.trans4mator.logger.spi.Severity;
import com.puresoltechnologies.trans4mator.logger.spi.Trans4mationLog;

public class CassandraTrans4mationLog implements Trans4mationLog {

	private Cluster cluster;

	@Override
	public void open() {
		cluster = CassandraUtils.connectCluster();
	}

	@Override
	public void close() {
		cluster.close();
	}

	@Override
	public void log(Date time, Severity severity, InetAddress host,
			Thread thread, Class<?> clazz, int line, String message,
			Throwable cause) {
		// TODO Auto-generated method stub

	}

}
