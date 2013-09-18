package edgruberman.bukkit.accesscontrol.commands.util;

import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public class IntegerParameter extends Parameter<Integer> {

    public IntegerParameter(final IntegerParameter.Factory factory) {
        super(factory);
    }

    @Override
    public Integer parse(final ExecutionRequest request) throws ArgumentParseException {
        final String argument = request.getArgument(this.index);
        if (argument == null) return this.defaultValue;

        try {
            return Integer.parseInt(argument);

        } catch (final NumberFormatException e) {
            throw new ArgumentUnknownException(request, this);
        }
    }





    public static class Factory extends Parameter.Factory<IntegerParameter, Integer> {

        public static IntegerParameter.Factory create(final String name, final ConfigurationCourier courier) {
            return new IntegerParameter.Factory(name, courier);
        }

        public Factory(final String name, final ConfigurationCourier courier) {
            super(name, courier);
        }

        @Override
        public IntegerParameter build() {
            return new IntegerParameter(this);
        }

        @Override
        public IntegerParameter.Factory setName(final String name) {
            super.setName(name);
            return this;
        }

        @Override
        public IntegerParameter.Factory setSyntax(final String syntax) {
            super.setSyntax(syntax);
            return this;
        }

        @Override
        public IntegerParameter.Factory setIndex(final int index) {
            super.setIndex(index);
            return this;
        }

        @Override
        public IntegerParameter.Factory setDefaultValue(final Integer defaultValue) {
            super.setDefaultValue(defaultValue);
            return this;
        }

    }

}
