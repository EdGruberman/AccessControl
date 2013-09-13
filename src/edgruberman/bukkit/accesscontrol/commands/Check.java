package edgruberman.bukkit.accesscontrol.commands;

import java.util.List;
import java.util.Locale;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import edgruberman.bukkit.accesscontrol.Main;

public class Check extends TokenizedExecutor {

    private final Server server;

    public Check(final Server server) {
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

        final String permission = args.get(0).toLowerCase(Locale.ENGLISH);
        final String name = ( args.size() >= 2 ? args.get(1) : sender.getName() );
        final Player player = this.server.getPlayer(name);
        if (player == null) {
            Main.courier.send(sender, "unknown-value", "player", name);
            return false;
        }

        String source = null;
        for (final PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (info.getPermission().equals(permission)) {
                source = ( info.getAttachment() != null ? info.getAttachment().getPlugin().getName() : this.server.getName() );
                break;
            }
        }
        if (source == null) source = this.server.getName();

        Main.courier.send(sender, "check", permission, player.getName(), player.hasPermission(permission)?1:0, player.isPermissionSet(permission)?1:0, source);
        return true;
    }

}
