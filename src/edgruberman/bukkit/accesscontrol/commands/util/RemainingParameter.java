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





    public static class Factory extends Parameter.Factory<RemainingParameter, List<String>, RemainingParameter.Factory> {

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

    }

}
