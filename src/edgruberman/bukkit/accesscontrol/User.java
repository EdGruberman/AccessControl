package edgruberman.bukkit.accesscontrol;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;

import edgruberman.bukkit.accesscontrol.Context.PlayerContext;

public final class User extends Principal {

    private PermissionAttachment attachment = null;

    private User(final String name, final Set<Descriptor> permissions, final Permission implicit) {
        super(name, permissions, implicit);
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(this.name);
    }

    /** default groups are added for default users */
    @Override
    public List<Group> memberships() {
        final List<Group> result = super.memberships();
        if (this.isDefault()) result.addAll(this.authority.getDefaults());
        return result;
    }

    @Override
    public void apply() {
        final OfflinePlayer player = this.getPlayer();
        if (!player.isOnline()) return;
        this.apply(player);
    }

    /** used during login events where the player is not fully online yet */
    public void apply(final OfflinePlayer player) {
        this.implicit.getChildren().clear();
        this.implicit.getChildren().putAll(this.permissions(new PlayerContext(player)));
        this.implicit.getChildren().remove(this.implicit.getName());

        if (this.attachment == null) {
            this.attachment = player.getPlayer().addAttachment(this.authority.getPlugin());
            this.attachment.setPermission(this.implicit, true);
        } else {
            this.implicit.recalculatePermissibles();
        }
    }

    @Override
    public void delete() {
        this.detach();
        super.delete();
    }

    @Override
    public boolean isPersistent() {
        if (this.permissions.size() > 0) return true;
        if (this.memberships.size() > 0) return true;
        return false;
    }

    /** @return true if User only uses default groups */
    public boolean isDefault() {
        return this.memberships.size() == 0 && this.permissions.size() == 0;
    }

    void detach() {
        if (this.attachment == null) return;
        this.attachment.remove();
        this.attachment = null;
    }



    public static class Factory extends Principal.Factory<User.Factory> {

        public static User.Factory of(final Comparator<Descriptor> sorter) {
            return new User.Factory(sorter);
        }

        private Factory(final Comparator<Descriptor> sorter) {
            super(sorter);
        }

        @Override
        public User build() {
            return new User(this.name, this.permissions, this.implicit);
        }

    }

}
