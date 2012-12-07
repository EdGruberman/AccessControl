package edgruberman.bukkit.accesscontrol.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.User;

public class Destroy implements CommandExecutor {

    private final Plugin plugin;
    private final AccountManager manager;

    public Destroy(final Plugin plugin, final AccountManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // usage: /<command> <Group>[ confirm]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            Main.courier.send(sender, "requires-argument", "<Group>");
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

        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            Main.courier.send(sender, "destroy-confirm", group.getName(), group.getDescription(), label);
            return true;
        }

        final List<Principal> changed = group.delete();
        ((Main) this.plugin).save();
        Main.courier.send(sender, "destroy", group.getName(), changed.size());
        return true;
    }

}
