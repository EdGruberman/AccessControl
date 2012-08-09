package edgruberman.bukkit.accesscontrol.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;

public class Set implements CommandExecutor {

    private final Plugin plugin;
    private final AccountManager manager;

    public Set(final Plugin plugin, final AccountManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // usage: /<command> <Permission> (true|false)[ <Principal>[ <World>]][ (+user|+group)]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
        if (args.length == 0) {
            Main.courier.send(sender, "requiresArgument", "<Permission>");
            return false;
        }

        if (sender instanceof ConsoleCommandSender && args.length == 1) {
            Main.courier.send(sender, "requiresArgument", "(true|false)");
            return false;
        }

        if (sender instanceof ConsoleCommandSender && args.length == 2) {
            Main.courier.send(sender, "requiresArgument", "<Principal>");
            return false;
        }

        final String permission = args[0];
        final boolean value = Boolean.valueOf(args[1]);

        Principal principal;
        if (args.length >= 3) {
            principal = this.manager.getPrincipal(args[2]);
            if (principal == null) {
                if (args[args.length - 1].equalsIgnoreCase("+user")) {
                    principal = this.manager.createUser(args[2]);

                } else if (args[args.length - 1].equalsIgnoreCase("+group")) {
                    principal = this.manager.createGroup(args[2]);

                } else {
                    Main.courier.send(sender, "principalNotFound", args[2]);
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
                Main.courier.send(sender, "alreadySet", principal.getName(), permission, value, "server");
                return true;
            }
        } else {
            final Boolean existing = principal.permissionsWorld(world).get(permission);
            if (existing != null && existing == value) {
                Main.courier.send(sender, "alreadySet", principal.getName(), permission, value, world);
                return true;
            }
        }

        principal.setPermission(permission, value, world);
        principal.update();
        ((Main) this.plugin).save();
        Main.courier.send(sender, "set", principal.getName(), permission, value, (world == null ? "server" : world));
        return true;
    }

}
