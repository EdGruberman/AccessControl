package edgruberman.bukkit.accesscontrol.versioning;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * format: &lt;major>.&lt;minor>.&lt;revision>[&lt;type>&lt;build>]
 * <p>
 * examples: 0.0.0a0 < 0.0.0 < 0.1.0b5 < 0.1.0rc1 < 0.1.0 < 1.0.0 < 17.500.3000
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Toolkit_version_format">
 * Mozilla Toolkit version format</a> implementation this is loosely based on
 *
 * @version 1.0.0
 */
public abstract class StandardVersion extends Version {

    public static final Pattern PARSE_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:(a|b|rc)(\\d+))?");

    /** @throws IllegalArgumentException if format is not recognized */
    public static StandardVersion parse(final String version) {
        final Matcher m = StandardVersion.PARSE_PATTERN.matcher(version);
        if (!m.find()) throw new IllegalArgumentException("unrecognized version \"" + version + "\"; expected <major>.<minor>.<revision>[<type><build>]");

        final IntegerVersionPart major = new IntegerVersionPart(Integer.parseInt(m.group(1)));
        final IntegerVersionPart minor =  new IntegerVersionPart(Integer.parseInt(m.group(2)));
        final IntegerVersionPart revision =  new IntegerVersionPart(Integer.parseInt(m.group(3)));
        if (m.group(4) == null) return new ReleasedStandardVersion(major, minor, revision);

        final TypeVersionPart type = TypeVersionPart.parse(m.group(4));
        final IntegerVersionPart build = new IntegerVersionPart(Integer.parseInt(m.group(5)));
        return new DevelopmentalStandardVersion(major, minor, revision, type, build);
    }



    private final IntegerVersionPart major;
    private final IntegerVersionPart minor;
    private final IntegerVersionPart revision;

    protected StandardVersion(final List<VersionPart> parts, final IntegerVersionPart major, final IntegerVersionPart minor, final IntegerVersionPart revision) {
        super(parts);
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    public IntegerVersionPart getMajor() {
        return this.major;
    }

    public IntegerVersionPart getMinor() {
        return this.minor;
    }

    public IntegerVersionPart getRevision() {
        return this.revision;
    }

}
