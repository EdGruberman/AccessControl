package edgruberman.bukkit.accesscontrol.descriptors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;

import edgruberman.bukkit.accesscontrol.Descriptor;

/** always applicable independent of context, no arguments */
public class Server extends Descriptor {

    public static final int ARGUMENT_COUNT = 0;

    protected final Map<String, Boolean> permissions;

    public Server(final Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }

    @Override
    public Map<String, Boolean> permissions(final OfflinePlayer context) {
        return this.permissions;
    }

    @Override
    public Map<String, Boolean> permissions(final List<String> context) {
        return this.permissions;
    }

    @Override
    public Boolean setPermission(final List<String> context, final String permission, final boolean value) {
        return this.permissions.put(permission, value);
    }

    @Override
    public Boolean unsetPermission(final List<String> context, final String permission) {
        return this.permissions.remove(permission);
    }

    @Override
    public Map<String, Object> serialize() {
        return Descriptor.serializePermissions(this.permissions);
    }



    public static class Factory extends Descriptor.Factory {

        @Override
        public Server deserialize(final Map<String, Object> values) {
            final Map<String, Boolean> permissions = Descriptor.Factory.deserializePermissions(values);
            return new Server(permissions);
        }

        @Override
        public Server create() {
            return new Server(new LinkedHashMap<String, Boolean>());
        }

    }


}
