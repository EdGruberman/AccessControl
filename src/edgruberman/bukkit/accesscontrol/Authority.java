package edgruberman.bukkit.accesscontrol;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.accesscontrol.Repository.GroupReference;
import edgruberman.bukkit.accesscontrol.Repository.PrincipalReference;

public final class Authority implements Listener {

    public static Authority get() {
        return Main.getAuthority();
    }



    private final Main plugin;
    private final Repository repository;
    private final Comparator<Descriptor> sorter;
    private final String implicitUser;
    private final String implicitGroup;

    private final Map<String, User> users = new HashMap<String, User>();
    private final Map<String, Group> groups = new HashMap<String, Group>();
    final Set<Group> defaults = new HashSet<Group>();

    Authority(final Main plugin, final Repository repository, final Comparator<Descriptor> sorter, final String implicitUser, final String implicitGroup, final List<String> defaultGroups) {
        this.plugin = plugin;
        this.repository = repository;
        this.sorter = sorter;
        this.implicitUser = implicitUser;
        this.implicitGroup = implicitGroup;
        for (final String group : defaultGroups) this.createGroup(group).setDefault(true);
        for (final Player player : Bukkit.getOnlinePlayers()) this.getUser(player).apply();
    }

    Plugin getPlugin() {
        return this.plugin;
    }

    boolean addDefault(final Group group) {
        return this.defaults.add(group);
    }

    boolean removeDefault(final Group group) {
        return this.defaults.remove(group);
    }

    void release() {
        this.repository.release();
    }

    List<Principal> members(final Group group) {
        final List<Principal> result = new ArrayList<Principal>();

        for (final PrincipalReference reference : this.repository.members(group)) {
            Principal principal = null;
            if (reference.type.equals(User.class)) principal = this.getUser(reference.name);
            if (reference.type.equals(Group.class)) principal = this.getGroup(reference.name);
            if (principal != null) result.add(principal);
        }

        return result;
    }

    List<Group> memberships(final Principal principal) {
        final List<Group> result = new ArrayList<Group>();

        for (final GroupReference reference : this.repository.memberships(principal)) {
            final Group group = this.getGroup(reference.name);
            if (group != null) result.add(group);
        }

        return result;
    }

    private Permission createImplicit(final String format, final String name) {
        final String formatted = MessageFormat.format(format, name);

        final PluginManager pm = this.plugin.getServer().getPluginManager();
        Permission result = pm.getPermission(formatted);
        if (result != null)  return result;

        result = new Permission(formatted, PermissionDefault.FALSE);
        pm.addPermission(result);

        return result;
    }

    public Set<Group> getDefaults() {
        return Collections.unmodifiableSet(this.defaults);
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(new ArrayList<User>(this.users.values()));
    }

    public List<Group> getGroups() {
        return Collections.unmodifiableList(new ArrayList<Group>(this.groups.values()));
    }

    public User getUser(final Player player) {
        return this.getUser(player.getName().toLowerCase(Locale.ENGLISH));
    }

    public User getUser(final String name) {
        User result = this.users.get(name);
        if (result != null) return result;

        result = this.repository.loadUser(name);
        if (result == null) return result;

        result.register(this);
        this.users.put(result.getName(), result);
        return result;
    }

    public Group getGroup(final String name) {
        Group result = this.groups.get(name);
        if (result != null) return result;

        result = this.repository.loadGroup(name);
        if (result == null) return result;

        result.register(this);
        this.groups.put(result.getName(), result);
        return result;
    }

    public User createUser(final String name) {
        User result = this.getUser(name);
        if (result != null) return result;

        result = User.Factory.of(this.sorter)
                .setName(name)
                .setImplicit(this.createImplicit(this.implicitUser, name))
                .build();

        result.register(this);
        this.users.put(result.getName(), result);
        return result;
    }

    public Group createGroup(final String name) {
        Group result = this.getGroup(name);
        if (result != null) return result;

        result = Group.Factory.of(this.sorter)
                .setName(name)
                .setImplicit(this.createImplicit(this.implicitGroup, name))
                .build();

        result.register(this);
        this.groups.put(result.getName(), result);
        return result;
    }

    void deletePrincipal(final Principal principal) {
        final boolean group = principal.getClass().equals(Group.class);
        if (group) this.defaults.remove(principal);
        ( group ? this.groups : this.users ).remove(principal);
        this.repository.delete(principal);
    }

    void savePrincipal(final Principal principal) {
        if (!principal.isPersistent()) {
            this.deletePrincipal(principal);
        } else {
            this.repository.save(principal);
        }
    }

    @EventHandler(priority = EventPriority.LOW) // before most others to have permissions assigned
    public void onPlayerLogin(final PlayerLoginEvent login) {
        final String name = login.getPlayer().getName().toLowerCase(Locale.ENGLISH);
        this.createUser(name).apply();
    }

    /** clean-up permission attachment when player leaves server */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent quit) {
        final String name = quit.getPlayer().getName().toLowerCase(Locale.ENGLISH);
        final User user = this.getUser(name);
        user.detach();
    }

}
