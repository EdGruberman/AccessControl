package edgruberman.bukkit.accesscontrol.versioning;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** establishes a comparable software development life-cycle reference */
public final class TypeVersionPart extends StringVersionPart {

    /** @throws IllegalArgumentException if designator not recognized */
    public static TypeVersionPart parse(final String designator) {
        final TypeVersionPart result = TypeVersionPart.KNOWN.get(designator);
        if (result == null) throw new IllegalArgumentException("unrecognized designator: " + designator);
        return result;
    }

    private static Map<String, TypeVersionPart> KNOWN = new HashMap<String, TypeVersionPart>();

    public static Map<String, TypeVersionPart> getKnown() {
        return Collections.unmodifiableMap(TypeVersionPart.KNOWN);
    }

    public static final TypeVersionPart ALPHA = new TypeVersionPart("ALPHA", "a");
    public static final TypeVersionPart BETA = new TypeVersionPart("BETA", "b");
    public static final TypeVersionPart RELEASE_CANDIDATE = new TypeVersionPart("RELEASE_CANDIDATE", "rc");



    private String name;

    private TypeVersionPart(final String name, final String designator) {
        super(designator);
        TypeVersionPart.KNOWN.put(designator, this);
    }

    public String getName() {
        return this.name;
    }

}
