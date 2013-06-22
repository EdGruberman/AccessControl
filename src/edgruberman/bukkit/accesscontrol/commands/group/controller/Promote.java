package edgruberman.bukkit.accesscontrol.commands.group.controller;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;

public class Promote extends ControllerExecutor {

    private final Plugin plugin;

    public Promote(final Plugin plugin, final AccountManager manager) {
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

        final Principal principal = ( this.manager.isGroup(args.get(1)) ? this.manager.getGroup(args.get(1)) : this.manager.createUser(args.get(1)) );
        if (principal == null) {
            Main.courier.send(sender, "unknown-argument", "<Player>", args.get(1));
            return false;
        }

        final Boolean previous = principal.setPermission("accesscontrol.controller." + group.getName(), true, null);
        if (previous == Boolean.TRUE) {
            Main.courier.send(sender, "promote-already", principal, group);
            return true;
        }

        principal.setMembership(group, true, null);
        principal.update();
        ((Main) this.plugin).save();
        Main.courier.send(sender, "promote", principal.getName(), group.getName());
        final Player target = Bukkit.getPlayerExact(principal.getName());
        if (target != null) Main.courier.send(target, "promote-notify", principal.getName(), group.getName());
        return true;
    }

}