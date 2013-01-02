package edgruberman.bukkit.accesscontrol;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
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
    final String groupBuild;
    final Pattern groupMatch;
    final int groupLength;

    final Map<String, User> users = new LinkedHashMap<String, User>();
    final Map<String, Group> groups = new LinkedHashMap<String, Group>();

    final Principal defaults;

    public AccountManager(final Plugin plugin, final String groupBuild, final String groupMatch, final int groupLength) {
        this.plugin = plugin;
        this.groupBuild = groupBuild;
        this.groupMatch = Pattern.compile(groupMatch);
        this.groupLength = groupLength;
        this.defaults = new Principal(null, new HashMap<String, Map<String, Boolean>>(), new HashMap<String, Map<Group, Boolean>>(), this);
    }

    public void load(final MemoryConfiguration usersConfig, final MemoryConfiguration groupsConfig) {
        this.users.clear();
        this.groups.clear();

        // groups - load first so users referencing groups use same group object
        for (final String name : groupsConfig.getKeys(false)) {
            final Group group = Group.fromConfiguration(groupsConfig.getConfigurationSection(name));
            final Group previous = this.groups.put(group.getName().toLowerCase(), group);
            // replace previous references to group by name only with fully configured group object
            if (previous != null)
                for (final Group existing : this.groups.values())
                    for (final String world : existing.memberships.keySet())
                        if (existing.memberships.get(world).containsKey(previous)) {
                            existing.memberships.get(world).remove(previous);
                            existing.memberships.get(world).put(group, true);
                        }
        }

        // users
        for (final String name : usersConfig.getKeys(false)) {
            final User user = User.fromConfiguration(usersConfig.getConfigurationSection(name));
            this.users.put(user.getName().toLowerCase(), user);
        }

        // update existing players
        for (final Player player : Bukkit.getOnlinePlayers())
            this.createUser(player.getName()).update(player);
    }

    public void clear() {
        for (final User user : this.users.values()) user.detach();
        this.users.clear();
        this.groups.clear();
    }

    public Collection<Group> getGroups() {
        return Collections.unmodifiableCollection(this.groups.values());
    }

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(this.users.values());
    }

    /** permissions applicable to a user with no direct permissions */
    public Map<String, Boolean> defaults(final String world, final Map<String, Boolean> append) {
        this.defaults.permissions(world, append);
        append.remove(this.defaults.getName()); // remove default principal self permission
        return append;
    }

    public boolean isGroup(final String name) {
        return this.groupMatch.matcher(name).find();
    }

    public String formatGroup(final String name) {
        if (this.isGroup(name)) return name;
        final int buildLength = MessageFormat.format(this.groupBuild, "").length();
        final int max = Math.min(name.length(), this.groupLength - buildLength);
        final String trimmed = name.substring(0, max);
        return MessageFormat.format(this.groupBuild, trimmed);
    }

    public Principal createPrincipal(final String name) {
        return ( this.isGroup(name) ? this.createGroup(name) : this.createUser(name) );
    }

    /** @return existing Group or newly created Group */
    public Group createGroup(final String name) {
        Group group = this.getGroup(name);
        if (group != null) return group;

        group = new Group(this.formatGroup(name), this);
        this.groups.put(group.name.toLowerCase(), group);
        return group;
    }

    /** @return existing User or newly created User */
    public User createUser(final String name) {
        User user = this.getUser(name);
        if (user != null) return user;

        user = new User(Bukkit.getOfflinePlayer(name).getName(), this);
        this.users.put(user.name.toLowerCase(), user);
        return user;
    }

    /** @return existing Principal or null */
    public Principal getPrincipal(final String name) {
        return ( this.isGroup(name) ? this.getGroup(name) : this.getUser(name) );
    }

    /** @return existing Group or null */
    public Group getGroup(final String name) {
        return this.groups.get(this.formatGroup(name).toLowerCase());
    }

    /** @return existing User or null */
    public User getUser(final String name) {
        return this.users.get(name.toLowerCase());
    }

    public List<Principal> removePrincipal(final Principal principal) {
        if (principal instanceof Group) return this.removeGroup((Group) principal);
        if (principal instanceof User) return this.removeUser((User) principal);
        throw new IllegalArgumentException("unsupported Principal class: " + principal.getClass().getName());
    }

    public List<Principal> removeGroup(final Group group) {
        final List<Principal> principals = new ArrayList<Principal>();
        principals.addAll(this.getGroups());
        principals.addAll(this.getUsers());

        // unset all direct references
        final List<Principal> changed = new ArrayList<Principal>();
        for (final Principal principal : principals)
            for (final String world : principal.worlds())
                if (principal.unsetMembership(group, world) != null)
                    changed.add(principal);

        this.groups.remove(group.getName().toLowerCase());
        return changed;
    }

    public List<Principal> removeUser(final User user) {
        this.users.remove(this);
        return Collections.emptyList();
    }

    /** configure player permissions when player connects to server */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerLoginEvent login) {
        this.createUser(login.getPlayer().getName()).update(login.getPlayer());
    }

    /** update player permissions when player changes worlds */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerWorldChange(final PlayerChangedWorldEvent changed) {
        this.getUser(changed.getPlayer().getName()).update();
    }

    /** clean-up permission attachment when player leaves server */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent quit) {
        // ignore ghost account quit
        if (quit.getPlayer().isOnline()) return;

        final User user = this.getUser(quit.getPlayer().getName());
        user.detach();
        if (!user.hasDirect()) this.users.remove(user);
    }

    public Configuration toConfiguration() {
        final Configuration config = new MemoryConfiguration();
        config.set("users", this.toConfigurationUsers());
        config.set("groups", this.toConfigurationGroups());
        return config;
    }

    public Configuration toConfigurationUsers() {
        final Configuration config = new MemoryConfiguration();
        for (final User user : this.users.values())
            if (user.hasDirect()) // skip saving connected users that have no direct memberships or permissions to save
                config.set(user.getName(), user.toConfiguration());
        return config;
    }

    public Configuration toConfigurationGroups() {
        final Configuration config = new MemoryConfiguration();
        for (final Group group : this.groups.values())
            config.set(group.getName(), group.toConfiguration());
        return config;
    }

}
