package edgruberman.bukkit.accesscontrol.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.util.FormattedArrayList;

public class Principal implements CommandExecutor {

    private final AccountManager manager;

    public Principal(final AccountManager manager) {
        this.manager = manager;
    }

    // usage: /<command>[ <Player>]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0 && !(sender instanceof Player)) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final edgruberman.bukkit.accesscontrol.Principal principal = this.manager.getPrincipal(( args.length >= 1 ? args[0] : sender.getName() ));
        if (!this.manager.isRegistered(principal)) {
            Main.courier.send(sender, "not-found", "<Principal>", args[0]);
            return false;
        }

        final List<String> granted = new FormattedArrayList<String>(Main.courier.getBase().getConfigurationSection("+principals"));
        final List<String> revoked = new FormattedArrayList<String>(Main.courier.getBase().getConfigurationSection("+principals"));

        for (final Map.Entry<String, Boolean> entry : principal.permissionsServer().entrySet())
            if (entry.getValue()) {
                final Group group = this.manager.getGroup(entry.getKey());
                granted.add(( group != null && group.isOperator(principal) ? Main.courier.format("+group-operator", entry.getKey()) : entry.getKey() ));
            } else {
                revoked.add(entry.getKey());
            }

        final Map<String, List<String>> worlds = new HashMap<String, List<String>>();
        List<String> grantedWorld;
        List<String> revokedWorld;
        for (final String world : principal.worlds()) {
            for (final Map.Entry<String, Boolean> entry : principal.permissionsWorld(world).entrySet()) {
                if (entry.getValue()) {
                    grantedWorld = worlds.get(world);
                    if (grantedWorld == null) grantedWorld = new FormattedArrayList<String>(Main.courier.getBase().getConfigurationSection("+principals"));
                    grantedWorld.add(entry.getKey());
                    worlds.put(world, grantedWorld);

                } else {
                    revokedWorld = worlds.get(world);
                    if (revokedWorld == null) grantedWorld = new FormattedArrayList<String>(Main.courier.getBase().getConfigurationSection("+principals"));
                    revokedWorld.add(entry.getKey());
                    worlds.put(world, revokedWorld);
                }
            }
        }

        Collections.sort(granted, Members.OperatorsFirst.COMPARATOR);
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
        return true;
    }

}
