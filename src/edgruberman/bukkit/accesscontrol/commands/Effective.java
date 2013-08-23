package edgruberman.bukkit.accesscontrol.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.messaging.Message;
import edgruberman.bukkit.accesscontrol.messaging.Recipients;
import edgruberman.bukkit.accesscontrol.util.TokenizedExecutor;

public class Effective extends TokenizedExecutor {

    private final Server server;

    public Effective(final Server server) {
        this.server = server;
    }

    // usage: /<command> [page] [player] [match]
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < 2 && !(sender instanceof Player)) {
            Main.courier.send(sender, "requires-argument", "player", 0);
            return false;
        }

        final int page;
        try {
            page = ( args.size() >= 1 ? Integer.valueOf(args.get(0)) : 1 );
        } catch (final NumberFormatException e) {
            Main.courier.send(sender, "unknown-value", "page", args.get(0));
            return false;
        }

        final String name = ( args.size() >= 2 ? args.get(1) : sender.getName() );
        final Player player = this.server.getPlayer(name);
        if (player == null) {
            Main.courier.send(sender, "unknown-value", "player", name);
            return false;
        }

        final String match = ( args.size() >= 3 ? args.get(2).toLowerCase(Locale.ENGLISH) : null);

        final List<PermissionAttachmentInfo> infos = new ArrayList<PermissionAttachmentInfo>(player.getEffectivePermissions());
        Collections.sort(infos, new AlphabeticalPermissionComparator());

        final List<Message> response = new ArrayList<Message>();
        for (final PermissionAttachmentInfo info : infos) {
            if (match != null && !info.getPermission().contains(match)) continue;
            final String source = ( info.getAttachment() != null ? info.getAttachment().getPlugin().getName() : this.server.getName() );
            final Message composed = Main.courier.compose("effective.permission", info.getPermission(), player.getName(), player.hasPermission(info.getPermission())?1:0, source);
            if (composed != null) response.add(composed);
        }

        if (response.size() == 0) {
            Main.courier.send(sender, "effective.none", match);
            return true;
        }

        final Message.Paginator paginator = new Message.Paginator(response, sender);
        final int count = paginator.count();
        final int index = Math.min(Math.max(0, page - 1), count - 1); // between 1 and max pages
        for (final Message message : paginator.page(index)) Main.courier.submit(Recipients.Sender.create(sender), message);
        Main.courier.send(sender, "effective.summary", index + 1, count);

        return true;
    }



    private static class AlphabeticalPermissionComparator implements Comparator<PermissionAttachmentInfo> {

        @Override
        public int compare(final PermissionAttachmentInfo o1, final PermissionAttachmentInfo o2) {
            return o1.getPermission().compareTo(o2.getPermission());
        }

    }

}
