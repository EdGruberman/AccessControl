package edgruberman.bukkit.accesscontrol.commands;

import java.util.Arrays;

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

public class Set implements CommandExecutor {

    private final JavaPlugin plugin;
    private final AccountManager manager;

    public Set(final JavaPlugin plugin, final String label, final AccountManager manager) {
        this.plugin = plugin;
        final PluginCommand command = plugin.getCommand(label);
        command.setExecutor(this);
        this.manager = manager;
    }

    // usage: /<command> <Permission> (true|false)[ <Principal>[ <World>]][ (+user|+group)]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Syntax error; <Permission> and (true|false) arguments required");
            return false;
        }

        if (sender instanceof ConsoleCommandSender && args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Syntax error; <Principal> argument required from console");
            return false;
        }

        final String permission = args[0];
        final boolean value = Boolean.valueOf(args[1]);

        Principal principal;
        if (args.length >= 3) {
            principal = this.manager.getPrincipal(args[2]);
            if (principal == null) {
                if (!args[args.length - 1].startsWith("+")) {
                    sender.sendMessage(ChatColor.RED + "Unable to find principal: " + args[2]);
                    return true;
                }

                if (args[args.length - 1].equalsIgnoreCase("+user")) {
                    principal = this.manager.createUser(args[2]);


                } else if (args[args.length - 1].equalsIgnoreCase("+group")) {
                    principal = this.manager.createGroup(args[2]);

                } else {
                    sender.sendMessage(ChatColor.RED + "Unable to find principal: " + args[2]);
                    return true;
                }
            }

        } else {
            principal = this.manager.createUser(sender.getName());
        }

        // Strip trailing optional directive if present
        if (args[args.length - 1].equalsIgnoreCase("+user") || args[args.length - 1].equalsIgnoreCase("+group"))
            args = Arrays.copyOf(args, args.length - 1);

        String world = null;
        if (args.length >= 4) world = args[3];

        if (world == null) {
            final Boolean existing = principal.permissionsServer().get(permission);
            if (existing != null && existing == value) {
                sender.sendMessage(ChatColor.YELLOW + principal.getName() + " already directly sets " + permission + " " + value + " in server");
                return true;
            }
        } else {
            final Boolean existing = principal.permissionsWorld(world).get(permission);
            if (existing != null && existing == value) {
                sender.sendMessage(ChatColor.YELLOW + principal.getName() + " already directly sets " + permission + " " + value + " in " + world);
                return true;
            }
        }

        principal.setPermission(permission, value, world);
        principal.update();
        ((Main) this.plugin).save();
        sender.sendMessage(principal.getName() + " now directly sets " + permission + " " + value + " in " + (world == null ? "server" : world));
        return true;
    }

}
