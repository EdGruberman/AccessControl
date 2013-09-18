package edgruberman.bukkit.accesscontrol.commands.util;

public class ArgumentMissingException extends ArgumentParseException {

    private static final long serialVersionUID = 1L;

    public ArgumentMissingException(final ExecutionRequest request, final Parameter<?> parameter) {
        super("argument-missing", request, parameter);
    }

}
