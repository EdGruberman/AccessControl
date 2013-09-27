package edgruberman.bukkit.accesscontrol.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/** deletes a key and any associated values */
public class RemoveKeyInstruction extends KeyInstruction {

    public static RemoveKeyInstruction create(final String... path) {
        return new RemoveKeyInstruction(path);
    }

    public RemoveKeyInstruction(final String... path) {
        super(path);
    }

    @Override
    public boolean conflicts(final FileConfiguration base) {
        final ConfigurationSection target = this.target(base);
        if (target == null) return false;

        return target.isSet(this.key);
    }

    @Override
    public void apply(final FileConfiguration base) {
        final ConfigurationSection target = this.target(base);
        if (target == null) return;

        target.set(this.key, null);
    }

}
