package edgruberman.bukkit.accesscontrol;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class AccountManager implements Listener {

    private static Main main = null;

    /**
     * Central AccessControl plugin's AccountManager instance
     *
     * @return main AccountManager; null if plugin is disabled
     */
    public static AccountManager get() {
        if (AccountManager.main == null) return null;

        return AccountManager.main.getAccountManager();
    }

    final Plugin plugin;

    final Map<String, User> users = new HashMap<String, User>();
    final Map<String, Group> groups = new HashMap<String, Group>();
    final Map<String, Group> defaultGroups = new HashMap<String, Group>();

    public AccountManager(final Plugin plugin) {
        this.plugin = plugin;
        if (plugin instanceof Main) AccountManager.main = (Main) plugin;
    }

    public void load(final MemoryConfiguration usersConfig, final MemoryConfiguration groupsConfig) {
        for (final String name : usersConfig.getKeys(false)) {
            final User user = new User(this, usersConfig.getConfigurationSection(name));
            this.users.put(user.getName().toLowerCase(), user);
        }

        for (final String name : groupsConfig.getKeys(false)) {
            final Group group = new Group(this, groupsConfig.getConfigurationSection(name));
            this.groups.put(group.getName().toLowerCase(), group);
            if (group.isDefault()) this.defaultGroups.put(group.getName().toLowerCase(), group);
        }

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    public void unload() {
        for (final User user : this.users.values()) user.detach();
        this.users.clear();

        this.groups.clear();

        HandlerList.unregisterAll(this);
    }

    public ConfigurationSection export() {
        final MemoryConfiguration exported = new MemoryConfiguration();
        exported.options().pathSeparator('|');

        final ConfigurationSection usersConfig = exported.createSection("users");
        for (final User user : this.users.values())
            if (!user.temporary) usersConfig.createSection(user.getName(), user.config.getValues(true));

        final ConfigurationSection groupsConfig = exported.createSection("groups");
        for (final Group group : this.groups.values())
            groupsConfig.createSection(group.getName(), group.config.getValues(true));

        return exported;
    }

    public Map<String, Boolean> defaultPermissions(final String world) {
        if (this.defaultGroups.isEmpty()) return Collections.emptyMap();

        final Map<String, Boolean> defaults = new LinkedHashMap<String, Boolean>();
        for (final Group group : this.defaultGroups.values())
            defaults.putAll(group.permissionsTotal(world));

        // Groups themselves override their children
        for (final Group group : this.defaultGroups.values())
            defaults.put(group.getName(), true);

        return defaults;
    }

    public Principal getPrincipal(final String name) {
        final String lookup = name.toLowerCase();
        Principal principal = this.groups.get(lookup);
        if (principal == null) principal = this.users.get(lookup);
        return principal;
    }

    public Group getGroup(final String name) {
        return this.groups.get(name.toLowerCase());
    }

    public User getUser(final String name) {
        return this.users.get(name.toLowerCase());
    }

    public User createUser(final String name) {
        User user = this.getUser(name);
        if (user != null) return user;


        final MemoryConfiguration config = new MemoryConfiguration();
        config.options().pathSeparator('|');
        user = new User(this, config.createSection(name));
        this.users.put(user.getName().toLowerCase(), user);
        return user;
    }

    public Group createGroup(final String name) {
        Group group = this.getGroup(name);
        if (group != null) return group;

        final MemoryConfiguration config = new MemoryConfiguration();
        config.options().pathSeparator('|');
        group = new Group(this, config.createSection(name));
        this.groups.put(group.getName().toLowerCase(), group);
        return group;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerLoginEvent login) {
        User user = this.getUser(login.getPlayer().getName());
        if (user == null) {
            user = this.createUser(login.getPlayer().getName());
            user.temporary = true;
        }

        user.update(login.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerWorldChange(final PlayerChangedWorldEvent changed) {
        final User user = this.getUser(changed.getPlayer().getName());
        if (user == null) return;

        user.update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent quit) {
        final User user = this.getUser(quit.getPlayer().getName());
        if (user == null) return;

        user.detach();
    }

}
