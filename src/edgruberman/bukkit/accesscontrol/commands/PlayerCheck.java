package edgruberman.bukkit.accesscontrol.commands;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.User;

/**
 *  /<command> <Permission>[ [<World>:]<Player>]
 */
public class PlayerCheck implements CommandExecutor {

    private Plugin plugin;
    private AccountManager manager;

    public PlayerCheck(final JavaPlugin plugin, final String label, final AccountManager manager) {
        this.manager = manager;
        this.plugin = plugin;

        final PluginCommand command = plugin.getCommand(label);
        if (command == null) {
            plugin.getLogger().warning("Unable to get plugin command: " + label);
            return;
        }

        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Syntax error; Missing <Permission> argument");
            return false;
        }

        if (sender instanceof ConsoleCommandSender && args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Syntax error; Missing <Player> argument");
            return false;
        }

        final String permission = args[0];

        String targetName = sender.getName();
        if (args.length >= 2) targetName = args[1];
        String world = null;
        if (targetName.contains(":")) {
            world = targetName.split(":")[0];
            targetName = targetName.split(":")[1];
        }
        final OfflinePlayer target = this.plugin.getServer().getOfflinePlayer(targetName);

        String nature = null;
        String effective = null;
        if (target.isOnline() && world == null) {
            world = target.getPlayer().getWorld().getName();
            nature = target.getPlayer().isPermissionSet(permission) ? ChatColor.BLUE + "sets" : ChatColor.LIGHT_PURPLE + "defaults";
            effective = target.getPlayer().hasPermission(permission) ? ChatColor.GREEN + "true" : ChatColor.RED + "false";
        } else {
            // offline, determine plugin configuration for user
            final User user = this.manager.createUser(targetName);
            final Map<String, Boolean> configured = user.configuredPermissions(world);
            if (!configured.containsKey(permission)) {
                // not set, use default
                nature = ChatColor.LIGHT_PURPLE + "defaults";
            } else if (configured.get(permission) == null) {
                // explicit unset, use default
                nature = ChatColor.DARK_GRAY + "unsets";
            } else {
                // set true/false
                nature = ChatColor.BLUE + "sets";
                effective = configured.get(permission) ? ChatColor.GREEN + "true" : ChatColor.RED + "false";
            }
        }

        if (effective == null) {
            final Permission perm = this.plugin.getServer().getPluginManager().getPermission(permission);
            if (perm != null) {
                effective = perm.getDefault().getValue(target.isOp()) ? ChatColor.GREEN + "true" : ChatColor.RED + "false";
            } else {
                effective = Permission.DEFAULT_PERMISSION.getValue(target.isOp()) ? ChatColor.GREEN + "true" : ChatColor.RED + "false";
            }
        }

        sender.sendMessage(ChatColor.WHITE + (world != null ? "[" + world + "] " : "") + target.getName() + " " + nature + " " + ChatColor.WHITE + permission + " " + effective);
        return true;
    }

}
