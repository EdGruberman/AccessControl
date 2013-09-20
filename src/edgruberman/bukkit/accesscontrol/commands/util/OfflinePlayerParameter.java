package edgruberman.bukkit.accesscontrol.commands.util;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class OfflinePlayerParameter extends Parameter<OfflinePlayer> {

    private final Server server;

    public OfflinePlayerParameter(final OfflinePlayerParameter.Factory factory) {
        super(factory);
        this.server = factory.server;
    }

    @Override
    protected OfflinePlayer parse(final ExecutionRequest request) throws ArgumentContingency {
        String argument = request.getArgument(this.index);

        if (argument == null) {
            if (this.defaultValue != null) return null;

            if (!(request.getSender() instanceof Player)) throw new MissingArgumentContingency(request, this);
            argument = request.getSender().getName();
        }

        return this.server.getOfflinePlayer(argument);
    }





    public static class Factory extends Parameter.Factory<OfflinePlayerParameter, OfflinePlayer, OfflinePlayerParameter.Factory> {

        public static OfflinePlayerParameter.Factory create(final String name, final Server server) {
            return new OfflinePlayerParameter.Factory().setName(name).setServer(server);
        }

        private Server server;

        public OfflinePlayerParameter.Factory setServer(final Server server) {
            this.server = server;
            return this.cast();
        }

        @Override
        public OfflinePlayerParameter.Factory cast() {
            return this;
        }

        @Override
        public OfflinePlayerParameter build() {
            return new OfflinePlayerParameter(this);
        }

    }

}
