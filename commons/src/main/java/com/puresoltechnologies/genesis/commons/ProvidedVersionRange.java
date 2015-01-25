package com.puresoltechnologies.genesis.commons;

import java.util.Objects;

import com.puresoltechnologies.versioning.Version;
import com.puresoltechnologies.versioning.VersionRange;

public class ProvidedVersionRange extends VersionRange {

	private static final long serialVersionUID = 6619616949182792683L;

	public ProvidedVersionRange(Version targetVersion, Version nextVersion) {
		super(Objects.requireNonNull(targetVersion,
				"startVersion must not be null."), true, nextVersion, false);
		if ((nextVersion != null) && (targetVersion.compareTo(nextVersion) >= 0)) {
			throw new IllegalArgumentException(
					"nextVersion must be greater than startVersion.");
		}
	}

	public final Version getTargetVersion() {
		return getMinimum();
	}

	public final Version getNextVersion() {
		return getMaximum();
	}
}
