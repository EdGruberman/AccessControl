package edgruberman.bukkit.accesscontrol.versioning;

import java.text.MessageFormat;
import java.util.Arrays;

public final class DevelopmentalStandardVersion extends StandardVersion {

    public static final String READABLE = "{0}.{1}.{2}{3}{4}"; // 0 = major, 1 = minor, 2 = revision, 3 = type, 4 = build

    private final TypeVersionPart type;
    private final IntegerVersionPart build;

    public DevelopmentalStandardVersion(final IntegerVersionPart major, final IntegerVersionPart minor, final IntegerVersionPart revision
            , final TypeVersionPart type, final IntegerVersionPart build) {
        super(Arrays.<VersionPart>asList(major, minor, revision, type, build), major, minor, revision);
        this.type = type;
        this.build = build;
    }

    public TypeVersionPart getType() {
        return this.type;
    }

    public IntegerVersionPart getBuild() {
        return this.build;
    }

    @Override
    public String toReadable() {
        return MessageFormat.format(DevelopmentalStandardVersion.READABLE, this.parts.toArray());
    }

}
