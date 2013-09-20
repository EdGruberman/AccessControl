package edgruberman.bukkit.accesscontrol.commands.util;

/** contains information for a command execution being cancelled */
public abstract class CancellationContingency extends Exception {

    private static final long serialVersionUID = 1L;

    protected final ExecutionRequest request;

    protected CancellationContingency(final ExecutionRequest request) {
        super();
        this.request = request;
    }

    protected CancellationContingency(final ExecutionRequest request, final String message, final Throwable cause) {
        super(message, cause);
        this.request = request;
    }

    protected CancellationContingency(final ExecutionRequest request, final String message) {
        super(message);
        this.request = request;
    }

    protected CancellationContingency(final ExecutionRequest request, final Throwable cause) {
        super(cause);
        this.request = request;
    }

    public ExecutionRequest getRequest() {
        return this.request;
    }

}