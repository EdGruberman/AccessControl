package edgruberman.bukkit.accesscontrol.versioning;

import java.util.Collections;

/**
 * null object pattern implementation
 *
 * @see {@link Version#MISSING}
 */
public class MissingVersion extends Version {

    MissingVersion() {
        super(Collections.<VersionPart>emptyList());
    }

    @Override
    public String toReadable() {
        return "";
    }

}
