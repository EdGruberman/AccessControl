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

public class Remove extends ControllerExecutor {

    private final Plugin plugin;

    public Remove(final Plugin plugin, final AccountManager manager) {
        super(manager, 0);
        this.plugin = plugin;
    }

    // usage: /<command> <Group> <Player>
    @Override
    public boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Group group) {
        if (args.size() < 2) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final Principal principal = this.manager.getPrincipal(args.get(1));
        if (this.manager.getUser(sender.getName()) == principal && !sender.hasPermission("accesscontrol.override.controller")) {
            Main.courier.send(sender, "not-self", label);
            return true;
        }

        final String world = ( args.size() >= 3 ? args.get(2) : null );
        final Map<Group, Boolean> memberships = ( principal != null ? principal.getMemberships(world) : null);
        final Boolean previous = ( memberships != null ? memberships.get(group) : null );
        if (previous == null) {
            Main.courier.send(sender, "remove-already", principal.getName(), group.getName(), ( world == null ? "server" : world ));
            return true;
        }

        principal.unsetPermission("accesscontrol.controller." + group.getName(), null);
        principal.unsetMembership(group, world);
        principal.update();
        ((Main) this.plugin).save();
        Main.courier.send(sender, "remove", principal.getName(), group.getName(), (world == null ? "server" : world), previous?1:-1);
        return true;
    }

}
