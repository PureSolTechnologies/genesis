package com.puresoltechnologies.genesis.commons;

import com.puresoltechnologies.versioning.Version;

/**
 * This class represents a single dependency of a sequence to another sequence
 * with a special version.
 * 
 * @author Rick-Rainer Ludwig
 */
public class SequenceDependency {

	private final String component;
	private final Version targetVersion;

	public SequenceDependency(String component, Version targetVersion) {
		super();
		this.component = component;
		this.targetVersion = targetVersion;
	}

	public String getComponent() {
		return component;
	}

	public Version getTargetVersion() {
		return targetVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((component == null) ? 0 : component.hashCode());
		result = prime * result
				+ ((targetVersion == null) ? 0 : targetVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SequenceDependency other = (SequenceDependency) obj;
		if (component == null) {
			if (other.component != null)
				return false;
		} else if (!component.equals(other.component))
			return false;
		if (targetVersion == null) {
			if (other.targetVersion != null)
				return false;
		} else if (!targetVersion.equals(other.targetVersion))
			return false;
		return true;
	};

	@Override
	public String toString() {
		return "depends on " + component + " " + targetVersion;
	}
}
