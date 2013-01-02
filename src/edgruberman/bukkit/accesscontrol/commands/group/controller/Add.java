package edgruberman.bukkit.accesscontrol.commands.group.controller;

import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;

public class Add extends ControllerExecutor {

    private final Plugin plugin;

    public Add(final Plugin plugin, final AccountManager manager) {
        super(manager, 0);
        this.plugin = plugin;
    }

    // usage: /<command> <Group> <Player>[ <World>]
    @Override
    public boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Group group) {
        if (args.size() < 2) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final Principal principal = ( this.manager.isGroup(args.get(1)) ? this.manager.getGroup(args.get(1)) : this.manager.createUser(args.get(1)) );
        if (principal == null) {
            Main.courier.send(sender, "unknown-argument", "<Player>", args.get(1));
            return false;
        }

        if (this.manager.getUser(sender.getName()) == principal && !sender.hasPermission("accesscontrol.override.controller")) {
            Main.courier.send(sender, "not-self", label);
            return true;
        }

        final String world = ( args.size() >= 3 ? args.get(2) : null );
        final Map<Group, Boolean> memberships = principal.getMemberships(world);
        final Boolean previous = (memberships != null ? memberships.get(group) : null);
        if (previous == Boolean.TRUE) {
            Main.courier.send(sender, "add-already", principal.getName(), group.getName(), ( world == null ? "server" : world ));
            return true;
        }

        principal.setMembership(group, true, world);
        ((Main) this.plugin).save();
        Main.courier.send(sender, "add", principal.getName(), group.getName(), ( world == null ? "server" : world ), ( previous == null ? 0 : -1 ));
        return true;
    }

}
