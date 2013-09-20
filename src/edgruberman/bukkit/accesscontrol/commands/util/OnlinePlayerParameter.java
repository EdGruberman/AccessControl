package edgruberman.bukkit.accesscontrol.commands.util;

import org.bukkit.Server;
import org.bukkit.entity.Player;

public class OnlinePlayerParameter extends Parameter<Player> {

    private final Server server;

    public OnlinePlayerParameter(final OnlinePlayerParameter.Factory factory) {
        super(factory);
        this.server = factory.server;
    }

    @Override
    public Player parse(final ExecutionRequest request) throws ArgumentContingency {
        String argument = request.getArgument(this.begin);

        if (argument == null) {
            if (!(request.getSender() instanceof Player)) throw new MissingArgumentContingency(request, this);
            argument = request.getSender().getName();
        }

        final Player result = this.server.getPlayer(argument);
        if (result == null) throw new UnknownArgumentContingency(request, this);

        return result;
    }





    public static class Factory extends Parameter.Factory<OnlinePlayerParameter, Player, OnlinePlayerParameter.Factory> {

        public static OnlinePlayerParameter.Factory create(final String name, final Server server) {
            return new OnlinePlayerParameter.Factory().setName(name).setServer(server);
        }

        private Server server;

        public OnlinePlayerParameter.Factory setServer(final Server server) {
            this.server = server;
            return this;
        }

        @Override
        public OnlinePlayerParameter build() {
            return new OnlinePlayerParameter(this);
        }

        @Override
        public OnlinePlayerParameter.Factory cast() {
            return this;
        }

    }

}
