package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.Locale;

public class LowerCaseParameter extends Parameter<String> {

    private final Locale locale;

    public LowerCaseParameter(final LowerCaseParameter.Factory factory) {
        super(factory);
        this.locale = factory.locale;
    }

    @Override
    public String parse(final ExecutionRequest request) throws ArgumentContingency {
        final String argument = request.getArgument(this.index);
        if (argument == null) return null;

        return argument.toLowerCase(this.locale);
    }





    public static class Factory extends Parameter.Factory<LowerCaseParameter, String, LowerCaseParameter.Factory> {

        public static LowerCaseParameter.Factory create(final String name) {
            return new LowerCaseParameter.Factory().setName(name).setLocale(Locale.ENGLISH);
        }

        protected Locale locale;

        public LowerCaseParameter.Factory setLocale(final Locale locale) {
            this.locale = locale;
            return this.cast();
        }

        @Override
        public LowerCaseParameter.Factory cast() {
            return this;
        }

        @Override
        public LowerCaseParameter build() {
            return new LowerCaseParameter(this);
        }

    }

}
