package edgruberman.bukkit.accesscontrol;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public final class AccountManager implements Listener {

    final Plugin plugin;
    final boolean setPlayerName;
    final Pattern groupMatch;
    final String groupBuild;

    final Map<String, User> users = new LinkedHashMap<String, User>();
    final Map<String, Group> groups = new LinkedHashMap<String, Group>();
    final Map<String, Group> defaultGroups = new HashMap<String, Group>();

    public AccountManager(final Plugin plugin, final boolean setPlayerName, final String groupMatch, final String groupBuild, final MemoryConfiguration usersConfig, final MemoryConfiguration groupsConfig) {
        this.plugin = plugin;
        this.setPlayerName = setPlayerName;
        this.groupMatch = Pattern.compile(groupMatch);
        this.groupBuild = groupBuild;

        // users
        for (final String name : usersConfig.getKeys(false)) {
            final User user = new User(this, usersConfig.getConfigurationSection(name));
            this.users.put(user.getName().toLowerCase(), user);
        }

        // groups
        for (final String name : groupsConfig.getKeys(false)) {
            final Group group = new Group(this, groupsConfig.getConfigurationSection(name));
            this.groups.put(group.getName().toLowerCase(), group);
            if (group.isDefault()) this.defaultGroups.put(group.getName().toLowerCase(), group);
        }

        // update existing players
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final User user = (User) this.getPrincipal(player.getName());
            this.register(user);
            user.update(player);
        }
    }

    public void clear() {
        for (final User user : this.users.values()) user.detach();
        this.users.clear();
        this.groups.clear();
    }

    public ConfigurationSection export() {
        final MemoryConfiguration exported = new MemoryConfiguration();
        exported.options().pathSeparator(Main.UNLIKELY);

        final ConfigurationSection usersConfig = exported.createSection("users");
        for (final User user : this.users.values())
            if (user.config.getValues(false).size() > 0) usersConfig.createSection(user.getName(), user.config.getValues(true));

        final ConfigurationSection groupsConfig = exported.createSection("groups");
        for (final Group group : this.groups.values())
            groupsConfig.createSection(group.getName(), group.config.getValues(true));

        return exported;
    }

    /** permissions associated with default groups */
    public Map<String, Boolean> defaultPermissions(final String world) {
        if (this.defaultGroups.isEmpty()) return Collections.emptyMap();

        final Map<String, Boolean> defaults = new LinkedHashMap<String, Boolean>();
        for (final Group group : this.defaultGroups.values())
            defaults.putAll(group.permissionsTotal(world));

        // groups themselves override their children
        for (final Group group : this.defaultGroups.values())
            defaults.put(group.getName(), true);

        return defaults;
    }

    public boolean isGroup(final String name) {
        return this.groupMatch.matcher(name).find();
    }

    public String formatGroup(final String name) {
        return ( this.isGroup(name) ? name : MessageFormat.format(this.groupBuild, name));
    }

    public Principal getPrincipal(final String name) {
        return ( this.isGroup(name) ? this.getGroup(name) : this.getUser(name) );
    }

    public List<Group> getGroups() {
        return new ArrayList<Group>(this.groups.values());
    }

    public Group getGroup(final String name) {
        final Group group = this.groups.get(this.formatGroup(name).toLowerCase());
        if (group != null) return group;

        final MemoryConfiguration groupConfig = new MemoryConfiguration();
        groupConfig.options().pathSeparator(Main.UNLIKELY);
        return new Group(this, groupConfig.createSection(this.formatGroup(name)));
    }

    public List<User> getUsers() {
        return new ArrayList<User>(this.users.values());
    }

    public User getUser(final String name) {
        final User user = this.users.get(name.toLowerCase());
        if (user != null) return user;

        final MemoryConfiguration userConfig = new MemoryConfiguration();
        userConfig.options().pathSeparator(Main.UNLIKELY);
        return new User(this, userConfig.createSection(name));
    }

    /** registered principals are persistent between restarts */
    public boolean isRegistered(final Principal principal) {
        switch (principal.type) {
        case USER:
            return this.users.containsKey(principal.getName().toLowerCase());
        case GROUP:
            return this.groups.containsKey(principal.getName().toLowerCase());
        }
        throw new IllegalStateException("unrecognized Principal.Type: " + principal.type.name());
    }

    public boolean register(final Principal principal) {
        switch (principal.type) {
        case USER:
            return (this.users.put(principal.getName().toLowerCase(), (User) principal) != null);
        case GROUP:
            return (this.groups.put(principal.getName().toLowerCase(), (Group) principal) != null);
        }
        throw new IllegalStateException("unrecognized Principal.Type: " + principal.type.name());
    }

    public boolean deregister(final Principal principal) {
        switch (principal.type) {
        case USER:
            return (this.users.remove(principal.getName().toLowerCase()) != null);
        case GROUP:
            return (this.groups.remove(principal.getName().toLowerCase()) != null);
        }
        throw new IllegalStateException("unrecognized Principal.Type: " + principal.type.name());
    }

    /** configure player permissions when player connects to server */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerLoginEvent login) {
        final User user = (User) this.getPrincipal(login.getPlayer().getName());
        this.register(user);
        user.update(login.getPlayer());
    }

    /** update player permissions when player changes worlds */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerWorldChange(final PlayerChangedWorldEvent changed) {
        this.getPrincipal(changed.getPlayer().getName()).update();
    }

    /** clean-up permission attachment when player leaves server */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent quit) {
        // ignore ghost account quit
        for (final Player player : Bukkit.getOnlinePlayers())
            if (player.getName().equals(quit.getPlayer().getName()))
                return;

        final User user = (User) this.getPrincipal(quit.getPlayer().getName());
        user.detach();
        if (user.config.getValues(false).size() == 0) this.users.remove(user);
    }

}
