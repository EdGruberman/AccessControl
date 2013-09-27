package edgruberman.bukkit.accesscontrol.configuration;

import org.bukkit.configuration.file.FileConfiguration;

/** puts or removes a header */
public class HeaderInstruction implements ConfigurationInstruction {

    public static HeaderInstruction create(final String value) {
        return new HeaderInstruction(value);
    }

    protected String value;

    public HeaderInstruction(final String value) {
        this.value = value;
    }

    @Override
    public boolean conflicts(final FileConfiguration base) {
        // removing or setting a header that does not exist
        if (base.options().header() == null) return false;

        // existing config has a header and would be removed
        if (this.value == null) return true;

        // existing config has a header and would be changed if different from value
        return !this.value.equals(base.options().header());
    }

    @Override
    public void apply(final FileConfiguration base) {
        base.options().header(this.value);
    }

}
