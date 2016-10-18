package com.puresoltechnologies.genesis.transformation.ductiledb;

import java.io.IOException;

import com.puresoltechnologies.ductiledb.core.DuctileDB;
import com.puresoltechnologies.ductiledb.core.DuctileDBBootstrap;
import com.puresoltechnologies.ductiledb.core.graph.GraphStore;
import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.transformation.spi.AbstractTransformationSequence;

public class DuctileDBTransformationSequence extends AbstractTransformationSequence {

    private DuctileDB ductileDB;
    private GraphStore ductileDBGraph;

    private final String host;

    public DuctileDBTransformationSequence(String host, SequenceMetadata metadata) {
	super(metadata);
	this.host = host;
    }

    @Override
    public final void open() {
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

    public final String getHost() {
	return host;
    }

    protected final GraphStore getDuctileDBGraph() {
	return ductileDBGraph;
    }
}
