package edgruberman.bukkit.accesscontrol.commands.group.controller;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.commands.Executor;

/** commands that require group controller status to use */
abstract class ControllerExecutor extends Executor {

    protected final AccountManager manager;
    protected final int index;

    /** @param index command argument position of group name (0 based) */
    protected ControllerExecutor(final AccountManager manager, final int index) {
        this.manager = manager;
        this.index = index;
    }

    @Override
    public boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < (this.index + 1)) {
            Main.courier.send(sender, "requires-argument", "<Group>");
            return false;
        }

        final Group group = this.manager.getGroup(args.get(this.index));
        if (group == null) {
            Main.courier.send(sender, "unknown-argument", "<Group>", args.get(this.index));
            return false;
        }

        if (!ControllerExecutor.allowed(sender, group)) {
            Main.courier.send(sender, "requires-controller", label);
            return true;
        }

        return this.execute(sender, command, label, args, group);
    }

    /** called only if group is found and sender is allowed to execute */
    public abstract boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Group group);



    /** @return true if sender is allowed to execute commands requiring group controller permission; false otherwise */
    protected static boolean allowed(final CommandSender sender, final Group group) {
        if (sender.hasPermission("accesscontrol.override.controller")) return true;
        if (sender.hasPermission("accesscontrol.controller." + group.getName())) return true;
        return false;
    }
}
