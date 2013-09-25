package edgruberman.bukkit.accesscontrol.versioning;

public class IntegerVersionPart extends VersionPart {

    private final Integer value;

    public IntegerVersionPart(final int value) {
        this.value = value;
    }

    @Override
    public int compareTo(final VersionPart other) {
        // differing part types are always earlier
        if (!(other instanceof IntegerVersionPart)) return 1;

        final IntegerVersionPart casted = (IntegerVersionPart) other;
        return this.value.compareTo(casted.value);
    }

    @Override
    public String toReadable() {
        return String.valueOf(this.value);
    }

}

