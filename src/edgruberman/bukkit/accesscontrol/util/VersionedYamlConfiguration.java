package edgruberman.bukkit.accesscontrol.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;

/** @version 1.0.0 */
public class VersionedYamlConfiguration extends YamlConfiguration {

    /** first capturing group will be parsed as version */
    public static Pattern VERSION = Pattern.compile("(?:@version )(\\S+)");



    protected Version version = null;

    public Version getVersion() {
        if (this.version == null) this.version = Version.parse(this.findVersion());
        return this.version;
    }

    protected String findVersion() {
        final String header = this.options().header();
        if (header == null) return null;

        final Matcher matcher = VersionedYamlConfiguration.VERSION.matcher(header);
        if (!matcher.matches()) return null;

        return matcher.group();
    }

}
