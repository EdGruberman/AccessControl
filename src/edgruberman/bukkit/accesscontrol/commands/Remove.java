package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;

public class Remove implements CommandExecutor {

    private final Plugin plugin;
    private final AccountManager manager;

    public Remove(final Plugin plugin, final AccountManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // usage: /<command> <Group> <Player>
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            Main.courier.send(sender, "requires-argument", "<Group>");
            return false;
        }

        if (args.length < 2) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final Group group = this.manager.getGroup(args[0]);
        if (!this.manager.isRegistered(group)) {
            Main.courier.send(sender, "not-found", "<Group>", args[0]);
            return false;
        }

        final World world = ( args.length >= 3 ? Bukkit.getWorld(args[2]) : null );
        if (args.length >= 3 && world == null) {
            Main.courier.send(sender, "not-found", "<World>", args[2]);
            return false;
        }

        final Principal principal = this.manager.getPrincipal(args[1]);
        if (!group.isMember(principal, ( world != null ? world.getName() : null ))) {
            Main.courier.send(sender, "remove-already", principal.getName(), group.getName(), ( world == null ? "server" : world.getName() ));
            return true;
        }

        group.removeMember(principal, ( world != null ? world.getName() : null ));
        ((Main) this.plugin).save();
        Main.courier.send(sender, "remove", principal.getName(), group.getName(), (world == null ? "server" : world.getName()));
        return true;
    }

}
