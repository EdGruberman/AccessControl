package edgruberman.bukkit.accesscontrol.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/** renames an existing key or creates a key with a default value */
public class RenameKeyInstruction extends KeyInstruction {

    public static RenameKeyInstruction create(final Object defaultValue, final String existingKey, final String... path) {
        return new RenameKeyInstruction(defaultValue, existingKey, path);
    }

    protected final Object defaultValue;
    protected final String existingKey;

    public RenameKeyInstruction(final Object defaultValue, final String existingKey, final String... path) {
        super(path);
        this.defaultValue = defaultValue;
        this.existingKey = existingKey;
    }

    @Override
    public boolean conflicts(final FileConfiguration base) {
        return false;
    }

    @Override
    public void apply(final FileConfiguration base) {
        final ConfigurationSection target = this.create(base);

        final boolean exists = target.isSet(this.existingKey);
        final Object value;
        if (exists) {
            value = target.get(this.existingKey);
            target.set(this.existingKey, null);
        } else {
            value = this.defaultValue;
        }

        target.set(this.key, value);
    }

}
