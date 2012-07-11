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

public class Check implements CommandExecutor {

    private final Plugin plugin;

    public Check(final JavaPlugin plugin, final String label) {
        this.plugin = plugin;
        final PluginCommand command = plugin.getCommand(label);
        command.setExecutor(this);
    }

    // TODO distinguish a temporary user's default permissions
    // usage: /<command> <Permission>[ <Player>]
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

        String target = sender.getName();
        if (args.length >= 2) target = args[1];

        final Player player = this.plugin.getServer().getPlayerExact(target);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + target);
            return true;
        }

        final String permission = args[0];
        final String nature = player.isPermissionSet(permission) ? ChatColor.BLUE + "sets" : ChatColor.LIGHT_PURPLE + "defaults";
        final String effective = player.hasPermission(permission) ? ChatColor.GREEN + "true" : ChatColor.RED + "false";
        sender.sendMessage(ChatColor.WHITE + player.getName() + "@" + player.getWorld().getName() + " " + nature + " " + ChatColor.WHITE + permission + " " + effective);
        return true;
    }

}
