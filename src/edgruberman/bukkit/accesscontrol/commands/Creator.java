package edgruberman.bukkit.accesscontrol.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.User;

public class Creator implements CommandExecutor {

    private final AccountManager manager;

    public Creator(final AccountManager manager) {
        this.manager = manager;
    }

    // usage: /<command>[ <Player>]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player) && args.length < 1) {
            Main.courier.send(sender, "requires-argument", "<Group>");
            return false;
        }

        final User user = this.manager.getUser(( args.length >= 1 ? args[0] : sender.getName() ));
        final List<Group> created = new ArrayList<Group>();
        for (final String permission : user.permissionsServer().keySet())
            if (permission.startsWith("accesscontrol.creator."))
                created.add(this.manager.getGroup(permission.substring("accesscontrol.creator.".length())));

        Collections.sort(created, new AlphabeticallyByName());
        for (final Group group : created) {
            final String description = group.getDescription();
            Main.courier.send(sender, "created", user.getName(), group.getName(), ( description != null ? description : "" ));
        }
        Main.courier.send(sender, "created-footer", user.getName(), created.size());
        return true;
    }



    private static class AlphabeticallyByName implements Comparator<Group> {

        @Override
        public int compare(final Group o1, final Group o2) {
            return o1.getName().compareTo(o2.getName());
        }

    }

}
