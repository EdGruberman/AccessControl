package edgruberman.bukkit.accesscontrol.commands.util;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public class OfflinePlayerParameter extends Parameter<OfflinePlayer> {

    private final Server server;

    public OfflinePlayerParameter(final OfflinePlayerParameter.Factory factory) {
        super(factory);
        this.server = factory.server;
    }

    @Override
    public OfflinePlayer parse(final ExecutionRequest request) throws ArgumentParseException {
        String argument = request.getArgument(this.index);

        if (argument == null) {
            if (!(request.getSender() instanceof Player)) throw new ArgumentMissingException(request, this);
            argument = request.getSender().getName();
        }

        return this.server.getOfflinePlayer(argument);
    }





    public static class Factory extends Parameter.Factory<OfflinePlayerParameter, OfflinePlayer> {

        public static OfflinePlayerParameter.Factory create(final String name, final ConfigurationCourier courier, final Server server) {
            final OfflinePlayerParameter.Factory result = new OfflinePlayerParameter.Factory(name, courier);
            result.setServer(server);
            return result;
        }

        private Server server;

        public Factory(final String name, final ConfigurationCourier courier) {
            super(name, courier);
        }

        public OfflinePlayerParameter.Factory setServer(final Server server) {
            this.server = server;
            return this;
        }

        @Override
        public OfflinePlayerParameter build() {
            return new OfflinePlayerParameter(this);
        }

        @Override
        public OfflinePlayerParameter.Factory setName(final String name) {
            super.setName(name);
            return this;
        }

        @Override
        public OfflinePlayerParameter.Factory setSyntax(final String syntax) {
            super.setSyntax(syntax);
            return this;
        }

        @Override
        public OfflinePlayerParameter.Factory setIndex(final int index) {
            super.setIndex(index);
            return this;
        }

        @Override
        public OfflinePlayerParameter.Factory setDefaultValue(final OfflinePlayer defaultValue) {
            super.setDefaultValue(defaultValue);
            return this;
        }

    }

}
