package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.User;

/**
 *  /<command> [<World>:]<Player> <Permission>
 */
public class PlayerRemovePermission implements CommandExecutor {

    private Plugin plugin;
    private AccountManager manager;
    private Command playerCheck;

    public PlayerRemovePermission(final JavaPlugin plugin, final String label, final AccountManager manager, final Command playerCheck) {
        this.manager = manager;
        this.plugin = plugin;
        this.playerCheck = playerCheck;

        final PluginCommand command = plugin.getCommand(label);
        if (command == null) {
            plugin.getLogger().warning("Unable to get plugin command: " + label);
            return;
        }

        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Syntax error");
            return false;
        }

        String name = args[0];
        String world = null;
        if (name.contains(":")) {
            world = name.split(":")[1];
            name = name.split(":")[0];
        }

        final OfflinePlayer target = this.plugin.getServer().getOfflinePlayer(args[0]);
        name = target.getName();
        if (target.isOnline()) name = target.getPlayer().getName();
        final User user = this.manager.createUser(name);

        final String permission = args[1];

        if (!user.removePermission(permission, world)) {
            sender.sendMessage(ChatColor.WHITE + (world != null ? "[" + world + "] " : "") + name + ChatColor.YELLOW + " does not directly configure " + ChatColor.WHITE + permission);
            return true;
        }

        this.manager.save();
        this.playerCheck.execute(sender, this.playerCheck.getLabel(), new String[] {permission, name});
        return true;
    }

}
