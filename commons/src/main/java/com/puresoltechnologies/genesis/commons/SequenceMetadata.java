package com.puresoltechnologies.genesis.commons;

import com.puresoltechnologies.versioning.Version;
import com.puresoltechnologies.versioning.VersionRange;

public class SequenceMetadata {

    private final Version startVersion;
    private final VersionRange providedVersionRange;

    public SequenceMetadata(Version startVersion,
	    VersionRange providedVersionRange) {
	super();
	this.startVersion = startVersion;
	this.providedVersionRange = providedVersionRange;
    }

    /**
     * This method returns the version from which the migration can be taken up.
     * If this sequence is a start sequence which starts the installation, the
     * version 0.0.0 should be provided.
     * 
     * @return A {@link Version} object is returned containing the start version
     *         which should be present for the sequence to be able to start.
     */
    public Version getStartVersion() {
	return startVersion;
    }

    /**
     * This method returns a {@link VersionRange} which is the range of versions
     * this sequence is responsible of. The minimum version does not need to be
     * the start version provided by {@link #getStartVersion()}. It is possible,
     * that a sequence of a later version was consolidated and the start version
     * for instance is 0.0.0, but the first version transformed to is version
     * 1.0.0 skipping all development versions before.
     * 
     * @return A {@link VersionRange} is returned providing the range of
     *         versions this sequence can handle.
     */
    public VersionRange getProvidedVersionRange() {
	return providedVersionRange;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime
		* result
		+ ((providedVersionRange == null) ? 0 : providedVersionRange
			.hashCode());
	result = prime * result
		+ ((startVersion == null) ? 0 : startVersion.hashCode());
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
	SequenceMetadata other = (SequenceMetadata) obj;
	if (providedVersionRange == null) {
	    if (other.providedVersionRange != null)
		return false;
	} else if (!providedVersionRange.equals(other.providedVersionRange))
	    return false;
	if (startVersion == null) {
	    if (other.startVersion != null)
		return false;
	} else if (!startVersion.equals(other.startVersion))
	    return false;
	return true;
    }

}
