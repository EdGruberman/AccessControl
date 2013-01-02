package edgruberman.bukkit.accesscontrol.commands.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.commands.Executor;
import edgruberman.bukkit.accesscontrol.util.FormattedArrayList;

public class Members extends Executor {

    private final AccountManager manager;
    private final CommandExecutor creator;

    public Members(final AccountManager manager, final CommandExecutor creator) {
        this.manager = manager;
        this.creator = creator;
    }

    // usage: /<command>[ <Group>]
    @Override
    public boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < 1) return this.creator.onCommand(sender, command, label, args.toArray(new String[0]));

        final Group group = this.manager.getGroup(this.manager.formatGroup(args.get(0)));
        if (group == null || (group.isPrivate() && !sender.hasPermission(group.getName()) && !sender.hasPermission("accesscontrol.override.private"))) {
            Main.courier.send(sender, "unknown-argument", "<Group>", args.get(0));
            return false;
        }

        final Principal creator = ( group.getCreator() != null ? this.manager.getPrincipal(group.getCreator()) : null );
        final String description = group.getDescription();
        Main.courier.send(sender, "describe", group.getName(), ( description != null ? description : "" ), 1
                , ( creator != null ? creator.getName() : "" ), group.getBukkitPermission().getDefault().name());

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
            value = p.getMemberships(null).get(group);
            if (value == null) continue;
            if (value) {
                granted.add(( p.permissions(null).containsKey("accesscontrol.controller." + group.getName()) ? Main.courier.format("+group-controller", p.getName()) : p.getName() ));
            } else {
                revoked.add(p.getName());
            }

            for (final String world : p.worlds()) {
                value = p.getMemberships(world).get(group.getName());
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

        Collections.sort(granted, new ControllersFirst());
        Collections.sort(revoked);
        if (granted.size() > 0) Main.courier.send(sender, "granted-server", granted);
        if (revoked.size() > 0) Main.courier.send(sender, "denied-server", revoked);

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
                if (revokedWorld.size() > 0) Main.courier.send(sender, "denied-world", revokedWorld);
            }
        }

        if (sender.hasPermission("accesscontrol.controller." + group.getName())) Main.courier.send(sender, "members-controller", group.getName());
        return true;
    }



    public static class ControllersFirst implements Comparator<String> {

        private final Pattern controller;

        public ControllersFirst() {
            this.controller = Pattern.compile(Main.courier.format("+group-controller", ".*"));
        }

        @Override
        public int compare(final String o1, final String o2) {
            final boolean op1 = this.controller.matcher(o1).find();
            final boolean op2 = this.controller.matcher(o2).find();
            if (op1 && !op2) return -1;
            if (!op1 && op2) return 1;
            if (op1 && op2) return o1.substring(1).compareTo(o2.substring(1));
            return o1.compareTo(o2);
        }

    }

}
