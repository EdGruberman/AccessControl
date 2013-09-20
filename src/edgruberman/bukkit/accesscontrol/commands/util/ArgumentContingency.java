package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.List;

/** contains information on a value that could not be parsed for a Parameter */
public class ArgumentContingency extends CancellationContingency {

    private static final long serialVersionUID = 1L;

    private final Parameter<?> parameter;

    protected ArgumentContingency(final ExecutionRequest request, final Parameter<?> parameter) {
        super(request);
        this.parameter = parameter;
    }

    protected ArgumentContingency(final ExecutionRequest request, final Parameter<?> parameter, final String message, final Throwable cause) {
        super(request, message, cause);
        this.parameter = parameter;
    }

    protected ArgumentContingency(final ExecutionRequest request, final Parameter<?> parameter, final String message) {
        super(request, message);
        this.parameter = parameter;
    }

    protected ArgumentContingency(final ExecutionRequest request, final Parameter<?> parameter, final Throwable cause) {
        super(request, cause);
        this.parameter = parameter;
    }

    public Parameter<?> getParameter() {
        return this.parameter;
    }

    public List<String> getArguments() {
        return this.request.getArguments(this.parameter);
    }

}
