package edgruberman.bukkit.accesscontrol;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public final class Group extends Principal {

    private static final boolean DEFAULT_DEFAULT_GRANT = false;
    private static final boolean DEFAULT_PRIVATE_MEMBERS = false;

    private String description;
    private String creator;
    private boolean defaultGrant; // TODO rename to distinguish from permission default?
    private boolean privateMembers;

    /** create Group with default configuration */
    Group(final String name, final AccountManager manager) {
        this(name, new HashMap<String, Map<String, Boolean>>(), new HashMap<String, Map<Group, Boolean>>(), manager, null, null, Group.DEFAULT_DEFAULT_GRANT, Group.DEFAULT_PRIVATE_MEMBERS);
    }

    /** create Group with initial configuration */
    Group(final String name, final Map<String, Map<String, Boolean>> permissions, final Map<String, Map<Group, Boolean>> memberships, final AccountManager manager
            , final String description, final String creator, final boolean defaultGrant, final boolean privateMembers) {
        super(name, permissions, memberships, manager);
        this.setDescription(description);
        this.setCreator(creator);
        this.setDefault(defaultGrant);
        this.setPrivate(privateMembers);
    }

    @Override
    public void update() {
        for (final Permissible p : Bukkit.getPluginManager().getPermission(this.getName()).getPermissibles())
            if (p instanceof Player)
                this.manager.getPrincipal(((Player) p).getName()).update();
    }

    public Permission getBukkitPermission() {
        Permission permission = Bukkit.getPluginManager().getPermission(this.getName());
        if (permission == null) {
            permission = new Permission(this.getName(), PermissionDefault.FALSE);
            Bukkit.getPluginManager().addPermission(permission);
        }
        return permission;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
        this.getBukkitPermission().setDescription(this.description);
    }

    public String getCreator() {
        return this.creator;
    }

    public void setCreator(final String creator) {
        this.creator = creator;
    }

    public boolean isDefault() {
        return this.defaultGrant;
    }

    public boolean setDefault(final boolean defaultGrant) {
        if (this.defaultGrant == defaultGrant) return false;

        this.defaultGrant = defaultGrant;
        if (this.defaultGrant) this.manager.defaults.setMembership(this, true, null);
        return true;
    }

    public boolean isPrivate() {
        return this.privateMembers;
    }

    public boolean setPrivate(final boolean privateMembers) {
        if (this.privateMembers == privateMembers) return false;

        this.privateMembers = privateMembers;
        return true;
    }

    public Boolean setMember(final Principal principal, final boolean value, final String world) {
        return principal.setMembership(this, value, world);
    }

    public Boolean unsetMember(final Principal principal, final String world) {
        return principal.unsetMembership(this, world);
    }

    /** @return true if principal does not depend on membership in another group to be a member of this group; false otherwise */
    public boolean isDirectMember(final Principal principal, final String world) {
        return principal.directMemberships(world).contains(this);
    }

    @Override
    public Configuration toConfiguration() {
        final Configuration config = new MemoryConfiguration();
        config.options().pathSeparator(Main.CONFIG_PATH_SEPARATOR);

        if (this.description != null) config.set("description", this.description);
        if (this.creator != null) config.set("creator", this.creator);
        if (this.defaultGrant != Group.DEFAULT_DEFAULT_GRANT) config.set("default", this.defaultGrant);
        if (this.privateMembers != Group.DEFAULT_PRIVATE_MEMBERS) config.set("private", this.privateMembers);

        final Configuration parent = super.toConfiguration();
        for (final String key : parent.getKeys(false)) config.set(key, parent.get(key));

        return config;
    }



    protected static Group fromConfiguration(final ConfigurationSection config) {
        final String name = config.getName();

        final AccountManager manager = Main.getDefaultAccountManager();
        final Map<String, Map<String, Boolean>> permissions = new HashMap<String, Map<String, Boolean>>();
        final Map<String, Map<Group, Boolean>> memberships = new HashMap<String, Map<Group, Boolean>>();
        Principal.separate(manager, config, permissions, memberships);

        final String creator = config.getString("creator");
        final String description = config.getString("description");
        final boolean defaultGrant = config.getBoolean("default", Group.DEFAULT_DEFAULT_GRANT);
        final boolean privateMembers = config.getBoolean("private", Group.DEFAULT_PRIVATE_MEMBERS);

        return new Group(name, permissions, memberships, manager, description, creator, defaultGrant, privateMembers);
    }

}
