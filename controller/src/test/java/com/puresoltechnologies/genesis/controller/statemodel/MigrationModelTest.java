package com.puresoltechnologies.genesis.controller.statemodel;

import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;

import com.puresoltechnologies.commons.versioning.Version;
import com.puresoltechnologies.commons.versioning.VersionRange;
import com.puresoltechnologies.genesis.controller.InvalidSequenceException;
import com.puresoltechnologies.genesis.transformation.spi.TransformationSequence;
import com.puresoltechnologies.genesis.transformation.spi.TransformationStep;
import com.puresoltechnologies.genesis.transformation.spi.ComponentTransformator;

public class MigrationModelTest {

    private static class TestSequence implements TransformationSequence {

	private final Version startVersion;
	private final Version targetVersion;
	private final Version finalVersion;

	public TestSequence(Version startVersion, Version targetVersion,
		Version finalVersion) {
	    super();
	    this.startVersion = startVersion;
	    this.targetVersion = targetVersion;
	    this.finalVersion = finalVersion;
	}

	@Override
	public void open() {
	    // intentionally left empty
	}

	@Override
	public void close() {
	    // intentionally left empty
	}

	@Override
	public Version getStartVersion() {
	    return startVersion;
	}

	@Override
	public VersionRange getProvidedVersionRange() {
	    return new VersionRange(targetVersion, true, finalVersion, false);
	}

	@Override
	public boolean isHostBased() {
	    return false;
	}

	@Override
	public List<TransformationStep> getTransformations() {
	    return null;
	}

    }

    /**
     * It is only an automated big bang test here, but started manually, the
     * output can be checked.
     * 
     * @throws InvalidSequenceException
     *             in case of an invalid sequence.
     */
    @Test
    public void testPrint() throws InvalidSequenceException {
	Set<TransformationSequence> sequences = new HashSet<>();
	sequences.add(new TestSequence(new Version(0, 0, 0), new Version(0, 1,
		0), new Version(0, 2, 0)));
	sequences.add(new TestSequence(new Version(0, 1, 0), new Version(0, 2,
		0), new Version(1, 0, 0)));
	sequences.add(new TestSequence(new Version(0, 1, 0), new Version(1, 0,
		0), null));
	sequences.add(new TestSequence(new Version(0, 2, 0), new Version(1, 0,
		0), null));
	sequences.add(new TestSequence(new Version(0, 0, 0), new Version(1, 0,
		0), null));
	ComponentTransformator transformator = Mockito.mock(ComponentTransformator.class);
	Mockito.when(transformator.getSequences()).thenReturn(sequences);
	MigrationModel model = MigrationModel.create(transformator);
	assertNotNull(model);
	model.print(System.out);
    }

}
