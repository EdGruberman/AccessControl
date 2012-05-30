package edgruberman.bukkit.accesscontrol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

/**
 * Individual player used to apply resultant permissions to attachment
 */
public final class User extends Principal {

    PermissionAttachment attachment = null;
    Set<Group> cachedMemberships = null;

    User(final AccountManager manager, final String name) {
        super(manager, name);
        this.setTemporary(true);
    }

    // TODO isolate membership management functions so temporary/removals don't cause cascading unexpected results
    @Override
    public boolean delete() {
        this.detach();

        for (final Group group : this.memberships) {
            group.members.remove(this);
        }
        this.memberships.clear();

        this.manager.temporaryUsers.remove(this);
        return (this.manager.users.remove(this.name.toLowerCase()) != null);
    }

    @Override
    public boolean addMembership(final Group group) {
        this.setTemporary(false);
        return super.addMembership(group);
    }

    @Override
    public boolean removeMembership(final Group group) {
        if (!super.removeMembership(group)) return false;

        if (this.memberships.size() == 0) this.setTemporary(true);
        return true;
    }

    @Override
    public Set<Group> configuredMemberships() {
        if (this.cachedMemberships == null) this.update();
        return this.cachedMemberships;
    }

    public PermissionAttachment getAttachment() {
        return this.attachment;
    }

    public boolean update() {
        this.cachedMemberships = super.configuredMemberships();

        // TODO ? for each membership, m.directPermissions(world)

        if (this.attachment == null) return true;

        // Calculate permissions that should be applied (exclude unset directives)
        final String world = ((Player) this.attachment.getPermissible()).getWorld().getName();
        final Map<String, Boolean> resultant = new HashMap<String, Boolean>();
        for (final Map.Entry<String, Boolean> p : this.configuredPermissions(world).entrySet())
            if (p.getValue() != null) resultant.put(p.getKey(), p.getValue());

        final Map<String, Boolean> applied = this.attachment.getPermissions();
        if (resultant.equals(applied)) return false;

        for (final String name : applied.keySet())
            this.attachment.unsetPermission(name);

        for (final Map.Entry<String, Boolean> p : resultant.entrySet())
            this.attachment.setPermission(p.getKey(), p.getValue());

        return true;
    }

    void attach(final Player player) {
        if (player == null) throw new IllegalArgumentException("Player can not be null");

        if (this.attachment != null) this.detach();
        this.attachment = player.addAttachment(this.manager.getPlugin());
    }

    void detach() {
        if (this.attachment == null) return;

        this.attachment.remove();
        this.attachment = null;
    }

    boolean setTemporary(final boolean temporary) {
        if (temporary) {
            if (!this.manager.temporaryUsers.add(this)) return false;

            // Remove all existing
            for (final Group group : this.memberships) {
                this.memberships.remove(group);
                group.members.remove(this);
            }

            // Add default groups
            for (final Group group : this.manager.defaultGroups) {
                this.memberships.add(group);
                group.members.add(this);
            }
            return true;
        }

        if (!this.manager.temporaryUsers.remove(this)) return false;

        // Remove default groups
        for (final Group group : this.manager.defaultGroups) {
            this.memberships.remove(group);
            group.members.remove(this);
        }
        return true;
    }

    boolean isTemporary() {
        return this.manager.temporaryUsers.contains(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + this.getClass().getName().hashCode();
    }

}
