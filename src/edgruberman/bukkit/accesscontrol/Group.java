package edgruberman.bukkit.accesscontrol;

import org.bukkit.configuration.ConfigurationSection;

public class Group extends Principal {

    Group(final AccountManager manager, final ConfigurationSection config) {
        super(manager, config);
    }

    public boolean isDefault() {
        return this.config.getBoolean("default");
    }

}
