package com.puresoltechnologies.genesis.transformation.titan;

import java.util.Properties;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.transformation.spi.AbstractTransformationSequence;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;

public class TitanTransformationSequence extends AbstractTransformationSequence {

    private TitanGraph titanGraph;

    private final String host;

    public TitanTransformationSequence(String host, SequenceMetadata metadata) {
	super(metadata);
	this.host = host;
    }

    @Override
    public final void open(Properties configuration) {
	titanGraph = connect();
    }

    private TitanGraph connect() {
	Configuration conf = getConfigurationForHBaseBackend();
	return TitanFactory.open(conf);
    }

    private Configuration getConfigurationForHBaseBackend() {
	Configuration conf = new BaseConfiguration();
	conf.setProperty("storage.backend", "hbase");
	conf.setProperty("storage.hostname", host);
	return conf;
    }

    @Override
    public final void close() {
	titanGraph.shutdown();
	titanGraph = null;
    }

    public final String getHost() {
	return host;
    }

    protected final TitanGraph getTitanGraph() {
	return titanGraph;
    }
}
