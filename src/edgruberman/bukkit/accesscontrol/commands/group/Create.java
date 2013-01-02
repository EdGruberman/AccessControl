package edgruberman.bukkit.accesscontrol.commands.group;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.User;
import edgruberman.bukkit.accesscontrol.commands.Executor;

public class Create extends Executor {

    private final Plugin plugin;
    private final AccountManager manager;
    private final int maximum;
    private final CommandExecutor creator;

    public Create(final Plugin plugin, final AccountManager manager, final int maximum, final CommandExecutor creator) {
        this.plugin = plugin;
        this.manager = manager;
        this.maximum = maximum;
        this.creator = creator;
    }

    // usage: /<command>[ <Group>[ <Description>]]
    @Override
    public boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < 1) return this.creator.onCommand(sender, command, label, args.toArray(new String[0]));

        Group group = this.manager.getGroup(args.get(0));
        if (group != null) {
            Main.courier.send(sender, "create-already", group.getName(), ( group.getCreator() != null ? group.getCreator() : "" ));
            return true;
        }

        final User creator = this.manager.getUser(sender.getName());
        if (!sender.hasPermission("accesscontrol.override.create")) {
            int created = 0;
            for (final Group g : this.manager.getGroups())
                if (g.getCreator() != null && g.getCreator().equalsIgnoreCase(creator.getName()))
                    created++;

            if (created >= this.maximum) {
                Main.courier.send(sender, "create-max", created, this.maximum);
                return true;
            }
        }

        group = this.manager.createGroup(args.get(0));
        group.setCreator(creator.getName());
        group.setDescription(( args.size() >= 2 ? Executor.join(args.subList(1, args.size()), " ") : null ));
        creator.setMembership(group, true, null);
        creator.setPermission("accesscontrol.controller." + group.getName(), true, null);
        creator.update();
        ((Main) this.plugin).save();
        Main.courier.send(sender, "create", group.getName());
        return true;
    }

}
