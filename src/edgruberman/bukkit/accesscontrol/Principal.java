package edgruberman.bukkit.accesscontrol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

public class Principal {

    final AccountManager manager;
    final ConfigurationSection config;

    Principal(final AccountManager manager, final ConfigurationSection config) {
        this.manager = manager;
        this.config = config;
    }

    public String getName() {
        return this.config.getName();
    }

    /**
     * Server Direct Permissions
     */
    public Map<String, Boolean> permissionsServer() {
        final ConfigurationSection server = this.config.getConfigurationSection("server");
        if (server == null) return Collections.emptyMap();

        final Map<String, Boolean> permissions = new LinkedHashMap<String, Boolean>();
        for (final String key : server.getKeys(false))
            permissions.put(key.toLowerCase(), server.getBoolean(key));

        return permissions;
    }

    /**
     * World Direct Permissions
     */
    public Map<String, Boolean> permissionsWorld(final String world) {
        final ConfigurationSection worlds = this.config.getConfigurationSection("worlds");
        if (worlds == null) return Collections.emptyMap();

        final ConfigurationSection worldConfig = worlds.getConfigurationSection(world);
        if (worldConfig == null) return Collections.emptyMap();

        final Map<String, Boolean> permissions = new LinkedHashMap<String, Boolean>();
        for (final String key : worldConfig.getKeys(false))
            permissions.put(key.toLowerCase(), worldConfig.getBoolean(key));

        return permissions;
    }

    /**
     * Combined (server and world) Direct Permissions
     */
    public Map<String, Boolean> permissions(final String world) {
        final Map<String, Boolean> permissions = new LinkedHashMap<String, Boolean>();

        permissions.putAll(this.permissionsServer());
        permissions.putAll(this.permissionsWorld(world)); // World overrides Server

        return permissions;
    }

    /**
     * Combined (server and world) Total (direct and inherited) Permissions
     */
    public Map<String, Boolean> permissionsTotal(final String world) {
        final Map<String, Boolean> combined = new LinkedHashMap<String, Boolean>();

        // Inherited (Children override Parents)
        for (final Group group : this.memberships(world))
            combined.putAll(group.permissionsTotal(world));

        // Direct (overrides Inherited)
        combined.putAll(this.permissions(world));

        return combined;
    }

    /**
     * Combined (server and world) Direct Memberships
     */
    public List<Group> memberships(final String world) {
        final Map<String, Boolean> permissions = this.permissions(world);

        final List<Group> groups = new ArrayList<Group>();
        for (final Map.Entry<String, Boolean> p : permissions.entrySet()) {
            if (!p.getValue()) continue;

            final Group group = this.manager.groups.get(p.getKey().toLowerCase());
            if (group == null) continue;

            groups.add(group);
        }

        return groups;
    }

    public void setPermission(final String permission, final boolean value, final String world) {
        this.createSection(world).set(permission, value);
    }

    public void unsetPermission(final String permission, final String world) {
        ConfigurationSection section;
        if (world == null) {
            section = this.config.getConfigurationSection("server");
        } else {
            final ConfigurationSection worlds = this.config.getConfigurationSection("worlds");
            if (worlds == null) return;

            section = worlds.getConfigurationSection(world);
        }
        if (section == null) return;

        section.set(permission, null);

        // TODO if no permissions set for server or any world, delete principal
    }

    private ConfigurationSection createSection(final String world) {
        if (world == null) {
            ConfigurationSection server = this.config.getConfigurationSection("server");
            if (server == null) server = this.config.createSection("server");
            return server;
        }

        ConfigurationSection worlds = this.config.getConfigurationSection("worlds");
        if (worlds == null) worlds = this.config.createSection("worlds");

        ConfigurationSection worldConfig = worlds.getConfigurationSection(world);
        if (worldConfig == null) worldConfig = worlds.createSection(world);

        return worldConfig;
    }

}
