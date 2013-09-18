package edgruberman.bukkit.accesscontrol.commands.util;

import edgruberman.bukkit.accesscontrol.messaging.MessagableException;

/** contains information on an argument that could not be parsed for a Parameter */
public class ArgumentParseException extends MessagableException {

    private static final long serialVersionUID = 1L;

    private final ExecutionRequest request;
    private final Parameter<?> parameter;

    public ArgumentParseException(final String message, final ExecutionRequest request, final Parameter<?> parameter, final Object... arguments) {
        super(message, ArgumentParseException.combine(request, parameter, arguments));
        this.request = request;
        this.parameter = parameter;
    }

    public ArgumentParseException(final Throwable cause, final String message, final ExecutionRequest request, final Parameter<?> parameter, final Object... arguments) {
        super(cause, message, ArgumentParseException.combine(request, parameter, arguments));
        this.request = request;
        this.parameter = parameter;
    }

    public ExecutionRequest getRequest() {
        return this.request;
    }

    public Parameter<?> getParameter() {
        return this.parameter;
    }

    protected static Object[] combine(final ExecutionRequest request, final Parameter<?> parameter, final Object... arguments) {
        final Object[] result= new Object[2 + arguments.length];
        result[0] = parameter.getSyntax();
        result[1] = request.getArgument(parameter.getIndex());
        if (arguments.length > 0) System.arraycopy(arguments, 0, result, 2, arguments.length);
        return result;
    }

}