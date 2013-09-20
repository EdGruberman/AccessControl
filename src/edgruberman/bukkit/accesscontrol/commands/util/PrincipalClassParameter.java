package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.User;

public class PrincipalClassParameter extends LimitedParameter<Class<? extends Principal>> {

    public static final Map<String, Class<? extends Principal>> ACCEPTABLE;

    static {
        final Comparator<String> insensitive = new CaseInsensitiveComparator();
        final Map<String, Class<? extends Principal>> result = new TreeMap<String, Class<? extends Principal>>(insensitive);

        result.put(User.class.getSimpleName().toLowerCase(Locale.ENGLISH), User.class);
        result.put(Group.class.getSimpleName().toLowerCase(Locale.ENGLISH), Group.class);

        ACCEPTABLE = Collections.unmodifiableMap(result);
    }



    public PrincipalClassParameter(final PrincipalClassParameter.Factory factory) {
        super(factory);
    }

    @Override
    public Class<? extends Principal> parseLimited(final ExecutionRequest request) throws ArgumentContingency {
        return PrincipalClassParameter.ACCEPTABLE.get(request.getArgument(this.index));
    }





    public static class Factory extends LimitedParameter.Factory<PrincipalClassParameter, Class<? extends Principal>, PrincipalClassParameter.Factory> {

        public static PrincipalClassParameter.Factory create(final String name) {
            final PrincipalClassParameter.Factory result = new PrincipalClassParameter.Factory().setName(name);
            result.acceptable = PrincipalClassParameter.ACCEPTABLE.keySet();
            return result;
        }

        /** @deprecated unsupported; always set to {@link PrincipalClassParameter#ACCEPTABLE ACCEPTABLE.keySet()} */
        @Override
        @Deprecated
        public PrincipalClassParameter.Factory setAcceptable(final Collection<String> acceptable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrincipalClassParameter.Factory cast() {
            return this;
        }

        @Override
        public PrincipalClassParameter build() {
            return new PrincipalClassParameter(this);
        }

    }





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
