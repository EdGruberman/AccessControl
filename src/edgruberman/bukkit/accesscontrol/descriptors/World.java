package edgruberman.bukkit.accesscontrol.descriptors;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Descriptor;

/** single argument of world name */
public class World extends Descriptor {

    public static final int MINIMUM_ARGUMENTS = 1;

    /** world, permission, value */
    protected Map<String, Map<String, Boolean>> permissions;

    public World(final Map<String, Map<String, Boolean>> worldPermissions) {
        this.permissions = worldPermissions;
    }

    @Override
    public Map<String, Boolean> permissions(final OfflinePlayer context) {
        if (!context.isOnline() || context.getPlayer().getWorld() == null) return Collections.emptyMap();
        final Map<String, Boolean> result = this.permissions.get(context.getPlayer().getWorld().getName().toLowerCase(Locale.ENGLISH));
        return ( result != null ? result : Collections.<String, Boolean>emptyMap() );
    }

    @Override
    public Map<String, Boolean> permissions(final List<String> context) {
        if (context == null || context.size() < World.MINIMUM_ARGUMENTS) return Collections.emptyMap();
        final Map<String, Boolean> result =  this.permissions.get(context.get(0).toLowerCase(Locale.ENGLISH));
        return ( result != null ? result : Collections.<String, Boolean>emptyMap() );
    }

    @Override
    public Boolean setPermission(final List<String> context, final String permission, final boolean value) {
        return this.setPermission(context.get(0), permission, value);
    }

    @Override
    public Boolean unsetPermission(final List<String> context, final String permission) {
        return this.unsetPermission(context.get(0), permission);
    }

    public Boolean setPermission(final String world, final String permission, final boolean value) {
        final String lower = world.toLowerCase(Locale.ENGLISH);
        Map<String, Boolean> permissions = this.permissions.get(lower);
        if (permissions == null) {
            permissions = new LinkedHashMap<String, Boolean>();
            this.permissions.put(lower, permissions);
        }
        return permissions.put(permission, value);
    }

    public Boolean unsetPermission(final String world, final String permission) {
        final String lower = world.toLowerCase(Locale.ENGLISH);
        final Map<String, Boolean> permissions = this.permissions.get(lower);
        if (permissions == null) return null;
        return permissions.remove(permission);
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();

        for (final Map.Entry<String, Map<String, Boolean>> entry : this.permissions.entrySet()) {
            result.put(entry.getKey(), Descriptor.serializePermissions(entry.getValue()));
        }

        return result;
    }



    public static class Factory extends Descriptor.Factory {

        @Override
        public World deserialize(final Map<String, Object> values) {
            final Map<String, Map<String, Boolean>> result = new LinkedHashMap<String, Map<String, Boolean>>();

            for (final String name : values.keySet()) {
                @SuppressWarnings("unchecked")
                final Map<String, Boolean> permissions = Descriptor.Factory.deserializePermissions((Map<String, Object>) values.get(name));
                result.put(name.toLowerCase(Locale.ENGLISH), permissions);
            }

            return new World(result);
        }

        @Override
        public World create() {
            return new World(new LinkedHashMap<String, Map<String, Boolean>>());
        }

    }



    public static class WorldPermissionApplicator implements Listener {

        @EventHandler(priority = EventPriority.LOW) // before most other plugins assess permissions at NORMAL
        public void onPlayerJoin(final PlayerJoinEvent join) {
            Authority.get().createUser(join.getPlayer()).apply(join.getPlayer());
        }

        @EventHandler(priority = EventPriority.LOW) // before most other plugins assess permissions at NORMAL
        public void onPlayerChangedWorld(final PlayerChangedWorldEvent change) {
            Authority.get().createUser(change.getPlayer()).apply(change.getPlayer());
        }

    }

}
