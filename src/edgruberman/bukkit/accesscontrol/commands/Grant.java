package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

        final String principal = ( args.length >= 2 ? args[1] : null );
        final String world = ( args.length >= 4 ? args[3] : null );
        this.set(sender, args[0], true, principal, world);
        return true;
    }

    boolean set(final CommandSender sender, final String permission, final boolean value, final String principal, final String world) {
        final Principal p = this.manager.getPrincipal(( principal != null ? principal : sender.getName() ));
        if (!this.manager.isRegistered(p)) this.manager.register(p);

        final Boolean existing = ( world == null ? p.permissionsServer().get(permission) : p.permissionsWorld(world).get(permission) );
        if (existing != null && existing == value) {
            Main.courier.send(sender, "already-set", p.getName(), permission, value, ( world == null ? "server" : world ));
            return false;
        }

        p.setPermission(permission, value, world);
        p.update();
        ((Main) this.plugin).save();
        Main.courier.send(sender, "set", p.getName(), permission, value, ( world == null ? "server" : world ));
        return true;
    }

}
