package edgruberman.bukkit.accesscontrol;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class Group extends Principal {

    Group(final AccountManager manager, final ConfigurationSection config) {
        super(manager, config);
        Bukkit.getPluginManager().addPermission(new Permission(this.getName(), PermissionDefault.FALSE));
    }

    @Override
    public void update() {
        for (final Permissible p : Bukkit.getServer().getPluginManager().getPermission(this.getName()).getPermissibles())
            if (p instanceof Player)
                this.manager.getUser(((Player) p).getName()).update();
    }

    public boolean isDefault() {
        return this.config.getBoolean("default");
    }

}
