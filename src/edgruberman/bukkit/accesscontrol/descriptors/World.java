package edgruberman.bukkit.accesscontrol.descriptors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Descriptor;

public class World extends Descriptor {

    public static final List<String> REQUIRED = Arrays.asList("world");

    /** world, permission, value */
    protected Map<String, Map<String, Boolean>> permissions;

    public World(final Map<String, Map<String, Boolean>> worldPermissions) {
        this.permissions = worldPermissions;
    }

    // ---- permissions ----

    @Override
    public Map<String, Boolean> permissions(final OfflinePlayer context) {
        if (!context.isOnline() || context.getPlayer().getWorld() == null) return Collections.emptyMap();
        return this.permissions(context.getPlayer().getWorld().getName());
    }

    @Override
    public Map<String, Boolean> permissions(final List<String> context) {
        return this.permissions(context.get(0));
    }

    public Map<String, Boolean> permissions(final org.bukkit.World world) {
        return this.permissions(world.getName());
    }

    public Map<String, Boolean> permissions(final String world) {
        final Map<String, Boolean> result =  this.permissions.get(world.toLowerCase(Locale.ENGLISH));
        if (result == null) return Collections.emptyMap();
        return result;
    }

    // ---- set ----

    @Override
    public Boolean setPermission(final List<String> context, final String permission, final boolean value) {
        return this.setPermission(context.get(0), permission, value);
    }

    public Boolean setPermission(final org.bukkit.World world, final String permission, final boolean value) {
        return this.setPermission(world.getName(), permission, value);
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

    // ---- unset ----

    @Override
    public Boolean unsetPermission(final List<String> context, final String permission) {
        return this.unsetPermission(context.get(0), permission);
    }

    public Boolean unsetPermission(final org.bukkit.World world, final String permission) {
        return this.unsetPermission(world.getName(), permission);
    }

    public Boolean unsetPermission(final String world, final String permission) {
        final String lower = world.toLowerCase(Locale.ENGLISH);
        final Map<String, Boolean> permissions = this.permissions.get(lower);
        if (permissions == null) return null;
        return permissions.remove(permission);
    }

    // ---- serialize ----

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();

        for (final Map.Entry<String, Map<String, Boolean>> entry : this.permissions.entrySet()) {
            final String world = entry.getKey();
            final Map<String, Object> worldPermissions = Descriptor.serializePermissions(entry.getValue());
            result.put(world, worldPermissions);
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

        @Override
        public List<String> arguments(final Player context) {
            final List<String> result = new ArrayList<String>();
            result.add(context.getWorld().getName());
            return result;
        }

        @Override
        public List<String> required() {
            return World.REQUIRED;
        }

    }



    public static class PermissionApplicator implements Listener {

        private final Authority authority;

        public PermissionApplicator(final Authority authority) {
            this.authority = authority;
        }

        @EventHandler(priority = EventPriority.LOW) // before most other plugins assess permissions at NORMAL
        public void onPlayerJoin(final PlayerJoinEvent join) {
            this.authority.createUser(join.getPlayer()).apply(join.getPlayer());
        }

        @EventHandler(priority = EventPriority.LOW) // before most other plugins assess permissions at NORMAL
        public void onPlayerChangedWorld(final PlayerChangedWorldEvent change) {
            this.authority.createUser(change.getPlayer()).apply(change.getPlayer());
        }

    }

}
