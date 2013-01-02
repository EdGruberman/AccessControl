package edgruberman.bukkit.accesscontrol.commands.permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;

public class Deny implements CommandExecutor {

    private final Plugin plugin;
    private final AccountManager manager;

    public Deny(final Plugin plugin, final AccountManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // usage: /<command> <Permission>[ (<Player>|<Principal>[ <World>])]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            Main.courier.send(sender, "requires-argument", "<Permission>");
            return false;
        }

        if (!(sender instanceof Player) && args.length < 2) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final String permission = args[0];
        final Principal principal = this.manager.createPrincipal(( args.length >= 2 ? args[1] : sender.getName() ));
        final String world = ( args.length >= 3 ? args[2] : null );
        final Boolean previous = principal.direct(world).get(permission);
        if (previous == Boolean.FALSE) {
            Main.courier.send(sender, "deny-already", principal.getName(), permission, ( world == null ? "server" : world ));
            return false;
        }

        principal.setPermission(permission, false, world);
        principal.update();
        ((Main) this.plugin).save();
        Main.courier.send(sender, "deny", principal.getName(), permission, ( world == null ? "server" : world ), ( previous == null ? 0 : 1 ));
        return true;
    }

}
