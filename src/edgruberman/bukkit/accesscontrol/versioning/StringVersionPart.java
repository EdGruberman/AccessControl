package edgruberman.bukkit.accesscontrol.versioning;

public class StringVersionPart extends VersionPart {

    private final String value;

    public StringVersionPart(final String value) {
        this.value = value;
    }

    @Override
    public int compareTo(final VersionPart other) {
        // differing part types are always later
        if (!(other instanceof StringVersionPart)) return -1;

        final StringVersionPart casted = (StringVersionPart) other;
        return this.value.compareTo(casted.value);
    }

    @Override
    public String toReadable() {
        return String.valueOf(this.value);
    }

}
