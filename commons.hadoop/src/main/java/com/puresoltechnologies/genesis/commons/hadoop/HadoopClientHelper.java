package com.puresoltechnologies.genesis.commons.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

/**
 * This class provides some common functionality to deal with Hadoop.
 * 
 * @author Rick-Rainer Ludwig
 *
 */
public class HadoopClientHelper {

    /**
     * This method provides the default configuration for Hadoop client.
     * 
     * @return
     */
    public static Configuration createConfiguration() {
	Configuration hadoopConfiguration = new Configuration();
	hadoopConfiguration.addResource(new Path("/opt/hadoop/etc/hadoop/core-site.xml"));
	hadoopConfiguration.addResource(new Path("/opt/hadoop/etc/hadoop/hdfs-site.xml"));
	hadoopConfiguration.addResource(new Path("/opt/hadoop/etc/hadoop/mapred-site.xml"));
	return hadoopConfiguration;
    }

}
