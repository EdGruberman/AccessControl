package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;

public class Unset implements CommandExecutor {

    private final Plugin plugin;
    private final AccountManager manager;

    public Unset(final Plugin plugin, final AccountManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // usage: /<command> <Permission>[ <Principal>[ <World>]]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            Main.courier.send(sender, "requiresArgument", "<Permission>");
            return false;
        }

        if (sender instanceof ConsoleCommandSender && args.length < 2) {
            Main.courier.send(sender, "requiresArgument", "<Principal>");
            return false;
        }

        final String permission = args[0];

        final Principal principal;
        if (args.length >= 2) {
            principal = this.manager.getPrincipal(args[1]);
            if (principal == null) {
                Main.courier.send(sender, "principalNotFound", args[1]);
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
                Main.courier.send(sender, "notSet", principal.getName(), permission, "server");
                return true;
            }
        } else {
            final Boolean existing = principal.permissionsWorld(world).get(permission);
            if (existing == null) {
                Main.courier.send(sender, "notSet", principal.getName(), permission, world);
                return true;
            }
        }

        principal.unsetPermission(permission, world);
        principal.update();
        ((Main) this.plugin).save();
        Main.courier.send(sender, "unset", principal.getName(), permission, (world == null ? "server" : world));
        return true;
    }

}
