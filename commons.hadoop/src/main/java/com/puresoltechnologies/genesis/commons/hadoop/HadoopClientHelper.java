package com.puresoltechnologies.genesis.commons.hadoop;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * This class provides some common functionality to deal with Hadoop.
 * 
 * @author Rick-Rainer Ludwig
 *
 */
public class HadoopClientHelper {

    private static final String DEFAULT_PATH_STRING = "/opt/hadoop";

    /**
     * This method provides the default configuration for Hadoop client. The
     * configuration for the client is looked up in /opt/hadoop. The Hadoop etc
     * directory is expected there.
     * 
     * @return A {@link Configuration} object is returned for the client to connect
     *         with.
     */
    public static Configuration createConfiguration() {
	return createConfiguration(DEFAULT_PATH_STRING);
    }

    private static Configuration createConfiguration(String defaultPathString) {
	return createConfiguration(new File(defaultPathString));
    }

    /**
     * This method provides the default configuration for Hadoop client. The
     * configuration for the client is looked up in the provided directory. The
     * Hadoop etc directory is expected there.
     * 
     * @param configurationDirectory
     *            is the directory where Hadoop's etc configuration directory can be
     *            found.
     * @return A {@link Configuration} object is returned for the client to connect
     *         with.
     */
    public static Configuration createConfiguration(File configurationDirectory) {
	Configuration hadoopConfiguration = new Configuration();
	hadoopConfiguration
		.addResource(new Path(new File(configurationDirectory, "etc/hadoop/core-site.xml").toString()));
	hadoopConfiguration
		.addResource(new Path(new File(configurationDirectory, "etc/hadoop/hdfs-site.xml").toString()));
	hadoopConfiguration
		.addResource(new Path(new File(configurationDirectory, "etc/hadoop/mapred-site.xml").toString()));
	return hadoopConfiguration;

    }

    /**
     * This method connects to Hadoop. The configuration for the client is looked up
     * in /opt/hadoop. The Hadoop etc directory is expected there.
     * 
     * @return A {@link FileSystem} object is returned to be used from the client.
     * @throws IOException
     *             is thrown in cases of unexpected events.
     */
    public static FileSystem connect() throws IOException {
	Configuration configuration = HadoopClientHelper.createConfiguration();
	return FileSystem.newInstance(configuration);
    }

    /**
     * This method connects to Hadoop. The configuration for the client is looked up
     * in the provided directory. The Hadoop etc directory is expected there.
     * 
     * @param configurationDirectory
     *            is the directory where Hadoop's etc configuration directory can be
     *            found.
     * @return A {@link FileSystem} object is returned to be used from the client.
     * @throws IOException
     *             is thrown in cases of unexpected events.
     */
    public static FileSystem connect(Properties configuration) throws IOException {
	Configuration conf = new Configuration();
	conf.set("fs.defaultFS", "hdfs://" + configuration.getProperty("transformator.dfs.host") + ":"
		+ configuration.getProperty("transformator.dfs.port"));
	return FileSystem.get(conf);
    }
}
