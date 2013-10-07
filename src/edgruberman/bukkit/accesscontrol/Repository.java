package edgruberman.bukkit.accesscontrol;

import java.util.List;

public interface Repository {

    /** @return User for name; null if not found */
    User loadUser(String name);

    /** @return Group for name; null if not found */
    Group loadGroup(String name);

    /** replaces existing Principal */
    void save(Principal principal);

    /** delete Group associated with name */
    void delete(Principal principal);

    /** @return groups with direct membership for principal; empty if none found */
    List<GroupReference> memberships(Principal principal);

    /** @return principals with a direct membership in group; empty if none found */
    List<PrincipalReference> members(Group group);

    /** prepare repository for garbage collection */
    void release();



    /** used to avoid endless recursion */
    public static class PrincipalReference {

        public final Class<? extends Principal> type;
        public final String name;

        public PrincipalReference(final Class<? extends Principal> type, final String name) {
            this.type = type;
            this.name = name;
        }

    }

    public static class GroupReference extends PrincipalReference {

        public GroupReference(final String name) {
            super(Group.class, name);
        }

    }

}
