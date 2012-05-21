package edgruberman.bukkit.accesscontrol.securables;

import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Principal;

/**
 * Grouping of owners and Permission AccessControlEntry instances that can be
 * collectively related to a single resource.
 */
public class AccessControlList {

    private final Set<Principal> owners = new HashSet<Principal>();
    private final Set<AccessControlEntry> entries = new HashSet<AccessControlEntry>();

    /**
     * Creates an empty list with no associated owners or entries.
     */
    public AccessControlList() {}

    /**
     * Returns a list of principals that are listed as owners for this ACL.
     * <p>
     * This is a convenient way to categorize an elevated list of principals
     * that are given special rights.  It is up the implementation to control
     * exactly what rights owners have but the following uses are common:<br/>
     * <br/>
     * - Owners can modify all permissions on the ACL<br/>
     * - Owners have full control to the object ACL is associated to<br/>
     * - Owners are points of contacts for notification/management of object
     * ACL is associated to<br/>
     *
     * @return owners of this list
     */
    public Set<Principal> getOwners() {
        return this.owners;
    }

    /**
     * Determines if principal is an owner or a member of a group that is an
     * owner.
     *
     * @param principal principal to check if it is an owner or not
     * @return true if principal is an owner, otherwise false
     */
    public boolean isOwner(final Principal principal) {
        if (this.owners.contains(principal)) return true;

        if (this.owners.size() == 0) return false;

        for (final Group group : principal.configuredMemberships())
            if (this.owners.contains(group)) return true;

        return false;
    }

    /**
     * Returns the entries associated with this access control list.
     *
     * @return the set of entries associated with this access control list
     */
    public Set<AccessControlEntry> getEntries() {
        return this.entries;
    }

    /**
     * Returns the effective set of allowed permissions for a principal.
     * <p>
     * <strong>
     * Direct overrides Inherited<br/>
     * Deny overrides Allow<br/>
     * (Direct Deny > Direct Allow > Inherited Deny > Inherited Allow)
     * </strong>
     * <p>
     * The effective set of allowed permissions is calculated as follows:
     * <p>
     * If there is no entry in this AccessControlList for the specified
     * principal, an empty permission set is returned.
     * <p>
     * Otherwise, the principal's inherited permission sets are determined. (A
     * principal can belong to one or more groups, where a group is a group of
     * principals, represented by the Group class. An entry whose principal is
     * a group the principal is a member of is considered to contain inherited
     * permissions for the principal.) The inherited allowed permission set is
     * the union of all the allow permissions of each group that the principal
     * belongs to. The inherited denied permission set is the union of all the
     * denied permissions of each group that the principal belongs to.
     * <p>
     * The direct allowed and direct denied permission sets are also determined.
     * The direct allowed permission set contains the permissions specified in
     * any allowed entries for the principal specifically. Similarly, the
     * direct denied permission set contains the permissions specified in
     * any denied entries for the principal specifically.
     * <p>
     * The set of effective permissions granted to the principal is then
     * calculated using two basic rules. Direct permissions always override
     * inherited permissions and denied permissions override allowed
     * permissions within the context direct or inherited. That is, the
     * principal's direct denied permission set overrides the inherited allowed
     * permission set, and the principal's direct allowed permission set
     * overrides the inherited denied permission set.
     *
     * @param principal principal to determine effective permission set for
     * @return effective permission set for principal
     */
    public Set<String> permissionsFor(final Principal principal) {
        final Set<AccessControlEntry> entries = this.entriesFor(principal);
        final Set<String> effective = new HashSet<String>();

        // Add inherited allowed permissions.
        for (final AccessControlEntry ace : entries)
            if (ace.isAllow() && !ace.getPrincipal().equals(principal))
                effective.addAll(ace.getPermissions());

        // Remove inherited denied permission.
        for (final AccessControlEntry ace : entries)
            if (!ace.isAllow() && !ace.getPrincipal().equals(principal))
                effective.removeAll(ace.getPermissions());

        // Add direct allowed permissions.
        for (final AccessControlEntry ace : entries)
            if (ace.isAllow() && ace.getPrincipal().equals(principal))
                effective.addAll(ace.getPermissions());

        // Remove direct denied permission.
        for (final AccessControlEntry ace : entries)
            if (!ace.isAllow() && ace.getPrincipal().equals(principal))
                effective.removeAll(ace.getPermissions());

        return effective;
    }

    /**
     * Determines which entries apply to a specific principal either directly
     * or through inheritance (i.e. If the assigned principal is a group the
     * requested principal is a member of).
     *
     * @param principal principal to return applicable entries for
     * @return applicable entries for principal
     */
    public Set<AccessControlEntry> entriesFor(final Principal principal) {
        final Set<AccessControlEntry> entries = new HashSet<AccessControlEntry>();

        for (final AccessControlEntry ace : this.entries)
            if (ace.appliesTo(principal)) entries.add(ace);

        return entries;
    }

}
