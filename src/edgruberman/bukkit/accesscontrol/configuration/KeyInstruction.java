package edgruberman.bukkit.accesscontrol.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

public abstract class KeyInstruction implements ConfigurationInstruction {

    protected final List<String> path;
    protected final String key;

    public KeyInstruction(final String... path) {
        final int last = path.length - 1;
        this.path = ( path.length > 1 ? Arrays.asList(path).subList(0, last) : Collections.<String>emptyList() );
        this.key = path[last];
    }

    /** @return section this instruction points to; null if it does not exist */
    protected ConfigurationSection target(final ConfigurationSection base) {
        ConfigurationSection result = base;

        for (final String p : this.path) {
            result = result.getConfigurationSection(p);
            if (result == null) return null;
        }

        return result;
    }

    /** @return section this instruction points to; newly created if necessary */
    protected ConfigurationSection create(final ConfigurationSection base) {
        ConfigurationSection result = base;

        for (final String p : this.path) {
            if (result.isConfigurationSection(p)) {
                result = result.getConfigurationSection(p);
            } else {
                result = result.createSection(p);
            }
        }

        return result;
    }

}
