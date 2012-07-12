package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.User;

public class Check implements CommandExecutor {

    private final Plugin plugin;
    private final AccountManager manager;

    public Check(final JavaPlugin plugin, final String label, final AccountManager manager) {
        this.plugin = plugin;
        final PluginCommand command = plugin.getCommand(label);
        command.setExecutor(this);
        this.manager = manager;
    }

    // usage: /<command> <Permission>[ (<Player>|<Principal> <World>)]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Syntax error; Missing <Permission> argument");
            return false;
        }

        if (sender instanceof ConsoleCommandSender && args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Syntax error; <Player> argument required from console");
            return false;
        }

        if (args.length <= 2) {
            // /<command> <Permission>[ <Player>]
            String target = sender.getName();
            if (args.length >= 2) target = args[1];

            final Player player = this.plugin.getServer().getPlayerExact(target);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + target);
                return true;
            }

            final String permission = args[0];

            final String nature = player.isPermissionSet(permission) ? ChatColor.BLUE + "sets" : ChatColor.LIGHT_PURPLE + "defaults";
            final String value = player.hasPermission(permission) ? ChatColor.GREEN + "true" : ChatColor.RED + "false";
            final String temporary = (this.manager.getUser(target).temporary ? ChatColor.GRAY + " temporarily" : "");

            sender.sendMessage(ChatColor.WHITE + player.getName() + "@" + player.getWorld().getName() + " effectively " + nature + " " + ChatColor.WHITE + permission + " " + value + temporary);
            return true;
        }

        // args.length >= 3
        // /<command> <Permission> <Principal> <World>
        final String permission = args[0];
        final Principal principal = this.manager.getPrincipal(args[1]);
        final String world = args[2];

        if (principal == null) {
            sender.sendMessage(ChatColor.WHITE + args[1] + "@" + world + ChatColor.DARK_GRAY + " does not configure " + ChatColor.WHITE + permission);
            return true;
        }

        String nature;
        String temporary = "";
        String valueText = "";
        Boolean value = principal.permissions(world).get(permission);
        if (value != null) {
            nature = ChatColor.BLUE + "directly configures";
            valueText = value ? ChatColor.GREEN + " true" : ChatColor.RED + " false";
            if (principal instanceof User && ((User) principal).temporary)
                temporary = ChatColor.GRAY + " temporarily";
        } else {
            value = principal.permissionsTotal(world).get(permission);
            if (value != null) {
                nature = ChatColor.LIGHT_PURPLE + "implicitly configures";
                valueText = value ? ChatColor.GREEN + " true" : ChatColor.RED + " false";
                if (principal instanceof User && ((User) principal).temporary)
                    temporary = ChatColor.GRAY + " temporarily";
            } else {
                // TODO check for offlineplayer to determine is op, then identify default permission value
                nature = ChatColor.DARK_GRAY + "does not configure";
            }
        }

        sender.sendMessage(ChatColor.WHITE + principal.getName() + "@" + world + " " + nature + " " + ChatColor.WHITE + permission + valueText + temporary);
        return true;
    }

}
