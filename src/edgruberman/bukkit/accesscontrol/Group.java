package edgruberman.bukkit.accesscontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public final class Group extends Principal {

    private String description;
    private String creator;

    private Group(final String name, final Set<Descriptor> permissions, final Permission implicit, final String description, final String creator) {
        super(name, permissions, implicit);
        this.description = description;
        this.creator = creator;
    }

    public List<Principal> members() {
        return this.authority.members(this);
    }

    /** apply required after changes finalized */
    public boolean addMember(final Principal member) {
        return member.addMembership(this);
    }

    /** apply required after changes finalized */
    public boolean removeMember(final Principal member) {
        return member.removeMembership(this);
    }

    /**
     * apply required after changes finalized
     * @return removed members
     */
    public List<Principal> resetMembers(final Collection<Principal> members) {
        final List<Principal> result = new ArrayList<Principal>();

        final Collection<Principal> current = this.members();
        for (final Principal existing : current) {
            if (!members.contains(existing)) {
                existing.removeMembership(this);
                result.add(existing);
            }
        }

        for (final Principal member : members) {
            if (!members.contains(member)) {
                member.addMembership(this);
            }
        }

        return result;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public User getCreator() {
        return this.authority.createUser(this.creator);
    }

    public void setCreator(final User creator) {
        this.creator = creator.getName();
    }

    public boolean isDefault() {
        return this.authority.getDefaults().contains(this);
    }

    /** apply required after changes finalized */
    public boolean setDefault(final boolean value) {
        if (value) return this.authority.addDefault(this);
        return this.authority.removeDefault(this);
    }

    @Override
    public void apply() {
        for (final Principal member : this.members()) member.apply();

        if (!this.isDefault()) return;
        for (final Player player : Bukkit.getServer().getOnlinePlayers()) {
            final User user = this.authority.getUser(player);
            if (user.isDefault()) user.apply();
        }
    }

    @Override
    public void delete() {
        for (final Principal member : this.members()) member.removeMembership(this);
        super.delete();
    }

    @Override
    public boolean isPersistent() {
        return this.permissions.size() > 0
                || this.memberships.size() > 0
                || this.members().size() > 0
                || this.creator != null
                || this.description != null
                || this.isDefault();
    }



    public static class Factory extends Principal.Factory<Group.Factory> {

        public static Group.Factory of(final Comparator<Descriptor> sorter) {
            return new Group.Factory(sorter);
        }

        private String description = null;
        private String creator = null;

        private Factory(final Comparator<Descriptor> sorter) {
            super(sorter);
        }

        public Group.Factory setDescription(final String description) {
            this.description = description;
            return this;
        }

        public Group.Factory setCreator(final String creator) {
            this.creator = creator;
            return this;
        }

        @Override
        public Group build() {
            return new Group(this.name, this.permissions, this.implicit, this.description, this.creator);
        }

        @Override
        public void reset() {
            super.reset();
            this.description = null;
            this.creator = null;
        }

    }

}
