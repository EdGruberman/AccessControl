package edgruberman.bukkit.accesscontrol.configuration;

import org.bukkit.configuration.file.FileConfiguration;

public interface ConfigurationInstruction {

    /** @return true when data would be lost if applied to base */
    boolean conflicts(final FileConfiguration base);

    void apply(final FileConfiguration base);

}
