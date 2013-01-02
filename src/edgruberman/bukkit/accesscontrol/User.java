package edgruberman.bukkit.accesscontrol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public final class User extends Principal {

    private PermissionAttachment attachment = null;

    User(final String name, final AccountManager manager) {
        this(name, new HashMap<String, Map<String, Boolean>>(), new HashMap<String, Map<Group, Boolean>>(), manager);
    }

    User(final String name, final Map<String, Map<String, Boolean>> permissions, final Map<String, Map<Group, Boolean>> memberships, final AccountManager manager) {
        super(name, permissions, memberships, manager);
    }

    @Override
    public Map<String, Boolean> permissions(final String world, final Map<String, Boolean> append) {
        if (this.hasDirect()) {
            super.permissions(world, append);
        } else {
            this.manager.defaults(world, append);
            append.put(this.name, true); // self overrides all
        }
        return append;
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
        this.attachment = player.addAttachment(this.manager.plugin);
        for (final Map.Entry<String, Boolean> p : this.permissions(player.getWorld().getName()).entrySet())
            this.attachment.setPermission(p.getKey(), p.getValue());
    }

    public void detach() {
        if (this.attachment == null) return;
        this.attachment.remove();
        this.attachment = null;
    }

    @Override
    public List<Principal> delete() {
        this.detach();
        return super.delete();
    }



    protected static User fromConfiguration(final ConfigurationSection config) {
        final String name = config.getName();

        final AccountManager manager = Main.getDefaultAccountManager();
        final Map<String, Map<String, Boolean>> permissions = new HashMap<String, Map<String, Boolean>>();
        final Map<String, Map<Group, Boolean>> memberships = new HashMap<String, Map<Group, Boolean>>();
        Principal.separate(manager, config, permissions, memberships);
        return new User(name, permissions, memberships, manager);
    }

}
