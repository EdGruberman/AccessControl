package edgruberman.bukkit.accesscontrol.securables;

import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.User;

/**
 *  Relates permissions to a principal.
 */
public class AccessControlEntry {

    private Principal principal;
    private boolean allow;
    private final Set<String> permissions = new HashSet<String>();

    public AccessControlEntry(final Principal principal) {
        this(principal, true);
    }

    public AccessControlEntry(final Principal principal, final boolean allow) {
        this.setPrincipal(principal);
        this.allow = allow;
    }

    public AccessControlEntry(final Principal principal, final boolean allow, final String permission) {
        this(principal, allow);
        this.permissions.add(permission);
    }

    public void setPrincipal(final Principal principal) {
        if (principal == null)
            throw new IllegalArgumentException("principal can not be null");

        this.principal = principal;
    }

    public Principal getPrincipal() {
        return this.principal;
    }

    public void setAllow(final boolean allow) {
        this.allow = allow;
    }

    public Set<String> getPermissions() {
        return this.permissions;
    }

    public boolean appliesTo(final Principal other) {
        if (this.principal instanceof User)
            return this.principal.equals(other);

        if (this.principal instanceof Group)
            return other.inheritedMemberships().contains(this.principal);

        return false;
    }

    public boolean isAllow() {
        return this.allow;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.allow ? 1231 : 1237);
        result = prime * result + this.principal.hashCode();
        result = prime * result + this.permissions.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null) return false;

        if (this.getClass() != other.getClass()) return false;
        final AccessControlEntry that = (AccessControlEntry) other;

        if (this.allow != that.allow) return false;
        if (!this.principal.equals(that.principal)) return false;
        if (!this.permissions.equals(that.permissions)) return false;

        return true;
    }

}
