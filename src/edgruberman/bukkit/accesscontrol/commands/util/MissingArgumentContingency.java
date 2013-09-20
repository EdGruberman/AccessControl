package edgruberman.bukkit.accesscontrol.commands.util;

import java.text.MessageFormat;

public class MissingArgumentContingency extends ArgumentContingency {

    private static final long serialVersionUID = 1L;

    public MissingArgumentContingency(final ExecutionRequest request, final Parameter<?> parameter) {
        super(request, parameter,
                MessageFormat.format("command \"/{0}\" sent from {1}:\"{2}\" did not supply a value for parameter \"{3}\" of type {4}"
                , request.getLabel(), request.getSender().getClass().getSimpleName(), request.getSender().getName()
                , parameter.getName(), parameter.getClass().getName())
        );
    }

}
