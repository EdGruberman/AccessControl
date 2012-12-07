package edgruberman.bukkit.accesscontrol;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class User extends Principal {

    PermissionAttachment attachment = null;

    User(final AccountManager manager, final ConfigurationSection config) {
        super(manager, config, Principal.Type.USER);
    }

    @Override
    public void update() {
        final Player player = Bukkit.getPlayerExact(this.getName());
        if (player == null) {
            this.detach();
            return;
        }

        this.update(player);
    }

    void update(final Player player) {
        this.detach();

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
