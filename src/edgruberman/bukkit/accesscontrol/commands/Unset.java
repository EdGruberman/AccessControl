package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;

public class Unset implements CommandExecutor {

    private final JavaPlugin plugin;
    private final AccountManager manager;

    public Unset(final JavaPlugin plugin, final String label, final AccountManager manager) {
        this.plugin = plugin;
        final PluginCommand command = plugin.getCommand(label);
        command.setExecutor(this);
        this.manager = manager;
    }

    // usage: /<command> <Permission>[ <Principal>[ <World>]]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Syntax error; Missing <Permission> argument");
            return false;
        }

        if (sender instanceof ConsoleCommandSender && args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Syntax error; <Principal> argument required from console");
            return false;
        }

        final String permission = args[0];

        final Principal principal;
        if (args.length >= 2) {
            principal = this.manager.getPrincipal(args[1]);
            if (principal == null) {
                sender.sendMessage(ChatColor.RED + "Unable to find principal: " + args[1]);
                return true;
            }
        } else {
            principal = this.manager.createUser(sender.getName());
        }

        String world = null;
        if (args.length >= 3) world = args[2];

        if (world == null) {
            final Boolean existing = principal.permissionsServer().get(permission);
            if (existing == null) {
                sender.sendMessage(ChatColor.YELLOW + principal.getName() + " does not directly set " + permission + " in server");
                return true;
            }
        } else {
            final Boolean existing = principal.permissionsWorld(world).get(permission);
            if (existing == null) {
                sender.sendMessage(ChatColor.YELLOW + principal.getName() + " does not directly set " + permission + " in " + world);
                return true;
            }
        }

        principal.unsetPermission(permission, world);
        principal.update();
        ((Main) this.plugin).save();
        sender.sendMessage(principal.getName() + " no longer directly sets " + permission + " in " + (world == null ? "server" : world));
        return true;
    }

}
