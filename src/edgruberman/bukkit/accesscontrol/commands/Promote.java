package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.User;

public class Promote implements CommandExecutor {

    private final Plugin plugin;
    private final AccountManager manager;

    public Promote(final Plugin plugin, final AccountManager manager) {
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

        final User requestor = this.manager.getUser(sender.getName());
        if (!sender.hasPermission("accesscontrol.override.operator") && !group.isOperator(requestor)) {
            Main.courier.send(sender, "requires-operator", label);
            return true;
        }

        final Principal principal = this.manager.getPrincipal(args[1]);
        if (!group.promoteOperator(principal)) {
            Main.courier.send(sender, "promote-already", principal, group);
            return true;
        }

        ((Main) this.plugin).save();
        Main.courier.send(sender, "promote", principal, group);
        final Player target = Bukkit.getPlayerExact(principal.getName());
        if (target != null) Main.courier.send(target, "promote-notify", principal, group);
        return true;
    }

}
