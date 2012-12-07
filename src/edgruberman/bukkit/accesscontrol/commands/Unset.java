package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

    // usage: /<command> <Permission>[ (<Player>|<Principal>[ <World>])]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            Main.courier.send(sender, "requires-argument", "<Permission>");
            return false;
        }

        if (!(sender instanceof Player) && args.length < 2) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final String permission = args[0];
        final Principal principal = this.manager.getPrincipal(( args.length >= 2 ? args[1] : sender.getName() ));
        final String world = ( args.length >= 3 ? args[2] : null );

        final Boolean existing = ( world == null ? principal.permissionsServer().get(permission) : principal.permissionsWorld(world).get(permission) );
        if (existing == null) {
            Main.courier.send(sender, "not-set", principal.getName(), permission, ( world == null ? "server" : world ));
            return true;
        }

        principal.unsetPermission(permission, world);
        principal.update();
        if (principal.permissionsServer().size() == 0 && principal.worlds().size() == 0) this.manager.deregister(principal);
        ((Main) this.plugin).save();
        Main.courier.send(sender, "unset", principal.getName(), permission, (world == null ? "server" : world));
        return true;
    }

}
