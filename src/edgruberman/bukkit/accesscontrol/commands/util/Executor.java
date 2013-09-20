package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.text.StrTokenizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * parses arguments according to a {@link StrTokenizer} definition
 * and defined parameters
 */
public abstract class Executor implements CommandExecutor {

    protected final StrTokenizer tokenizer = new StrTokenizer();
    protected final List<Parameter<?>> parameters = new ArrayList<Parameter<?>>();

    /** configures tokenizer to delimit by spaces using double quotes as the quote character */
    protected Executor() {
        this.tokenizer.setDelimiterChar(' ');
        this.tokenizer.setQuoteChar('"');
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
     * sets begin to next available index
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
            return this.execute(request);

        } catch (final CancellationContingency e) {
            throw new IllegalStateException(e);
        }
    }

    protected List<String> tokenize(final String... args) {
        this.tokenizer.reset(JoinList.join(Arrays.asList(args)));
        return Arrays.asList(this.tokenizer.getTokenArray());
    }

    /**
     * @return true if command processing completed successfully
     * @throws CancellationContingency when the execution is stopped due to a requirement not being met
     */
    protected abstract boolean execute(final ExecutionRequest request) throws CancellationContingency;

}
