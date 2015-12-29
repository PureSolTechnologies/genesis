package com.puresoltechnologies.genesis.transformation.ductiledb;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

import com.puresoltechnologies.ductiledb.api.DuctileDBGraph;
import com.puresoltechnologies.ductiledb.core.DuctileDBGraphFactory;
import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.transformation.spi.AbstractTransformationSequence;

public class DuctileDBTransformationSequence extends AbstractTransformationSequence {

    private DuctileDBGraph ductileDBGraph;

    private final String host;

    public DuctileDBTransformationSequence(String host, SequenceMetadata metadata) {
	super(metadata);
	this.host = host;
    }

    @Override
    public final void open() {
	try {
	    ductileDBGraph = connect();
	} catch (IOException e) {
	    throw new RuntimeException("Could not open DuctileDB.", e);
	}
    }

    private DuctileDBGraph connect() throws IOException {
	Configuration configuration = DuctileDBGraphFactory.createConfiguration();
	return DuctileDBGraphFactory.createGraph(configuration);
    }

    @Override
    public final void close() {
	try {
	    ductileDBGraph.close();
	} catch (IOException e) {
	    throw new RuntimeException("Could not close DuctileDB.", e);
	} finally {
	    ductileDBGraph = null;
	}
    }

    public final String getHost() {
	return host;
    }

    protected final DuctileDBGraph getDuctileDBGraph() {
	return ductileDBGraph;
    }
}
