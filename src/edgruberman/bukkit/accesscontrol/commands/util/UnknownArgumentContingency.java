package edgruberman.bukkit.accesscontrol.commands.util;

import java.text.MessageFormat;

public class UnknownArgumentContingency extends ArgumentContingency {

    private static final long serialVersionUID = 1L;

    public UnknownArgumentContingency(final ExecutionRequest request, final Parameter<?> parameter) {
        super(request, parameter,
                MessageFormat.format("command \"/{0}\" sent from {1}:\"{2}\" supplied unrecognizable value \"{3}\" for parameter \"{4}\" of type {5}"
                , request.getLabel(), request.getSender().getClass().getSimpleName(), request.getSender().getName()
                , request.getArgument(parameter.getBegin()), parameter.getName(), parameter.getClass().getName())
        );
    }

}
