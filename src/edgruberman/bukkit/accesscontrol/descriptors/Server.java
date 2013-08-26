package edgruberman.bukkit.accesscontrol.descriptors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Descriptor;

/** always applicable independent of context, no arguments */
public class Server extends Descriptor {

    public static final int MINIMUM_ARGUMENTS = 0;

    protected final Map<String, Boolean> permissions;

    public Server(final Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }

    @Override
    public Map<String, Boolean> permissions(final OfflinePlayer context) {
        return this.permissions;
    }

    @Override
    public Map<String, Boolean> permissions(final List<String> context) {
        return this.permissions;
    }

    @Override
    public Boolean setPermission(final List<String> context, final String permission, final boolean value) {
        return this.permissions.put(permission, value);
    }

    @Override
    public Boolean unsetPermission(final List<String> context, final String permission) {
        return this.permissions.remove(permission);
    }

    @Override
    public Map<String, Object> serialize() {
        return Descriptor.serializePermissions(this.permissions);
    }



    public static class Factory extends Descriptor.Factory {

        @Override
        public Server deserialize(final Map<String, Object> values) {
            final Map<String, Boolean> permissions = Descriptor.Factory.deserializePermissions(values);
            return new Server(permissions);
        }

        @Override
        public Server create() {
            return new Server(new LinkedHashMap<String, Boolean>());
        }

    }



    public static class ServerPermissionApplicator implements Listener {

        @EventHandler(priority = EventPriority.LOW) // before most other plugins assess permissions at NORMAL
        public void onPlayerLogin(final PlayerLoginEvent login) {
            Authority.get().createUser(login.getPlayer()).apply(login.getPlayer());
        }

    }

}
