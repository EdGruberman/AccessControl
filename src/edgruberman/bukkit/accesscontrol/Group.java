package edgruberman.bukkit.accesscontrol;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class Group extends Principal {

    Group(final AccountManager manager, final ConfigurationSection config) {
        super(manager, config, Principal.Type.GROUP);
        Permission permission = this.getPermission();
        if (permission == null) {
            permission = new Permission(this.getName(), PermissionDefault.FALSE);
            Bukkit.getPluginManager().addPermission(permission);
        }
        permission.setDescription(this.getDescription());
    }

    @Override
    public void update() {
        for (final Permissible p : Bukkit.getPluginManager().getPermission(this.getName()).getPermissibles())
            if (p instanceof Player)
                this.manager.getPrincipal(((Player) p).getName()).update();
    }

    public Permission getPermission() {
        return Bukkit.getPluginManager().getPermission(this.getName());
    }

    public String getDescription() {
        return this.config.getString("description");
    }

    public void setDescription(final String description) {
        this.config.set("description", description);
        this.getPermission().setDescription(description);
    }

    public boolean isDefault() {
        return this.config.getBoolean("default");
    }

    public boolean isPrivate() {
        return this.config.getBoolean("private");
    }

    public User getCreator() {
        for (final User user : this.manager.getUsers())
            if (user.permissionsServer().containsKey("accesscontrol.creator." + this.getName()))
                return user;

        return null;
    }

    public boolean addMember(final Principal principal, final String world) {
        final Boolean existing = ( world == null ? principal.permissionsServer().get(this.getName()) : principal.permissionsWorld(world).get(this.getName()) );
        if (existing != null && existing) return false;

        principal.setPermission(this.getName(), true, ( world != null ? world : null ));
        if (!this.manager.isRegistered(principal)) this.manager.register(principal);
        principal.update();
        return true;
    }

    public boolean removeMember(final Principal principal, final String world) {
        final Boolean existing = ( world == null ? principal.permissionsServer().get(this.getName()) : principal.permissionsWorld(world).get(this.getName()) );
        if (existing == null || !existing) return false;

        principal.unsetPermission("accesscontrol.operator." + this.getName(), null);
        principal.unsetPermission(this.getName(), ( world != null ? world : null ));
        if (this.manager.isRegistered(principal) && principal.permissionsServer().size() == 0 && principal.worlds().size() == 0) this.manager.deregister(principal);
        principal.update();
        return true;
    }

    public boolean isMember(final CommandSender sender) {
        return sender.hasPermission(this.getName());
    }

    public boolean isMember(final Principal principal, final String world) {
        final Boolean existing = ( world == null ? principal.permissionsServer().get(this.getName()) : principal.permissionsWorld(world).get(this.getName()) );
        return (existing != null && existing);
    }

    public boolean promoteOperator(final Principal principal) {
        if (principal.permissionsServer().get("accesscontrol.operator." + this.getName()) != null) return false;

        principal.setPermission(this.getName(), true, null);
        principal.setPermission("accesscontrol.operator." + this.getName(), true, null);
        if (!this.manager.isRegistered(principal)) this.manager.register(principal);
        principal.update();
        return true;
    }

    public boolean demoteOperator(final Principal principal) {
        if (principal.permissionsServer().get("accesscontrol.operator." + this.getName()) == null) return false;

        principal.unsetPermission("accesscontrol.operator." + this.getName(), null);
        if (this.manager.isRegistered(principal) && principal.permissionsServer().size() == 0 && principal.worlds().size() == 0) this.manager.deregister(principal);
        principal.update();
        return true;
    }

    public boolean isOperator(final Principal principal) {
        return (principal.permissionsServer().containsKey("accesscontrol.operator." + this.getName()));
    }

    public List<Principal> delete() {
        // find (iterate all groups and users) direct references and unset
        final List<Principal> changed = new ArrayList<Principal>();
        final List<Principal> principals = new ArrayList<Principal>();
        principals.addAll(this.manager.getGroups());
        principals.addAll(this.manager.getUsers());
        for (final Principal principal : principals) {
            // remove operators
            if (principal.permissionsServer().containsKey("accesscontrol.operator." + this.getName())) {
                principal.unsetPermission("accesscontrol.operator." + this.getName(), null);
                changed.add(principal);
            }

            // remove creator
            if (principal.permissionsServer().containsKey("accesscontrol.creator." + this.getName())) {
                principal.unsetPermission("accesscontrol.creator." + this.getName(), null);
                changed.add(principal);
            }

            // removes server members
            if (principal.permissionsServer().containsKey(this.getName())) {
                principal.unsetPermission(this.getName(), null);
                changed.add(principal);
            }

            // remove world members
            for (final String world : principal.worlds()) {
                if (principal.permissionsWorld(world).containsKey(this.getName())) {
                    principal.unsetPermission(this.getName(), world);
                    changed.add(principal);
                }
            }
        }
        for (final Principal principal : changed) principal.update();

        this.manager.deregister(this);
        return changed;
    }

}
