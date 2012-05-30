package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.User;

/**
 *  /<command> <Player> <Group>
 */
public class PlayerRemoveMembership implements CommandExecutor {

    private Plugin plugin;
    private AccountManager manager;

    public PlayerRemoveMembership(final JavaPlugin plugin, final String label, final AccountManager manager) {
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
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Syntax error");
            return false;
        }

        String name = args[0];
        final OfflinePlayer target = this.plugin.getServer().getOfflinePlayer(args[0]);
        name = target.getName();
        if (target.isOnline()) name = target.getPlayer().getName();
        final User user = this.manager.createUser(name);

        final String groupName = this.manager.extractName(args[1]);
        final Group group = this.manager.createGroup(groupName);
        if (!user.removeMembership(group)) {
            sender.sendMessage(ChatColor.WHITE + user.getName() + ChatColor.BLUE + " is not a direct member of " + ChatColor.WHITE + group.getName());
            return true;
        }

        this.manager.save();
        String senderName = sender.getName();
        if (sender instanceof Player) senderName = ((Player) sender).getDisplayName();
        if (user.getAttachment() != null) ((Player) user.getAttachment().getPermissible()).sendMessage(ChatColor.WHITE + senderName + ChatColor.GRAY + " removed you from group: " + ChatColor.WHITE + this.manager.formatName(group));
        sender.sendMessage(ChatColor.WHITE + user.getName() + ChatColor.GREEN + " removed as a direct member of " + ChatColor.WHITE + this.manager.formatName(group));
        return true;
    }

}
