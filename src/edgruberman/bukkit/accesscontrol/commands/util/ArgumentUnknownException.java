package edgruberman.bukkit.accesscontrol.commands.util;

public class ArgumentUnknownException extends ArgumentParseException {

    private static final long serialVersionUID = 1L;

    public ArgumentUnknownException(final ExecutionRequest request, final Parameter<?> parameter) {
        super("argument-unknown", request, parameter);
    }

}
