package edgruberman.bukkit.accesscontrol.commands.group;

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
    private final int maximum;

    public Creator(final AccountManager manager, final int maximum) {
        this.manager = manager;
        this.maximum = maximum;
    }

    // usage: /<command>[ <Player>]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player) && args.length < 1) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final User creator = this.manager.getUser(( args.length >= 1 ? args[0] : sender.getName() ));
        final List<Group> created = new ArrayList<Group>();
        for (final Group group : this.manager.getGroups())
            if (group.getCreator() != null && group.getCreator().equalsIgnoreCase(creator.getName()))
                created.add(group);

        Collections.sort(created, new AlphabeticallyByName());
        final int remaining = Math.max(this.maximum - created.size(), 0);
        Main.courier.send(sender, "creator-header", creator.getName(), created.size(), this.maximum, remaining);
        for (final Group group : created) {
            final String description = group.getDescription();
            Main.courier.send(sender, "creator", creator.getName(), group.getName(), ( description != null ? description : "" ));
        }
        Main.courier.send(sender, "creator-footer", creator.getName(), created.size(), this.maximum, remaining);
        if (remaining > 0 && (sender instanceof Player) && sender.getName().equalsIgnoreCase(creator.getName())) Main.courier.send(sender,  "creator-remaining-self");
        return true;
    }



    private static class AlphabeticallyByName implements Comparator<Group> {

        @Override
        public int compare(final Group o1, final Group o2) {
            return o1.getName().compareTo(o2.getName());
        }

    }

}
