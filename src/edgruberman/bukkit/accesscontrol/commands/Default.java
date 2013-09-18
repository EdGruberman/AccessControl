package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import edgruberman.bukkit.accesscontrol.commands.util.ArgumentParseException;
import edgruberman.bukkit.accesscontrol.commands.util.ExecutionRequest;
import edgruberman.bukkit.accesscontrol.commands.util.Executor;
import edgruberman.bukkit.accesscontrol.commands.util.LowerCaseParameter;
import edgruberman.bukkit.accesscontrol.commands.util.OfflinePlayerParameter;
import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public class Default extends Executor {

    private final Server server;
    private final LowerCaseParameter permission;
    private final OfflinePlayerParameter player;

    public Default(final ConfigurationCourier courier, final Server server) {
        super(courier);
        this.server = server;

        this.permission = this.addRequired(LowerCaseParameter.Factory.create("permission", courier));
        this.player = this.addOptional(OfflinePlayerParameter.Factory.create("player", courier, server));
    }

    // usage: /<command> permission [player]
    @Override
    protected boolean execute(final ExecutionRequest request) throws ArgumentParseException {
        final String permission = request.parse(this.permission);
        final OfflinePlayer player = request.parse(this.player);

        final Permission instance = this.server.getPluginManager().getPermission(permission);
        final PermissionDefault defaultPermission = ( instance != null ? instance.getDefault() : Permission.DEFAULT_PERMISSION );

        final boolean result = defaultPermission.getValue(player.isOp());
        this.courier.send(request.getSender(), "default", permission, player.getName(), result?1:0, defaultPermission.name(), ( instance != null ? 1 : 0 ));

        return true;
    }

}
