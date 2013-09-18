package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.Locale;

import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public class LowerCaseParameter extends Parameter<String> {

    private final Locale locale;

    public LowerCaseParameter(final LowerCaseParameter.Factory factory) {
        super(factory);
        this.locale = factory.locale;
    }

    @Override
    public String parse(final ExecutionRequest request) throws ArgumentParseException {
        final String argument = request.getArgument(this.index);
        if (argument == null) return argument;
        return argument.toLowerCase(this.locale);
    }





    public static class Factory extends Parameter.Factory<LowerCaseParameter, String> {

        public static LowerCaseParameter.Factory create(final String name, final ConfigurationCourier courier) {
            final LowerCaseParameter.Factory result = new LowerCaseParameter.Factory(name, courier);
            result.setLocale(Locale.ENGLISH);
            return result;
        }

        protected Locale locale;

        public Factory(final String name, final ConfigurationCourier courier) {
            super(name, courier);
        }

        public LowerCaseParameter.Factory setLocale(final Locale locale) {
            this.locale = locale;
            return this;
        }

        @Override
        public LowerCaseParameter build() {
            return new LowerCaseParameter(this);
        }

        @Override
        public LowerCaseParameter.Factory setName(final String name) {
            super.setName(name);
            return this;
        }

        @Override
        public LowerCaseParameter.Factory setSyntax(final String syntax) {
            super.setSyntax(syntax);
            return this;
        }

        @Override
        public LowerCaseParameter.Factory setIndex(final int index) {
            super.setIndex(index);
            return this;
        }

        @Override
        public LowerCaseParameter.Factory setDefaultValue(final String defaultValue) {
            super.setDefaultValue(defaultValue);
            return this;
        }

    }

}
