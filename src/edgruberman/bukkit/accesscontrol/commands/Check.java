package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import edgruberman.bukkit.accesscontrol.commands.util.ArgumentParseException;
import edgruberman.bukkit.accesscontrol.commands.util.ExecutionRequest;
import edgruberman.bukkit.accesscontrol.commands.util.Executor;
import edgruberman.bukkit.accesscontrol.commands.util.LowerCaseParameter;
import edgruberman.bukkit.accesscontrol.commands.util.OnlinePlayerParameter;
import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public class Check extends Executor {

    private final Server server;
    private final LowerCaseParameter permission;
    private final OnlinePlayerParameter player;

    public Check(final ConfigurationCourier courier, final Server server) {
        super(courier);
        this.server = server;

        this.permission = this.addRequired(LowerCaseParameter.Factory.create("permission", courier));
        this.player = this.addOptional(OnlinePlayerParameter.Factory.create("player", courier, server));
    }

    // usage: /<command> permission [player]
    @Override
    protected boolean execute(final ExecutionRequest request) throws ArgumentParseException {
        final String permission = request.parse(this.permission);
        final Player player = request.parse(this.player);

        String source = null;
        for (final PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (info.getPermission().equals(permission)) {
                source = ( info.getAttachment() != null ? info.getAttachment().getPlugin().getName() : this.server.getName() );
                break;
            }
        }
        if (source == null) source = this.server.getName();

        this.courier.send(request.getSender(), "check", permission, player.getName(), player.hasPermission(permission)?1:0, player.isPermissionSet(permission)?1:0, source);
        return true;
    }

}
