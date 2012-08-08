package edgruberman.bukkit.accesscontrol;

import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class User extends Principal {

    public boolean temporary = false;
    PermissionAttachment attachment = null;

    User(final AccountManager manager, final ConfigurationSection config) {
        super(manager, config);
    }

    @Override
    public void setPermission(final String permission, final boolean value, final String world) {
        this.temporary = false;
        super.setPermission(permission, value, world);
    }

    @Override
    public void unsetPermission(final String permission, final String world) {
        super.unsetPermission(permission, world);
        if (this.config.getKeys(false).size() == 0) this.temporary = true;
    }

    @Override
    public void update() {
        this.update(this.manager.plugin.getServer().getPlayerExact(this.getName()));
    }

    void update(final Player player) {
        this.detach();
        if (player == null) return;

        final Map<String, Boolean> permissions = this.permissionsTotal(player.getWorld().getName());
        if (permissions.size() == 0) permissions.putAll(this.manager.defaultPermissions(player.getWorld().getName()));

        this.attachment = player.addAttachment(this.manager.plugin);
        for (final Map.Entry<String, Boolean> p : permissions.entrySet())
            this.attachment.setPermission(p.getKey(), p.getValue());

        if (this.manager.setPlayerName) this.attachment.setPermission(player.getName(), true);
    }

    public void detach() {
        if (this.attachment == null) return;

        this.attachment.remove();
        this.attachment = null;
    }

}
