package edgruberman.bukkit.accesscontrol.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import edgruberman.bukkit.accesscontrol.commands.util.CancellationContingency;
import edgruberman.bukkit.accesscontrol.commands.util.ConfigurationExecutor;
import edgruberman.bukkit.accesscontrol.commands.util.ExecutionRequest;
import edgruberman.bukkit.accesscontrol.commands.util.IntegerParameter;
import edgruberman.bukkit.accesscontrol.commands.util.LowerCaseParameter;
import edgruberman.bukkit.accesscontrol.commands.util.OnlinePlayerParameter;
import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.accesscontrol.messaging.Message;
import edgruberman.bukkit.accesscontrol.messaging.RecipientList;

public class Effective extends ConfigurationExecutor {

    private final Server server;
    private final IntegerParameter page;
    private final OnlinePlayerParameter player;
    private final LowerCaseParameter match;

    public Effective(final ConfigurationCourier courier, final Server server) {
        super(courier);
        this.server = server;

        this.page = this.addOptional(IntegerParameter.Factory.create("page").setDefaultValue(1));
        this.player = this.addOptional(OnlinePlayerParameter.Factory.create("player", server));
        this.match = this.addOptional(LowerCaseParameter.Factory.create("match"));
    }

    // usage: /<command> [page] [player] [match]
    @Override
    protected boolean executeImplementation(final ExecutionRequest request) throws CancellationContingency {
        final Integer page = request.parse(this.page);
        final Player player = request.parse(this.player);
        final String match = request.parse(this.match);

        final List<PermissionAttachmentInfo> infos = new ArrayList<PermissionAttachmentInfo>(player.getEffectivePermissions());
        Collections.sort(infos, new AlphabeticalPermissionComparator());

        final List<Message> response = new ArrayList<Message>();
        for (final PermissionAttachmentInfo info : infos) {
            if (match != null && !info.getPermission().contains(match)) continue;
            final String source = ( info.getAttachment() != null ? info.getAttachment().getPlugin().getName() : this.server.getName() );
            final Message composed = this.courier.draft("effective-permission", info.getPermission(), player.getName(), player.hasPermission(info.getPermission())?1:0, source);
            if (composed != null) response.add(composed);
        }

        if (response.size() == 0) {
            this.courier.send(request.getSender(), "effective-none", match);
            return true;
        }

        final Message.Paginator paginator = new Message.Paginator(response, request.getSender());
        final int count = paginator.count();
        final int index = Math.min(Math.max(0, page - 1), count - 1); // between 1 and max pages
        for (final Message message : paginator.page(index)) this.courier.submit(RecipientList.Sender.create(request.getSender()), message);
        this.courier.send(request.getSender(), "effective-summary", index + 1, count);

        return true;
    }



    private static class AlphabeticalPermissionComparator implements Comparator<PermissionAttachmentInfo> {

        @Override
        public int compare(final PermissionAttachmentInfo o1, final PermissionAttachmentInfo o2) {
            return o1.getPermission().compareTo(o2.getPermission());
        }

    }

}
