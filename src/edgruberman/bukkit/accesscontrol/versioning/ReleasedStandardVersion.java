package edgruberman.bukkit.accesscontrol.versioning;

import java.text.MessageFormat;
import java.util.Arrays;

public final class ReleasedStandardVersion extends StandardVersion {

    public static final String READABLE = "{0}.{1}.{2}"; // 0 = major, 1 = minor, 2 = revision

    public ReleasedStandardVersion(final IntegerVersionPart major, final IntegerVersionPart minor, final IntegerVersionPart revision) {
        super(Arrays.<VersionPart>asList(major, minor, revision), major, minor, revision);
    }

    @Override
    public String toReadable() {
        return MessageFormat.format(ReleasedStandardVersion.READABLE, this.parts.toArray());
    }

}
