package edgruberman.bukkit.accesscontrol.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * format: Major.Minor.Revision[(Type)Build]
 * <p>example: 1.2.3a17
 * @version 2.0.0
 */
public final class Version implements Comparable<Version> {

    public static Version parse(final String version) {
        int major, minor, revision;
        Type type; Integer build;

        final Matcher m = Pattern.compile("(\\d+)\\.(\\d+).(\\d+)(?:(a|b|rc)(\\d+))?").matcher(version);
        if (!m.find()) throw new IllegalArgumentException("unrecognized version format \"" + version + "\"; expected <major>.<minor>.<revision>[<type><build>]");

        major = Integer.parseInt(m.group(1));
        minor = Integer.parseInt(m.group(2));
        revision = Integer.parseInt(m.group(3));
        type = ( m.group(4) != null ? Type.parse(m.group(4)) : null );
        build = ( m.group(5) != null ? Integer.parseInt(m.group(5)) : null );

        return new Version(major, minor, revision, type, build);
    }



    private final int major;
    private final int minor;
    private final int revision;
    private final Type type;
    private final Integer build;

    public Version(final int major, final int minor, final int revision, final Type type, final Integer build) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.type = type;
        this.build = build;
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getRevision() {
        return this.revision;
    }

    public Type getType() {
        return this.type;
    }

    public Integer getBuild() {
        return this.build;
    }

    public String readable() {
        return MessageFormat.format("{0}.{1}.{2}{5,choice,0#|1#{3}{4}}", this.major, this.minor, this.revision, this.type, this.build, this.type!=null?1:0);
    }

    @Override
    public String toString() {
        return this.readable();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.major;
        result = prime * result + this.minor;
        result = prime * result + this.revision;
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        result = prime * result + ((this.build == null) ? 0 : this.build.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Version)) return false;

        final Version other = (Version) obj;

        if (this.build == null) {
            if (other.build != null) return false;
        } else if (!this.build.equals(other.build)) return false;

        if (this.type == null) {
            if (other.type != null) return false;
        } else if (!this.type.equals(other.type)) return false;

        if (this.revision != other.revision) return false;
        if (this.minor != other.minor) return false;
        if (this.major != other.major) return false;
        return true;
    }

    @Override
    public int compareTo(final Version other) {
        // null instances are less than any non-null instances
        if (other == null) return 1;

        if (this.equals(other)) return 0;

        if (this.major != other.major) return Double.compare(this.major, other.major);
        if (this.minor != other.minor) return Double.compare(this.minor, other.minor);
        if (this.revision != other.revision) return Double.compare(this.revision, other.revision);

        if (this.type == null && other.type != null) return 1;
        if (this.type != null && other.type == null) return -1;
        if (!this.type.equals(other.type)) return this.type.compareTo(other.type);

        if (this.build == null && other.build != null) return 1;
        if (this.build != null && other.build == null) return -1;
        return this.build.compareTo(other.build);
    }





    /** software life-cycle indicator (alpha -> beta -> release candidate -> production release) */
    public static class Type implements Comparable<Type> {

        private static final Collection<Type> known = new ArrayList<Type>();

        public static final Type ALPHA = new Type("a", 0);
        public static final Type BETA = new Type("b", 1);
        public static final Type CANDIDATE = new Type("rc", 2);
        public static final Type RELEASE = new Type("", 3);

        public static Type parse(final String designator) {
            for (final Type type : Type.known) {
                if (type.designator.equals(designator)) return type;
            }

            throw new IllegalArgumentException("unknown designator: " + designator);
        }



        private final String designator;
        private final Integer level;

        private Type(final String designator, final int level) {
            this.designator = designator;
            this.level = level;
            Type.known.add(this);
        }

        public String getDesignator() {
            return this.designator;
        }

        public Integer getLevel() {
            return this.level;
        }

        @Override
        public String toString() {
            return this.designator;
        }

        @Override
        public int compareTo(final Type other) {
            return (other == null ? 1 : this.level.compareTo(other.level));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.designator == null) ? 0 : this.designator.hashCode());
            result = prime * result + ((this.level == null) ? 0 : this.level.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (!(obj instanceof Type)) return false;

            final Type other = (Type) obj;

            if (this.designator == null) {
                if (other.designator != null) return false;
            } else if (!this.designator.equals(other.designator)) return false;

            if (this.level == null) {
                if (other.level != null) return false;
            } else if (!this.level.equals(other.level)) return false;

            return true;
        }

    }

}
