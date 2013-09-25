package edgruberman.bukkit.accesscontrol.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;

import edgruberman.bukkit.accesscontrol.versioning.StandardVersion;
import edgruberman.bukkit.accesscontrol.versioning.Version;

/** @version 1.0.0 */
public class VersionedYamlConfiguration extends YamlConfiguration {

    /** first capturing group is used to parse version */
    public static Pattern VERSION_TAG = Pattern.compile("(?:@version (" + StandardVersion.PARSE_PATTERN.pattern() + "))");



    protected Version version = null;

    /** @return {@link Version#MISSING} when no version tag found */
    public Version getVersion() {
        if (this.version == null) {
            final String found = this.findVersion();
            this.version = ( found != null ? StandardVersion.parse(found) : Version.MISSING );
        }

        return this.version;
    }

    protected String findVersion() {
        final String header = this.options().header();
        if (header == null) return null;

        final Matcher matcher = VersionedYamlConfiguration.VERSION_TAG.matcher(header);
        if (!matcher.matches() || matcher.groupCount() == 0) return null;

        return matcher.group(1);
    }

}
