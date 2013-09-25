package edgruberman.bukkit.accesscontrol.versioning;

import java.util.Collections;
import java.util.List;

/**
 * comprised of multiple {@link #VersionPart} instances that are each
 * individually compared to the same index from another Version
 *
 * @version 1.0.0
 */
public abstract class Version implements Comparable<Version> {

    /** useful for null object pattern implementations to indicate no version */
    public static final MissingVersion MISSING = new MissingVersion();

    public static final IntegerVersionPart EMPTY_PART = new IntegerVersionPart(Integer.MIN_VALUE);

    protected final List<VersionPart> parts;

    public Version(final List<VersionPart> parts) {
        this.parts = Collections.unmodifiableList(parts);
    }

    public List<VersionPart> getParts() {
        return this.parts;
    }

    public VersionPart getPart(final int index) {
        return this.parts.get(index);
    }

    /**
     * compares individual VersionParts for the same index in order
     * <p>
     * when two versions have a differing number of parts,
     * {@link Version#EMPTY_PART} is used for comparison
     *
     * @return first non-equal result
     *
     * @throws IndexOutOfBoundsException when Versions do not contain
     * the same number of VersionParts
     */
    @Override
    public int compareTo(final Version other) {
        final int max = Math.max(this.getParts().size(), other.getParts().size());

        for (int i = 0; i < max; i++) {
            final VersionPart thisPart = ( i < this.getParts().size() ? this.getPart(i) : Version.EMPTY_PART );
            final VersionPart otherPart = ( i < other.getParts().size() ? other.getPart(i) : Version.EMPTY_PART );
            final int comparison = thisPart.compareTo(otherPart);
            if (comparison != 0) return comparison;
        }

        return 0;
    }

    public abstract String toReadable();

    @Override
    public String toString() {
        return this.toReadable();
    }

}
