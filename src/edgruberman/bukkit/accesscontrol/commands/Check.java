package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.User;

public class Check implements CommandExecutor {

    private final Plugin plugin;
    private final AccountManager manager;

    public Check(final Plugin plugin, final AccountManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // usage: /<command> <Permission>[ (<Player>|<Principal> <World>)]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            Main.courier.send(sender, "requires-argument", "<Permission>");
            return false;
        }

        if (sender instanceof ConsoleCommandSender && args.length < 2) {
            Main.courier.send(sender, "requires-argument", "(<Player>|<Principal> <World>)");
            return false;
        }

        if (args.length <= 2) {
            // /<command> <Permission>[ <Player>]
            String target = sender.getName();
            if (args.length >= 2) target = args[1];

            final Player player = this.plugin.getServer().getPlayerExact(target);
            if (player == null) {
                Main.courier.send(sender, "player-not-found", target);
                return true;
            }

            final String permission = args[0];
            final String nature = Main.courier.format(player.isPermissionSet(permission) ? "check.+set" : "check.+default");
            final String value = Main.courier.format(player.hasPermission(permission) ? "check.+true" : "check.+false");
            final String temporary = (this.manager.getUser(target).temporary ? Main.courier.format("check.+temporary") : "");
            Main.courier.send(sender, "check.format", player.getName(), player.getWorld().getName(), nature, permission, value, temporary);
            return true;
        }

        // args.length >= 3
        // /<command> <Permission> <Principal> <World>
        final String permission = args[0];
        final Principal principal = this.manager.getPrincipal(args[1]);
        final String world = args[2];
        String nature = Main.courier.format("check.+not-configured");

        if (principal == null) {
            Main.courier.send(sender, "check.format", args[1], world, nature, permission);
            return true;
        }

        String temporary = "";
        String valueText = "";
        Boolean value = principal.permissions(world).get(permission);
        if (value != null) {
            nature = Main.courier.format("check.+direct");
            valueText = Main.courier.format(value ? "check.+true" : "check.+false");
            if (principal instanceof User && ((User) principal).temporary) temporary = Main.courier.format("check.+temporary");
        } else {
            value = principal.permissionsTotal(world).get(permission);
            if (value != null) {
                nature = Main.courier.format("check.+inherit");
                valueText = Main.courier.format(value ? "check.+true" : "check.+false");
                if (principal instanceof User && ((User) principal).temporary) temporary = Main.courier.format("check.+temporary");
            } else {
                // TODO check for offline player to determine is op, then identify default permission value
            }
        }

        Main.courier.send(sender, "check.format", principal.getName(), world, nature, permission, valueText, temporary);
        return true;
    }

}
