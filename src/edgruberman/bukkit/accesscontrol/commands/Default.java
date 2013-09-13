package edgruberman.bukkit.accesscontrol.commands;

import java.util.List;
import java.util.Locale;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import edgruberman.bukkit.accesscontrol.Main;

public class Default extends TokenizedExecutor {

    private final Server server;

    public Default(final Server server) {
        this.server = server;
    }

    // usage: /<command> permission [player]
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < 1) {
            Main.courier.send(sender, "requires-argument", "permission", 0);
            return false;
        }

        if (args.size() < 2 && !(sender instanceof Player)) {
            Main.courier.send(sender, "requires-argument", "player", 0);
            return false;
        }

        final String name = ( args.size() >= 2 ? args.get(1) : sender.getName() );
        final OfflinePlayer player = this.server.getOfflinePlayer(name);

        final String permission = args.get(0).toLowerCase(Locale.ENGLISH);
        final Permission instance = this.server.getPluginManager().getPermission(permission);
        final PermissionDefault defaultPermission = ( instance != null ? instance.getDefault() : Permission.DEFAULT_PERMISSION );

        final boolean result = defaultPermission.getValue(player.isOp());
        Main.courier.send(sender, "default", permission, player.getName(), result?1:0, defaultPermission.name(), ( instance != null ? 1 : 0 ));

        return true;
    }

}
