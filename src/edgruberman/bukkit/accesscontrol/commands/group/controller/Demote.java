package edgruberman.bukkit.accesscontrol.commands.group.controller;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;

public class Demote extends ControllerExecutor {

    private final Plugin plugin;

    public Demote(final Plugin plugin, final AccountManager manager) {
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

        final Boolean previous = principal.unsetPermission("accesscontrol.controller." + group.getName(), null);
        if (previous == null || previous == Boolean.FALSE) {
            Main.courier.send(sender, "demote-already", principal.getName(), group.getName());
            return true;
        }

        principal.update();
        ((Main) this.plugin).save();
        Main.courier.send(sender, "demote", principal.getName(), group.getName());
        return true;
    }

}
