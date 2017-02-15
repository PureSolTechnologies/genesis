package com.puresoltechnologies.genesis.transformation.ductiledb;

import java.io.IOException;
import java.util.Properties;

import com.puresoltechnologies.ductiledb.core.DuctileDB;
import com.puresoltechnologies.ductiledb.core.DuctileDBBootstrap;
import com.puresoltechnologies.ductiledb.core.DuctileDBConfiguration;
import com.puresoltechnologies.ductiledb.core.graph.GraphStore;
import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.transformation.spi.AbstractTransformationSequence;

public class DuctileDBTransformationSequence extends AbstractTransformationSequence {

    private DuctileDB ductileDB;
    private GraphStore ductileDBGraph;
    private final DuctileDBConfiguration configuration;

    public DuctileDBTransformationSequence(DuctileDBConfiguration configuration, SequenceMetadata metadata) {
	super(metadata);
	this.configuration = configuration;
    }

    @Override
    public final void open(Properties configuration) {
	try {
	    ductileDB = connect();
	    ductileDBGraph = ductileDB.getGraph();
	} catch (IOException e) {
	    throw new RuntimeException("Could not open DuctileDB.", e);
	}
    }

    private DuctileDB connect() throws IOException {
	DuctileDBBootstrap.start(configuration);
	return DuctileDBBootstrap.getInstance();
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

    protected final GraphStore getDuctileDBGraph() {
	return ductileDBGraph;
    }
}
