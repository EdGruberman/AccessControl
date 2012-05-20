package edgruberman.bukkit.accesscontrol;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
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
     * This plugin's main AccountManager instance
     *
     * @return main AccountManager; null if plugin is disabled
     */
    public static AccountManager get() {
        if (AccountManager.main == null) return null;

        return AccountManager.main.getAccountManager();
    }

    private final Plugin plugin;

    protected final Map<String, User> users = new HashMap<String, User>();
    protected final Map<String, Group> groups = new HashMap<String, Group>();

    protected final Set<User> temporaryUsers = new HashSet<User>();
    protected final Set<Group> defaultGroups = new HashSet<Group>();

    public AccountManager(final Plugin plugin) {
        this.plugin = plugin;
        if (plugin instanceof Main) AccountManager.main = (Main) plugin;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public void enable() {
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            User user = this.getUser(player.getName());
            if (user == null) {
                user = this.createUser(player.getName());
            }
            user.attach(player);
            user.update();
        }

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    public void disable() {
        HandlerList.unregisterAll(this);

        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            final User user = this.getUser(player.getName());
            user.detach();
            if (user.isTemporary()) user.delete();
        }
    }

    public void save() {
        ((Main) AccountManager.get().plugin).save(AccountManager.get().plugin.getConfig());
    }

    public User getUser(final String name) {
        return this.users.get(name.toLowerCase());
    }

    public User getUser(final Player player) {
        return this.getUser(player.getName());
    }

    public User createUser(final String name) {
        User user = this.users.get(name.toLowerCase());
        if (user != null) return user;

        user = new User(this, name);
        this.users.put(name.toLowerCase(), user);
        return user;
    }

    public User createUser(final Player player) {
        return this.createUser(player.getName());
    }

    public Group getGroup(final String name) {
        return this.groups.get(name.toLowerCase());
    }

    public Group createGroup(final String name) {
        Group group = this.groups.get(name.toLowerCase());
        if (group != null) return group;

        group = new Group(this, name);
        this.groups.put(name.toLowerCase(), group);
        return group;
    }

    public Set<Group> getDefaultGroups() {
        return Collections.unmodifiableSet(this.defaultGroups);
    }

    /**
     * Retrieve account for player or group.
     *
     * @param name formatted name of account
     * @return user or group account
     */
    public Principal getAccount(final String name) {
        if (this.isFormattedGroupName(name))
            return this.getGroup(this.extractName(name));

        return this.getUser(name);
    }

    public String formatName(final Principal principal) {
        if (principal instanceof User)
            return principal.getName();

        if (principal instanceof Group)
            return String.format("[%s]", principal.getName());

        return null;
    }

    public String extractName(final String name) {
        if (!this.isFormattedGroupName(name)) return name;

        return name.substring(1, name.length() - 1);
    }

    public boolean isFormattedGroupName(final String name) {
        if (name == null) return false;

        return name.startsWith("[") && name.endsWith("]");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerLoginEvent login) {
        final User user = this.createUser(login.getPlayer().getName());
        user.attach(login.getPlayer());
        user.update();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerWorldChange(final PlayerChangedWorldEvent changed) {
        this.getUser(changed.getPlayer().getName())
            .update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent quit) {
        final User user = this.getUser(quit.getPlayer().getName());
        user.detach();
        if (user.isTemporary()) user.delete();
    }

}
