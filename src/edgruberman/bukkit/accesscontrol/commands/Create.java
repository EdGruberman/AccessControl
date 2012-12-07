package edgruberman.bukkit.accesscontrol.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.User;

public class Create implements CommandExecutor {

    private final Plugin plugin;
    private final AccountManager manager;
    private final int maximum;

    public Create(final Plugin plugin, final AccountManager manager, final int maximum) {
        this.plugin = plugin;
        this.manager = manager;
        this.maximum = maximum;
    }

    // usage: /<command> <Group>[ <Description>]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            Main.courier.send(sender, "requires-argument", "<Group>");
            return false;
        }

        final Group group = this.manager.getGroup(args[0]);
        final User creator = group.getCreator();
        if (this.manager.isRegistered(group)) {
            Main.courier.send(sender, "create-already", group.getName(), ( creator != null ? creator.getName() : "" ));
            return false;
        }

        final User user = this.manager.getUser(sender.getName());
        if (!sender.hasPermission("accesscontrol.override.create")) {
            int created = 0;
            for (final String permission : user.permissionsServer().keySet())
                if (permission.startsWith("accesscontrol.creator.")) created++;
            if (created >= this.maximum) {
                Main.courier.send(sender, "create-max", created, this.maximum);
                return true;
            }
        }

        this.manager.register(group);
        group.setDescription(( args.length >= 2 ? StringUtils.join(args, " ", 1, args.length) : null ));
        if (sender instanceof Player) {
            if (!this.manager.isRegistered(user)) this.manager.register(user);
            user.setPermission("accesscontrol.creator." + group.getName(), true, null);
            group.promoteOperator(user);
        }
        ((Main) this.plugin).save();
        Main.courier.send(sender, "create", group.getName());
        return true;
    }

}
