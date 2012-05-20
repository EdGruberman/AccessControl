package edgruberman.bukkit.accesscontrol;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Collection of other security principals
 */
public final class Group extends Principal {

    Set<Principal> members = new HashSet<Principal>();

    Group(final AccountManager manager, final String name) {
        super(manager, name);
    }

    @Override
    public boolean delete() {
        for (final Principal member : this.members)
            member.removeMembership(this);

        return (this.manager.groups.remove(this) != null);
    }

    public boolean addMember(final Principal principal) {
        if (!this.members.add(principal)) return false;

        principal.addMembership(this);
        return true;
    }

    public boolean removeMember(final Principal principal) {
        if (!this.members.remove(principal)) return false;

        principal.removeMembership(this);
        return true;
    }

    public Set<Principal> getMembers() {
        return Collections.unmodifiableSet(this.members);
    }

    public boolean setDefault(final boolean defaultGroup) {
        if (defaultGroup) {
            if (this.manager.defaultGroups.contains(this)) return false;

            this.manager.defaultGroups.add(this);
            for (final User user : this.manager.temporaryUsers) user.addMembership(this);
            return true;
        }

        if (!this.manager.defaultGroups.contains(this)) return false;

        this.manager.defaultGroups.remove(this);
        for (final User user : this.manager.temporaryUsers) user.removeMembership(this);
        return true;
    }

    boolean isDefault() {
        return this.manager.defaultGroups.contains(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + this.getClass().getName().hashCode();
    }

}
