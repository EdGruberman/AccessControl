package edgruberman.bukkit.accesscontrol.commands.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

public class SenderRejectedContingency extends CancellationContingency {

    private static final long serialVersionUID = 1L;

    private final List<Class<? extends CommandSender>> valid;

    protected SenderRejectedContingency(final ExecutionRequest request, final List<Class<? extends CommandSender>> valid) {
        super(request, MessageFormat.format("rejected sender of {0}; valid senders are {1}"
                , request.getSender().getClass(), new ArrayList<Class<? extends CommandSender>>(valid)));
        this.valid = Collections.unmodifiableList(valid);
    }

    public List<Class<? extends CommandSender>> getValid() {
        return this.valid;
    }

}
