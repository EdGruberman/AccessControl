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

public class Grant implements CommandExecutor {

    private final Plugin plugin;
    private final AccountManager manager;

    public Grant(final Plugin plugin, final AccountManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // usage: /<command> <Permission> [ <Principal>[ <World>]][ (+user|+group)]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
        if (args.length < 1) {
            Main.courier.send(sender, "requires-argument", "<Permission>");
            return false;
        }

        if (sender instanceof ConsoleCommandSender && args.length < 2) {
            Main.courier.send(sender, "requires-argument", "<Principal>");
            return false;
        }

        final String principal = ( args.length >= 2 ? args[1] : null );
        final boolean createUser = args[args.length - 1].equalsIgnoreCase("+user");
        final boolean createGroup = args[args.length - 1].equalsIgnoreCase("+group");
        // Strip trailing optional directive if present
        if (args[args.length - 1].equalsIgnoreCase("+user") || args[args.length - 1].equalsIgnoreCase("+group"))
            args = Arrays.copyOf(args, args.length - 1);
        final String world = ( args.length >= 4 ? args[3] : null );
        this.set(sender, args[0], true, principal, world, createUser, createGroup);
        return true;
    }

    boolean set(final CommandSender sender, final String permission, final boolean value, final String principal, final String world, final boolean createUser, final boolean createGroup) {
        Principal p;
        if (principal != null) {
            p = this.manager.getPrincipal(principal);
            if (p == null) {
                if (createUser) {
                    p = this.manager.createUser(principal);

                } else if (createGroup) {
                    p = this.manager.createGroup(principal);

                } else {
                    Main.courier.send(sender, "principal-not-found", principal);
                    return false;
                }
            }

        } else {
            p = this.manager.createUser(sender.getName());
        }

        if (world == null) {
            final Boolean existing = p.permissionsServer().get(permission);
            if (existing != null && existing == value) {
                Main.courier.send(sender, "already-set", p.getName(), permission, value, "server");
                return false;
            }
        } else {
            final Boolean existing = p.permissionsWorld(world).get(permission);
            if (existing != null && existing == value) {
                Main.courier.send(sender, "already-set", p.getName(), permission, value, world);
                return false;
            }
        }

        p.setPermission(permission, value, world);
        p.update();
        ((Main) this.plugin).save();
        Main.courier.send(sender, "set", p.getName(), permission, value, (world == null ? "server" : world));
        return true;
    }

}
