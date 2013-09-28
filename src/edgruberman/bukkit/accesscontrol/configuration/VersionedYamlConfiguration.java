package edgruberman.bukkit.accesscontrol.configuration;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;

import edgruberman.bukkit.accesscontrol.versioning.StandardVersion;
import edgruberman.bukkit.accesscontrol.versioning.Version;

/**
 * stores version tag in header
 * and supports migration of existing configuration files
 * @version 1.0.1
 */
public class VersionedYamlConfiguration extends YamlConfiguration {

    /** first capturing group is used to determine version */
    public static final Pattern VERSION_PARSE = Pattern.compile("(?:^" + YamlConfiguration.COMMENT_PREFIX + "@version (" + StandardVersion.PARSE_PATTERN.pattern() + ")\\r?\\n)");

    /** first capturing group is used to remove version tag */
    public static final Pattern VERSION_STRIP = Pattern.compile("(" + VersionedYamlConfiguration.VERSION_PARSE.pattern() + ")");

    /** 0 = readable, 1 = built header, 2 = line break */
    public static final String VERSION_STAMP = "# @version {0}{2}{1}";

    protected static final String LINE_SEPARATOR = System.getProperty("line.separator");



    protected Version version = Version.MISSING;

    public Version getVersion() {
        return this.version;
    }

    public void setVersion(final Version version) {
        this.version = version;
    }

    // parse and strip version tag
    @Override
    protected String parseHeader(final String input) {
        final Matcher value = VersionedYamlConfiguration.VERSION_PARSE.matcher(input);
        final String found = ( value.find() ? value.group(1) : null );
        if (found == null) return super.parseHeader(input);

        this.version = StandardVersion.parse(found);

        final Matcher tag = VersionedYamlConfiguration.VERSION_STRIP.matcher(input);
        if (!tag.find()) return super.parseHeader(input);

        final String stripped = tag.replaceFirst("");
        return super.parseHeader(stripped);
    }

    // stamp version tag
    @Override
    protected String buildHeader() {
        final String built = super.buildHeader();
        if (this.version == Version.MISSING) return built;

        final String stamped = MessageFormat.format(VersionedYamlConfiguration.VERSION_STAMP, this.version.toReadable(), built, VersionedYamlConfiguration.LINE_SEPARATOR);
        return stamped;
    }

    public boolean conflicts(final ConfigurationDefinition definition) {
        return definition.conflicts(this, this.getVersion());
    }

    public void apply(final ConfigurationDefinition definition) {
        definition.apply(this, this.getVersion());
        this.setVersion(definition.getLatest());
    }

}
