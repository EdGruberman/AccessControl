package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.text.StrTokenizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * parses arguments according to a {@link StrTokenizer} definition
 * and defined {@link Parameter}s
 * @version 1.2.0
 */
public abstract class Executor implements CommandExecutor {

    static {
        final List<Class<? extends CommandSender>> any = new ArrayList<Class<? extends CommandSender>>();
        any.add(CommandSender.class);
        SENDERS_ANY = Collections.unmodifiableList(any);

        final List<Class<? extends CommandSender>> players = new ArrayList<Class<? extends CommandSender>>();
        players.add(Player.class);
        SENDERS_PLAYERS = Collections.unmodifiableList(players);
    }

    public static final List<Class<? extends CommandSender>> SENDERS_ANY;
    public static final List<Class<? extends CommandSender>> SENDERS_PLAYERS;

    public static final List<Class<? extends CommandSender>> DEFAULT_SENDERS = Executor.SENDERS_ANY;





    protected final StrTokenizer tokenizer = new StrTokenizer();
    protected final List<Class<? extends CommandSender>> senders = new ArrayList<Class<? extends CommandSender>>(Executor.DEFAULT_SENDERS);
    protected final List<Parameter<?>> parameters = new ArrayList<Parameter<?>>();

    /** configures tokenizer to delimit by spaces using double quotes as the quote character */
    protected Executor() {
        this.tokenizer.setDelimiterChar(' ');
        this.tokenizer.setQuoteChar('"');
    }

    protected void requirePlayer() {
        this.validSenders(Executor.SENDERS_PLAYERS);
    }

    protected void validSenders(final Collection<Class<? extends CommandSender>> valid) {
        this.senders.clear();
        this.senders.addAll(valid);
    }

    /**
     * configures as required and sets begin to next available index
     * @return the resultant parameter
     */
    protected <P extends Parameter<T>, T> P addRequired(final Parameter.Factory<P, T, ?> factory) {
        return this.addParameter(factory.setRequired(true));
    }

    /**
     * configures as not required and sets begin to next available index
     * @return the resultant parameter
     */
    protected <P extends Parameter<T>, T> P addOptional(final Parameter.Factory<P, T, ?> factory) {
        return this.addParameter(factory.setRequired(false));
    }

    /**
     * sets index to next available
     * @return the resultant parameter
     */
    protected <P extends Parameter<T>, T> P addParameter(final Parameter.Factory<P, T, ?> factory) {
        final P result = factory.setIndex(this.parameters.size()).build();
        this.parameters.add(result);
        return result;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final List<String> tokenized = this.tokenize(args);
        final ExecutionRequest request = new ExecutionRequest(sender, command, label, tokenized);

        try {
            if (!this.validate(request)) return true;
            return this.execute(request);

        } catch (final CancellationContingency e) {
            throw new IllegalStateException(e);
        }
    }

    protected List<String> tokenize(final String... args) {
        this.tokenizer.reset(JoinList.join(args));
        return Arrays.asList(this.tokenizer.getTokenArray());
    }

    protected boolean validate(final ExecutionRequest request) throws CancellationContingency {
        for (final Class<? extends CommandSender> acceptable : this.senders) {
            if (acceptable.isAssignableFrom(request.getSender().getClass())) return true;
        }

        throw new SenderRejectedContingency(request, this.senders);
    }

    /**
     * @return true if command processing completed successfully
     * @throws CancellationContingency when the execution is stopped due to a requirement not being met
     */
    protected abstract boolean execute(final ExecutionRequest request) throws CancellationContingency;

}
