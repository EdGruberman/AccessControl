package edgruberman.bukkit.accesscontrol.commands.util;

import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public class StringParameter extends Parameter<String> {

    public StringParameter(final StringParameter.Factory factory) {
        super(factory);
    }

    @Override
    public String parse(final ExecutionRequest request) throws ArgumentParseException {
        return request.getArgument(this.index);
    }





    public static class Factory extends Parameter.Factory<StringParameter, String, StringParameter.Factory> {

        public static StringParameter.Factory create(final String name, final ConfigurationCourier courier) {
            return new StringParameter.Factory(name, courier);
        }

        public Factory(final String name, final ConfigurationCourier courier) {
            super(name, courier);
        }

        @Override
        public StringParameter build() {
            return new StringParameter(this);
        }

    }

}
