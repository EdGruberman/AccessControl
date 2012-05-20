package edgruberman.bukkit.accesscontrol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class Principal {

    final AccountManager manager;
    final String name;
    final Map<String, Map<String, Boolean>> permissions = new HashMap<String, Map<String, Boolean>>();
    final Set<Group> memberships = new HashSet<Group>();

    Principal(final AccountManager manager, final String name) {
        this.manager = manager;
        this.name = name;
    }

    public abstract boolean delete();

    public String getName() {
        return this.name;
    }

    public boolean addPermission(final String name, final Boolean value) {
        return this.addPermission(name, value, null);
    }

    public boolean addPermission(String name, final Boolean value, String world) {
        if (world != null) world = world.toLowerCase();
        if (!this.permissions.containsKey(world)) this.permissions.put(world, new HashMap<String, Boolean>());

        name = name.toLowerCase();
        final Map<String, Boolean> permissions = this.permissions.get(world);
        if (permissions.containsKey(name)) {
            final Boolean existing = permissions.get(name);
            if (existing == null) {
                if (value == null) return false;
            } else {
                if (existing.equals(value)) return false;
            }
        }

        permissions.put(name, value);
        return true;
    }

    public boolean removePermission(final String name) {
        return this.removePermission(name, null);
    }

    public boolean removePermission(String name, String world) {
        if (world != null) world = world.toLowerCase();
        final Map<String, Boolean> permissions = this.permissions.get(world);
        if (permissions == null) return false;

        name = name.toLowerCase();
        if (!permissions.containsKey(name)) return false;

        permissions.remove(name);
        return true;
    }

    public Map<String, Map<String, Boolean>> getPermissions() {
        return this.permissions;
    }

    // Direct Server and Direct World permissions (Unset directives included)
    public Map<String, Boolean> directPermissions(final String world) {
        final Map<String, Boolean> direct = new HashMap<String, Boolean>();

        final Map<String, Boolean> serverPermissions = this.permissions.get(null);
        if (serverPermissions != null) direct.putAll(serverPermissions);

        if (world != null) {
            final Map<String, Boolean> worldPermissions = this.permissions.get(world.toLowerCase());
            if (worldPermissions != null) direct.putAll(worldPermissions);
        }

        return direct;
    }

    // Inherited Server, Inherited World, Direct Server, and Direct World permissions (Unset directives included)
    public Map<String, Boolean> configuredPermissions(final String world) {
        final Map<String, Boolean> inherited = new HashMap<String, Boolean>();

        // Apply permission from root first, then override as we move down inheritance chain
        for (final Group group : this.memberships)
            inherited.putAll(group.configuredPermissions(world));

        inherited.putAll(this.directPermissions(world));
        return inherited;
    }

    public boolean addMembership(final Group group) {
        if (!this.memberships.add(group)) return false;

        group.addMember(this);
        return true;
    }

    public boolean addMemberships(final Set<Group> groups) {
        boolean changed = false;
        for (final Group group : groups)
            if (this.addMembership(group))
                changed = true;

        if (!changed) return false;

        return true;
    }

    public boolean removeMembership(final Group group) {
        if (!this.memberships.remove(group)) return false;

        group.removeMember(this);
        return true;
    }

    public boolean removeMemberships(final Set<Group> groups) {
        boolean changed = false;
        for (final Group group : groups)
            if (this.removeMembership(group))
                changed = true;

        if (!changed) return false;

        return true;
    }

    public Set<Group> getMemberships() {
        return this.memberships;
    }

    public Set<Group> inheritedMemberships() {
        final Set<Group> inherited = new LinkedHashSet<Group>();

        for (final Group group : this.memberships)
            inherited.addAll(group.inheritedMemberships());

        inherited.addAll(this.memberships);
        return inherited;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (this.getClass() != other.getClass()) return false;
        final Principal that = (Principal) other;
        if (this.name == null) {
            if (that.name != null) return false;
        } else if (!this.name.equals(that.name)) return false;
        return true;
    }

}
