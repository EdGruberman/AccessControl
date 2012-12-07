package edgruberman.bukkit.accesscontrol.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.User;
import edgruberman.bukkit.accesscontrol.util.FormattedArrayList;

public class Members implements CommandExecutor {

    private final AccountManager manager;

    public Members(final AccountManager manager) {
        this.manager = manager;
    }

    // usage: /<command> <Group>
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            Main.courier.send(sender, "requires-argument", "<Group>");
            return false;
        }

        final Group group = this.manager.getGroup(this.manager.formatGroup(args[0]));
        if (!this.manager.isRegistered(group) || (group.isPrivate() && !group.isMember(sender) && !sender.hasPermission("accesscontrol.override.private"))) {
            Main.courier.send(sender, "not-found", "<Group>", args[0]);
            return false;
        }

        final User creator = group.getCreator();
        final String description = group.getDescription();
        Main.courier.send(sender, "describe", group.getName(), ( description != null ? description : "" ), 1
                , ( creator != null ? creator.getName() : "" ), group.getPermission().getDefault().name());

        final List<String> granted = new FormattedArrayList<String>(Main.courier.getBase().getConfigurationSection("+principals"));
        final List<String> revoked = new FormattedArrayList<String>(Main.courier.getBase().getConfigurationSection("+principals"));
        final Map<String, List<String>> worlds = new HashMap<String, List<String>>();

        // iterate all groups/players compiling full list of direct references
        List<String> grantedWorld;
        List<String> revokedWorld;
        Boolean value;
        final List<Principal> principals = new ArrayList<Principal>();
        principals.addAll(this.manager.getGroups());
        principals.addAll(this.manager.getUsers());
        for (final Principal p : principals) {
            value = p.permissionsServer().get(group.getName());
            if (value == null) continue;
            if (value) {
                granted.add(( group.isOperator(p) ? Main.courier.format("+group-operator", p.getName()) : p.getName() ));
            } else {
                revoked.add(p.getName());
            }

            for (final String world : p.worlds()) {
                value = p.permissionsWorld(world).get(group.getName());
                if (value == null) continue;
                if (value) {
                    grantedWorld = worlds.get(world);
                    if (grantedWorld == null) grantedWorld = new FormattedArrayList<String>(Main.courier.getBase().getConfigurationSection("+principals"));
                    grantedWorld.add(p.getName());
                    worlds.put(world, grantedWorld);

                } else {
                    revokedWorld = worlds.get(world);
                    if (revokedWorld == null) grantedWorld = new FormattedArrayList<String>(Main.courier.getBase().getConfigurationSection("+principals"));
                    revokedWorld.add(p.getName());
                    worlds.put(world, revokedWorld);
                }
            }
        }

        Collections.sort(granted, OperatorsFirst.COMPARATOR);
        Collections.sort(revoked);
        if (granted.size() > 0) Main.courier.send(sender, "granted-server", granted);
        if (revoked.size() > 0) Main.courier.send(sender, "revoked-server", revoked);

        final List<String> worldsSorted = new ArrayList<String>(worlds.keySet());
        Collections.sort(worldsSorted);
        for (final String world : worldsSorted) {
            grantedWorld = worlds.get(world);
            if (grantedWorld != null) {
                Collections.sort(grantedWorld);
                if (grantedWorld.size() > 0) Main.courier.send(sender, "granted-world", grantedWorld);
            }
            revokedWorld = worlds.get(world);
            if (revokedWorld != null) {
                Collections.sort(revokedWorld);
                if (revokedWorld.size() > 0) Main.courier.send(sender, "revoked-world", revokedWorld);
            }
        }

        final User requestor = this.manager.getUser(sender.getName());
        if (group.isOperator(requestor)) Main.courier.send(sender, "members-operator", group.getName());

        return true;
    }



    static class OperatorsFirst implements Comparator<String> {

        static final Comparator<String> COMPARATOR = new OperatorsFirst();

        @Override
        public int compare(final String o1, final String o2) {
            if (o1.startsWith("!") && !o2.startsWith("!")) return 1;
            if (!o1.startsWith("!") && o2.startsWith("!")) return -1;
            return o1.substring(1).compareTo(o2.substring(1));
        }

    }

}
