package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.Collections;
import java.util.List;

import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public class RemainingParameter extends Parameter<List<String>> {

    public RemainingParameter(final RemainingParameter.Factory factory) {
        super(factory);
    }

    @Override
    public List<String> parse(final ExecutionRequest request) throws ArgumentParseException {
        if (this.index >= request.getArguments().size()) return Collections.emptyList();
        return request.getArguments().subList(this.index, request.getArguments().size());
    }





    public static class Factory extends Parameter.Factory<RemainingParameter, List<String>> {

        public static RemainingParameter.Factory create(final String name, final ConfigurationCourier courier) {
            return new RemainingParameter.Factory(name, courier);
        }

        public Factory(final String name, final ConfigurationCourier courier) {
            super(name, courier);
        }

        @Override
        public RemainingParameter build() {
            return new RemainingParameter(this);
        }

        @Override
        public RemainingParameter.Factory setName(final String name) {
            super.setName(name);
            return this;
        }

        @Override
        public RemainingParameter.Factory setSyntax(final String syntax) {
            super.setSyntax(syntax);
            return this;
        }

        @Override
        public RemainingParameter.Factory setIndex(final int index) {
            super.setIndex(index);
            return this;
        }

        @Override
        public RemainingParameter.Factory setDefaultValue(final List<String> defaultValue) {
            super.setDefaultValue(defaultValue);
            return this;
        }

    }

}
