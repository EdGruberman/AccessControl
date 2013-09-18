package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.User;
import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public class PrincipalClassParameter extends LimitedParameter<Class<? extends Principal>> {

    public static final Map<String, Class<? extends Principal>> ACCEPTABLE = new TreeMap<String, Class<? extends Principal>>(PrincipalClassParameter.CASE_INSENSITIVE_ORDER);

    static {
        PrincipalClassParameter.ACCEPTABLE.put(User.class.getSimpleName().toLowerCase(Locale.ENGLISH), User.class);
        PrincipalClassParameter.ACCEPTABLE.put(Group.class.getSimpleName().toLowerCase(Locale.ENGLISH), Group.class);
    }

    public PrincipalClassParameter(final PrincipalClassParameter.Factory factory) {
        super(factory);
    }

    @Override
    public Class<? extends Principal> doParse(final ExecutionRequest request) throws ArgumentParseException {
        return PrincipalClassParameter.ACCEPTABLE.get(request.getArgument(this.index));
    }





    public static class Factory extends LimitedParameter.Factory<PrincipalClassParameter, Class<? extends Principal>> {

        public static PrincipalClassParameter.Factory create(final String name, final ConfigurationCourier courier) {
            return new PrincipalClassParameter.Factory(name, courier);
        }

        public Factory(final String name, final ConfigurationCourier courier) {
            super(name, courier, PrincipalClassParameter.ACCEPTABLE.keySet());
        }

        @Override
        public PrincipalClassParameter build() {
            return new PrincipalClassParameter(this);
        }

        /** @deprecated unsupported; always set to {@link PrincipalClassParameter#ACCEPTABLE ACCEPTABLE.keySet()} */
        @Override
        @Deprecated
        public PrincipalClassParameter.Factory setAcceptable(final Collection<String> acceptable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrincipalClassParameter.Factory setName(final String name) {
            super.setName(name);
            return this;
        }

        @Override
        public PrincipalClassParameter.Factory setSyntax(final String syntax) {
            super.setSyntax(syntax);
            return this;
        }

        @Override
        public PrincipalClassParameter.Factory setIndex(final int index) {
            super.setIndex(index);
            return this;
        }

        @Override
        public PrincipalClassParameter.Factory setDefaultValue(final Class<? extends Principal> defaultValue) {
            super.setDefaultValue(defaultValue);
            return this;
        }

    }




    private  static final Comparator<String> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();

    /** null friendly implementation modified from {@link String#CASE_INSENSITIVE_ORDER} */
    private static class CaseInsensitiveComparator implements Comparator<String> {

        @Override
        public int compare(final String s1, final String s2) {
            if (s1 == null && s2 == null) return 0;
            if (s1 == null) return -1;
            if (s2 == null) return 1;
            return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
        }

    }

}
