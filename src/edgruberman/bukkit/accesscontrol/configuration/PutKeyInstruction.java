package edgruberman.bukkit.accesscontrol.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/** creates a new value or changes an existing value */
public class PutKeyInstruction extends KeyInstruction {

    public static PutKeyInstruction create(final Object value, final String... path) {
        return new PutKeyInstruction(value, path);
    }

    protected final Object value;

    public PutKeyInstruction(final Object value, final String... path) {
        super(path);
        this.value = value;
    }

    /** @return true when data would be lost if applied to base */
    @Override
    public boolean conflicts(final FileConfiguration base) {
        final ConfigurationSection target = this.target(base);
        if (target == null) return false;

        if (!target.isSet(this.key)) return false;

        final Object existing = target.get(this.key, null);
        if (this.value == null) return (existing != null);

        return !this.value.equals(existing);
    }

    @Override
    public void apply(final FileConfiguration base) {
        final ConfigurationSection target = this.create(base);
        target.set(this.key, this.value);
    }

}
