package edgruberman.bukkit.accesscontrol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/** conditional permissions */
public abstract class Descriptor {

    /**
     * @param context player state
     * @return direct permissions to apply for context, empty map if none
     */
    public abstract Map<String, Boolean> permissions(final OfflinePlayer context);

    /**
     * @param context state represented in command arguments, minimum size for required
     * @return direct permissions to apply for context, empty map if none
     */
    public abstract Map<String, Boolean> permissions(final List<String> context);

    /**
     * @param context state represented in command arguments, minimum size for required
     * @return previous value associated with permission; null if none
     */
    public abstract Boolean setPermission(List<String> context, String permission, boolean value);

    /**
     * @param context state represented in command arguments, minimum size for required
     * @return previous value associated with permission; null if none
     */
    public abstract Boolean unsetPermission(List<String> context, String permission);

    /** prepare for storage in repository */
    public abstract Map<String, Object> serialize();



    /** convenience method to serialize a common permissions map */
    protected static Map<String, Object> serializePermissions(final Map<String, Boolean> permissions) {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (final Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }





    public abstract static class Factory {

        /** deserialization from a repository */
        public abstract Descriptor deserialize(Map<String, Object> serialized);

        /** construct empty descriptor */
        public abstract Descriptor create();

        /** @return command arguments that represent player context */
        public abstract List<String> arguments(Player context);

        /** @return minimum command argument names for context in expected order; empty when none required */
        public abstract List<String> required();



        /** convenience method to deserialize a common permissions map from a repository */
        protected static Map<String, Boolean> deserializePermissions(final Map<String, Object> permissions) {
            final Map<String, Boolean> result = new LinkedHashMap<String, Boolean>();
            for (final Map.Entry<String, Object> entry : permissions.entrySet()) {
                if (entry.getValue() instanceof Boolean) {
                    result.put(entry.getKey(), (Boolean) entry.getValue());
                }
            }
            return result;
        }

    }

}
