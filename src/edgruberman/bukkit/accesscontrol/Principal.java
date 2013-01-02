package edgruberman.bukkit.accesscontrol;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

public class Principal {

    protected final String name;

    /** &lt;World name (null for server), &lt;Permission name, granted/revoked>> */
    protected final Map<String, Map<String, Boolean>> permissions;

    /** &lt;World name (null for server), &lt;Group, granted/revoked>> */
    protected final Map<String, Map<Group, Boolean>> memberships;

    protected final AccountManager manager;

    Principal(final String name, final Map<String, Map<String, Boolean>> permissions, final Map<String, Map<Group, Boolean>> memberships, final AccountManager manager) {
        this.name = name;
        this.permissions = permissions;
        this.memberships = memberships;
        this.manager = manager;
    }

    /** recalculate associated Permissible permissions */
    public void update() {};

    /**
     * removes Principal from saved configuration
     * @return modified Principals as a result of removal
     */
    public List<Principal> delete() {
        return this.manager.removePrincipal(this);
    }

    public String getName() {
        return this.name;
    }

    /** @return direct permissions for specified world only */
    public Map<String, Boolean> getPermissions(final String world) {
        final Map<String, Boolean> p = this.permissions.get(world);
        if (p == null) return Collections.emptyMap();
        return Collections.unmodifiableMap(p);
    }

    /** @return direct memberships for specified world only */
    public Map<Group, Boolean> getMemberships(final String world) {
        final Map<Group, Boolean> m = this.memberships.get(world);
        if (m == null) return Collections.emptyMap();
        return Collections.unmodifiableMap(m);
    }

    /** @return previous value or null if none */
    public Boolean setPermission(final String permission, final boolean value, final String world) {
        if (permission == null) throw new IllegalArgumentException("permission can not be null");

        if (this.manager.isGroup(permission))
            return this.setMembership(this.manager.createGroup(permission), value, world);

        Map<String, Boolean> permissions = this.permissions.get(world);
        if (permissions == null) {
            permissions = new LinkedHashMap<String, Boolean>();
            this.permissions.put(world, permissions);
        }
        return permissions.put(permission, value);
    }

    /** @return previous value or null if none */
    public Boolean unsetPermission(final String permission, final String world) {
        if (this.manager.isGroup(permission))
            return this.unsetMembership(this.manager.getGroup(permission), world);

        final Map<String, Boolean> permissions = this.permissions.get(world);
        if (permissions == null) return null;
        final Boolean previous = permissions.remove(permission);
        if (permissions.size() == 0) this.permissions.remove(world);
        return previous;
    }

    /** @return previous value or null if none */
    public Boolean setMembership(final Group group, final boolean value, final String world) {
        if (group == null) throw new IllegalArgumentException("group can not be null");

        Map<Group, Boolean> memberships = this.memberships.get(world);
        if (memberships == null) {
            memberships = new LinkedHashMap<Group, Boolean>();
            this.memberships.put(world, memberships);
        }
        return memberships.put(group, value);
    }

    /** @return previous value or null if none */
    public Boolean unsetMembership(final Group group, final String world) {
        final Map<Group, Boolean> memberships = this.memberships.get(world);
        if (memberships == null) return null;
        final Boolean previous = memberships.remove(group);
        if (memberships.size() == 0) this.memberships.remove(world);
        return previous;
    }

    /** @return whether or not any direct memberships or direct permissions are defined */
    public boolean hasDirect() {
        return (this.memberships.size() > 0 || this.permissions.size() > 0);
    }

    /** @return all worlds Principal is configured for either permissions or memberships in */
    public Set<String> worlds() {
        final Set<String> worlds = new HashSet<String>(this.permissions.keySet());
        worlds.addAll(this.memberships.keySet());
        return worlds;
    }

    /** @return combined (server and world) permissions assigned to this principal itself  */
    public Map<String, Boolean> direct(final String world, final Map<String, Boolean> append) {
        // server
        final Map<String, Boolean> directServer = this.permissions.get(null);
        if (directServer != null) append.putAll(directServer);

        // world overrides server
        if (world != null) {
            final Map<String, Boolean> directWorld = this.permissions.get(world);
            if (directWorld != null) append.putAll(directWorld);
        }

        return append;
    }

    /** @return combined (server and world) permissions assigned to this principal itself */
    public Map<String, Boolean> direct(final String world) {
        return this.direct(world, new LinkedHashMap<String, Boolean>());
    }

    /** @return permissions assigned as a result of a group membership (children/closest override parents/farthest) */
    public Map<String, Boolean> inherited(final String world, final Map<String, Boolean> append) {
        for (final Group group : this.directMemberships(world)) group.permissions(world, append);
        return append;
    }

    /** @return permissions assigned as a result of a group membership (children/closest override parents/farthest) */
    public Map<String, Boolean> inherited(final String world) {
        return this.inherited(world, new LinkedHashMap<String, Boolean>());
    }

    /** @return total (inherited server, inherited world, direct server, direct world, and self [listed in order of increasing priority]) permissions */
    public Map<String, Boolean> permissions(final String world, final Map<String, Boolean> append) {
        this.inherited(world, append);
        this.direct(world, append); // direct overrides inherited
        append.put(this.name, true); // self overrides all
        return append;
    }

    /** @return total (inherited server, inherited world, direct server, direct world, and self [listed in order of increasing priority]) permissions */
    public Map<String, Boolean> permissions(final String world) {
        return this.permissions(world, new LinkedHashMap<String, Boolean>());
    }

    /** @return combined (server and world) direct memberships */
    public Set<Group> directMemberships(final String world) {
        final Set<Group> combined = new LinkedHashSet<Group>();

        // server
        final Map<Group, Boolean> serverMemberships = this.memberships.get(null);
        if (serverMemberships != null)
            for (final Map.Entry<Group, Boolean> membership : serverMemberships.entrySet())
                if (membership.getValue())
                    combined.add(membership.getKey());

        // world overrides server
        if (world != null) {
            final Map<Group, Boolean> worldMemberships = this.memberships.get(world);
            if (worldMemberships != null)
                for (final Map.Entry<Group, Boolean> membership : worldMemberships.entrySet())
                    if (membership.getValue()) {
                        combined.add(membership.getKey());
                    } else {
                        combined.remove(membership.getKey());
                    }
        }

        return combined;
    }

    // TODO inheritedMemberships
    // TODO memberships (total)

    @Override
    public String toString() {
        return "Principal[name: " + this.name + "; memberships: [" + this.toString(this.memberships) + "]; permissions: [" + this.toString(this.permissions) + "]]";
    }

    private String toString(final Map<?, ?> map) {
        final StringBuilder sb = new StringBuilder();
        for (final Object world : map.keySet()) {
            sb.append(( world == null ? "server" : world )).append(": [");
            final Iterator<?> it = ((Map<?, ?>) map.get(world)).entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) it.next();
                sb.append(( entry.getKey() instanceof Group ? ((Group) entry.getKey()).getName() : entry.getKey().toString() ));
                sb.append(": ").append(entry.getValue());
                if (it.hasNext()) sb.append(", ");
            }
            sb.append("]");
        }
        return sb.toString();
    }

    public Configuration toConfiguration() {
        final Configuration config = new MemoryConfiguration();
        config.options().pathSeparator(Main.CONFIG_PATH_SEPARATOR);

        for (final Map.Entry<String, Map<Group, Boolean>> world : this.memberships.entrySet()) {
            final String path = ( world.getKey() == null ? "server" : "worlds" + config.options().pathSeparator() + world.getKey() );
            final ConfigurationSection section = ( config.contains(path) ? config.getConfigurationSection(path) : config.createSection(path) );
            for (final Map.Entry<Group, Boolean> membership : world.getValue().entrySet())
                section.set(membership.getKey().getName(), membership.getValue());
        }

        for (final Map.Entry<String, Map<String, Boolean>> world : this.permissions.entrySet()) {
            final String path = ( world.getKey() == null ? "server" : "worlds" + config.options().pathSeparator() + world.getKey() );
            final ConfigurationSection section = ( config.contains(path) ? config.getConfigurationSection(path) : config.createSection(path) );
            for (final Map.Entry<String, Boolean> permission : world.getValue().entrySet())
                section.set(permission.getKey(), permission.getValue());
        }

        return config;
    }



    // TODO builder pattern that only returns a builder for sub-classes to clone and build further and use setPerm/Memb
    protected static void separate(final AccountManager manager, final ConfigurationSection config, final Map<String, Map<String, Boolean>> permissions, final Map<String, Map<Group, Boolean>> memberships) {
        final ConfigurationSection server = config.getConfigurationSection("server");
        if (server != null) Principal.separate(manager, null, server, permissions, memberships);

        final ConfigurationSection worlds = config.getConfigurationSection("worlds");
        if (worlds != null)
            for (final String world : worlds.getKeys(false))
                Principal.separate(manager, world, worlds.getConfigurationSection(world), permissions, memberships);
    }

    private static void separate(final AccountManager manager, final String world, final ConfigurationSection entries, final Map<String, Map<String, Boolean>> permissions, final Map<String, Map<Group, Boolean>> memberships) {
        for (final String key : entries.getKeys(false)) {
            if (manager.isGroup(key)) {
                if (!memberships.containsKey(world)) memberships.put(world, new LinkedHashMap<Group, Boolean>());
                memberships.get(world).put(manager.createGroup(key), (Boolean) entries.get(key));
            } else {
                if (!permissions.containsKey(world)) permissions.put(world, new LinkedHashMap<String, Boolean>());
                permissions.get(world).put(key, (Boolean) entries.get(key));
            }
        }
    }

}
