package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.User;

public class Demote implements CommandExecutor {

    private final Plugin plugin;
    private final AccountManager manager;

    public Demote(final Plugin plugin, final AccountManager manager) {
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
        if (principal == requestor) {
            Main.courier.send(sender, "demote-self");
            return true;
        }

        if (!group.demoteOperator(principal)) {
            Main.courier.send(sender, "demote-already", principal, group);
            return true;
        }

        ((Main) this.plugin).save();
        Main.courier.send(sender, "demote", principal, group);
        return true;
    }

}
