package edgruberman.bukkit.accesscontrol.securables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Principal;

/**
 * Boolean access (granted/revoked) management and owners.
 */
public class SimpleAccessControlList extends AccessControlList {

    private final AccountManager manager;

    /**
     * Arbitrary permission name to reference in ACL; null is acceptable.
     */
    private final String permission = null;

    /**
     * Create empty access list that no one has been granted access to.
     */
    public SimpleAccessControlList(final AccountManager manager) {
        this(manager, (List<String>) null, (List<String>) null);
    }

    /**
     * Create access list allowing the specified player access.
     *
     * @param allow player that is granted access
     */
    public SimpleAccessControlList(final AccountManager manager, final Player allow) {
        this(manager, allow.getName());
    }

    /**
     * Create access list allowing the specified player or group access.
     *
     * @param allow player name or formatted group name that is granted access
     */
    public SimpleAccessControlList(final AccountManager manager, final String allow) {
        this(manager, Arrays.asList(allow), (List<String>) null);
    }

    /**
     * Create access list allowing the specified players and/or groups access.
     *
     * @param allow player names and/or formatted group names that are granted
     * access
     */
    public SimpleAccessControlList(final AccountManager manager, final List<String> allow) {
        this(manager, allow, (List<String>) null);
    }

    /**
     * Create access list allowing the specified players or groups access and
     * a another set of players and/or groups as owners.
     *
     * @param allow player names and/or formatted group names that are granted
     * access
     * @param owners player names and/or formatted group names that are owners
     */
    public SimpleAccessControlList(final AccountManager manager, final List<String> allow, final List<String> owners) {
        if (manager == null) throw new IllegalArgumentException("AccountManager can not be null");

        this.manager = manager;

        if (allow != null)
            for (final String name : allow)
                this.grant(name);

        if (owners != null)
            for (final String name : owners)
                this.addOwner(name);
    }

    /**
     * Determines if the player is considered an owner for this access list.
     *
     * @param player player to determine if they are an owner for this access
     * list
     * @return true if player is a direct owner or a member of a group that is
     * an owner
     */
    public boolean isOwner(final Player player) {
        return this.isOwner(this.manager.getUser(player));
    }

    /**
     * Determines if the player or group is considered an owner for this access
     * list.
     *
     * @param name player name or formatted group name to determine if they are
     * an owner for this access list
     * @return true if player/group is a direct owner or member of a group that
     * is an owner
     */
    public boolean isOwner(final String name) {
        return this.isOwner(this.manager.getAccount(name));
    }

    /**
     * Adds a player as a direct owner. (Owners are granted full access.)
     *
     * @param player player to add as owner
     * @return true if player was added, false if player was already a direct
     * owner
     */
    public boolean addOwner(final Player player) {
        return this.addOwner(player.getName());
    }

    /**
     * Adds player or group as a direct owner. (Owners are granted full
     * access.)
     *
     * @param name player name or formatted group name to add as owner
     * @return true if player/group was added, false if they were already a
     * direct owner
     */
    public boolean addOwner(final String name) {
        return this.getOwners().add(this.manager.getAccount(name));
    }

    /**
     * Removes a player from being a direct owner.
     *
     * @param player player to remove from being a direct owner
     * @return true if player was removed, false if player is not a direct
     * owner
     */
    public boolean removeOwner(final Player player) {
        return this.removeOwner(player.getName());
    }

    /**
     * Removes a player or group from being a direct owner.
     *
     * @param name player name or formatted group name to remove from being a
     * direct owner
     * @return true if player/group was removed, false if player
     * or group is not a direct owner
     */
    public boolean removeOwner(final String name) {
        return this.getOwners().remove(this.manager.getAccount(name));
    }

    /**
     * Determines if player is allowed access.
     *
     * @param player player to determine if they have access
     * @return true if player has access, otherwise false
     */
    public boolean isAllowed(final Player player) {
        return this.isAllowed(player.getName());
    }

    /**
     * Determines if player or group is allowed access.
     *
     * @param name player name or formatted group name to determine if they
     * have the permission
     * @return true if player/group has the permission, otherwise false
     */
    public boolean isAllowed(final String name) {
        final Principal principal = this.manager.getAccount(name);

        if (this.isOwner(principal)) return true;

        if (this.permissionsFor(principal).contains(this.permission)) return true;

        return false;
    }

    /**
     * Allows a player access directly.
     *
     * @param player player to grant access to
     * @return true if player was granted access, otherwise false if player
     * already directly had access
     */
    public boolean grant(final Player player) {
        return this.grant(player.getName());
    }

    /**
     * Allows a player or group access directly.
     *
     * @param name player name or formatted group name to grant access to
     * @return true if player/group has access, otherwise false if player
     * already directly had access
     */
    public boolean grant(final String name) {
        final Principal principal = this.manager.getAccount(name);
        final AccessControlEntry ace = new AccessControlEntry(principal, true, this.permission);
        return this.getEntries().add(ace);
    }

    /**
     * Removes a player from having direct access.
     *
     * @param player player to remove direct access from
     * @return true if access was removed, otherwise false if player does
     * not directly have access
     */
    public boolean revoke(final Player player) {
        return this.revoke(player.getName());
    }

    /**
     * Removes a player or group from directly having a specific permission.
     *
     * @param name player name or formatted group name to remove the direct
     * permission from
     * @return true if permission was removed, otherwise false if player/group
     * does not directly have the permission
     */
    public boolean revoke(final String name) {
        final Principal principal = this.manager.getAccount(name);
        final AccessControlEntry ace = new AccessControlEntry(principal, true, this.permission);
        return this.getEntries().remove(ace);
    }

    /**
     * Retrieves a list of direct owner formatted names.
     *
     * @return player names and/or group names that are owners
     */
    public List<String> formatOwners() {
        final List<String> owners = new ArrayList<String>();
        for (final Principal owner : this.getOwners())
            owners.add(this.manager.formatName(owner));

        return owners;
    }

    /**
     * Retrieves a list of players/groups formatted names with direct access.
     *
     * @return player names and/or group names with access
     */
    public List<String> formatAllowed() {
        final List<String> allowed = new ArrayList<String>();
        for (final AccessControlEntry ace : this.getEntries())
            allowed.add(this.manager.formatName(ace.getPrincipal()));

        return allowed;
    }

}
