package edgruberman.bukkit.accesscontrol.commands.group.controller;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;

public class Destroy extends ControllerExecutor {

    private final Plugin plugin;

    public Destroy(final Plugin plugin, final AccountManager manager) {
        super(manager, 0);
        this.plugin = plugin;
    }

    // usage: /<command> <Group>[ confirm]
    @Override
    public boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Group group) {
        if (args.size() < 2 || !args.get(1).equalsIgnoreCase("confirm")) {
            Main.courier.send(sender, "destroy-confirm", group.getName(), group.getDescription(), label);
            return true;
        }

        final List<Principal> changed = group.delete();
        for (final Principal affected : changed) {
            affected.unsetPermission("accesscontrol.controller." + group.getName(), null);
            affected.update();
        }
        ((Main) this.plugin).save();
        Main.courier.send(sender, "destroy", group.getName(), changed.size());
        return true;
    }

}
