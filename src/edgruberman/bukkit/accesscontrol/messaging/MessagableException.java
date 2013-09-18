package edgruberman.bukkit.accesscontrol.messaging;

/**
 * exception that can be used to format a message to a recipient
 * @author EdGruberman (ed@rjump.com)
 * @version 1.0.0
 */
public class MessagableException extends Exception {

    private static final long serialVersionUID = 1L;

    private final Object[] arguments;

    public MessagableException(final String message, final Object... arguments) {
        super(message);
        this.arguments = arguments;
    }

    public MessagableException(final Throwable cause, final String message, final Object... arguments) {
        super(message, cause);
        this.arguments = arguments;
    }

    public Object[] getArguments() {
        return this.arguments;
    }

    public Message draft(final Courier courier) {
        final Message result = courier.draft(this.getMessage(), this.arguments);

        if (this.getCause() instanceof MessagableException) {
            final MessagableException cause = (MessagableException) this.getCause();
            result.append(cause.draft(courier));
        }

        return result;
    }

    public void submit(final Courier courier, final RecipientList recipients) {
        final Message message = this.draft(courier);
        courier.submit(recipients, message);
    }

}
