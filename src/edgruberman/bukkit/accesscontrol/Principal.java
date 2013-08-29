package edgruberman.bukkit.accesscontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.permissions.Permission;

public abstract class Principal {

    protected final String name;
    protected final Set<Descriptor> permissions;
    protected final Permission implicit;

    protected final Set<Group> memberships = new LinkedHashSet<Group>();

    protected Authority authority;

    Principal(final String name, final Set<Descriptor> permissions, final Permission implicit) {
        this.name = name;
        this.permissions = permissions;
        this.implicit = implicit;
    }

    void register(final Authority authority) {
        this.authority = authority;
        this.resetMemberships(authority.memberships(this));
    }

    public String getName() {
        return this.name;
    }

    /** root permission all other permissions are applied to */
    public Permission getImplicit() {
        return this.implicit;
    }

    public Set<Descriptor> getPermissions() {
        return Collections.unmodifiableSet(this.permissions);
    }

    public boolean addPermissions(final Descriptor permission) {
        return this.permissions.add(permission);
    }

    public boolean removePermissions(final Class<? extends Descriptor> type) {
        final Iterator<Descriptor> it = this.permissions.iterator();
        while (it.hasNext()) {
            final Descriptor descriptor = it.next();
            if (!descriptor.getClass().equals(type)) continue;
            it.remove();
            return true;
        }

        return false;
    }

    public void resetPermissions(final Collection<Descriptor> permissions) {
        this.permissions.clear();
        this.permissions.addAll(permissions);
    }

    /** @return descriptor matching specified class; null if none match */
    public Descriptor getPermissions(final Class<? extends Descriptor> type) {
        Descriptor result = null;

        for (final Descriptor descriptor : this.permissions) {
            if (!descriptor.getClass().equals(type)) continue;
            result = descriptor;
            break;
        }

        return result;
    }

    /** direct memberships */
    public Set<Group> getMemberships() {
        return Collections.unmodifiableSet(this.memberships);
    }

    public boolean addMembership(final Group membership) {
        return this.memberships.add(membership);
    }

    public boolean removeMembership(final Group membership) {
        return this.memberships.remove(membership);
    }

    /** clear direct memberships and replace with supplied memberships */
    public void resetMemberships(final Collection<Group> memberships) {
        this.memberships.clear();
        this.memberships.addAll(memberships);
    }

    /** calculate resultant memberships; farthest inherited first to closest inherited, then direct last */
    public List<Group> memberships() {
        final List<Group> result = new ArrayList<Group>();

        // inherited memberships
        for (final Group direct : this.memberships) {
            for (final Group inherited : direct.memberships()) {
                // ensure group is last entry in list to allow for closer group assignments to override by being applied as late as possible
                result.remove(inherited);
                result.add(inherited);
            }
        }

        // direct memberships
        for (final Group direct : this.memberships) {
            if (result.contains(direct)) continue;
            result.add(direct);
        }

        return result;
    }

    /** calculate resultant permissions; farthest inherited first to closest inherited, then direct */
    public Map<String, Boolean> permissions(final Context context) {
        final Map<String, Boolean> result = new LinkedHashMap<String, Boolean>();

        // inherited
        for (final Group inherited : this.memberships()) {
            inherited.permissionsDirect(context, result);
        }

        // direct
        this.permissionsDirect(context, result);

        return result;
    }

    protected void permissionsDirect(final Context context, final Map<String, Boolean> result) {
        // direct
        for (final Descriptor descriptor : this.permissions) {
            result.putAll(context.permissions(descriptor));
        }

        // implicit
        result.put(this.implicit.getName(), true);
    }

    /** calculate assignments related to permission; farthest inherited first to closest inherited, then direct */
    public List<PermissionAssignment> trace(final Context context, final String permission) {
        final List<PermissionAssignment> result = new ArrayList<PermissionAssignment>();

        // inherited
        for (final Group inherited : this.memberships()) {
            inherited.traceDirect(context, permission, result);
        }

        // direct
        this.traceDirect(context, permission, result);

        // implicit
        if (this.implicit.getName().equals(permission)) {
            result.add(new PermissionAssignment(this, true));
        }

        return result;
    }

    protected void traceDirect(final Context context, final String permission, final List<PermissionAssignment> result) {
        for (final Descriptor descriptor : this.permissions) {
            for (final Map.Entry<String, Boolean> entry : context.permissions(descriptor).entrySet()) {
                if (entry.getKey().equals(permission)) {
                    result.add(new PermissionAssignment(this, entry.getValue(), descriptor));
                    break;
                }
            }
        }
    }

    public void delete() {
        this.authority.deletePrincipal(this);
    }

    /** save changes to repository, will delete from repository if not persistent */
    public void save() {
        this.authority.savePrincipal(this);
    }

    /** recalculate and assign permissions for online players for context */
    public abstract void apply();

    /** @return true if this should be saved to repository */
    public abstract boolean isPersistent();



    public static class PermissionAssignment {

        private final Principal source;
        private final Boolean value;
        private final Descriptor descriptor;

        /** implicit assignment */
        PermissionAssignment(final Principal source, final Boolean value) {
            this(source, value, null);
        }

        PermissionAssignment(final Principal source, final Boolean value, final Descriptor descriptor) {
            this.source = source;
            this.value = value;
            this.descriptor = descriptor;
        }

        public Principal getSource() {
            return this.source;
        }

        public Boolean getValue() {
            return this.value;
        }

        public Descriptor getDescriptor() {
            return this.descriptor;
        }

    }



    public abstract static class Factory<F> {

        protected final Set<Descriptor> permissions;

        protected String name = null;
        protected Permission implicit = null;

        protected Factory(final Comparator<Descriptor> sorter) {
            this.permissions = new TreeSet<Descriptor>(sorter);
        }

        @SuppressWarnings("unchecked")
        public F setName(final String name) {
            this.name = name;
            return (F) this;
        }

        @SuppressWarnings("unchecked")
        public F setImplicit(final Permission implicit) {
            this.implicit = implicit;
            return (F) this;
        }

        @SuppressWarnings("unchecked")
        public F addPermissions(final Collection<Descriptor> descriptors) {
            this.permissions.addAll(descriptors);
            return (F) this;
        }

        public abstract Principal build();

        /** reverts factory to defaults */
        public void reset() {
            this.name = null;
            this.implicit = null;
            this.permissions.clear();
        }

    }

}
