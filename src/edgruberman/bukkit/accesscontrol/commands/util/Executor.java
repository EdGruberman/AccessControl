package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.text.StrTokenizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.accesscontrol.commands.util.ConditionalParameter.OptionalParameter;
import edgruberman.bukkit.accesscontrol.commands.util.ConditionalParameter.RequiredParameter;
import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.accesscontrol.messaging.RecipientList;

/**
 * parses arguments according to a {@link StrTokenizer} definition
 * and defined parameters
 */
public abstract class Executor implements CommandExecutor {

    protected ConfigurationCourier courier;
    protected final StrTokenizer tokenizer = new StrTokenizer();
    protected final List<Parameter<?>> parameters = new ArrayList<Parameter<?>>();

    /** configures tokenizer to delimit by spaces using double quotes as the quote character */
    protected Executor(final ConfigurationCourier courier) {
        this.courier = courier;
        this.tokenizer.setDelimiterChar(' ');
        this.tokenizer.setQuoteChar('"');
    }

    protected <P extends Parameter<T>, T> P addRequired(final Parameter.Factory<P, T, ?> factory) {
        factory.setSyntax(this.courier.format("argument-required", factory.syntax));
        factory.setIndex(this.parameters.size());

        final P result = factory.build();
        this.parameters.add(new RequiredParameter<T>(factory, result, this.courier));
        return result;
    }

    protected <P extends Parameter<T>, T> P addOptional(final Parameter.Factory<P, T, ?> factory) {
        factory.setSyntax(this.courier.format("argument-optional", factory.syntax));
        factory.setIndex(this.parameters.size());

        final P result = factory.build();
        this.parameters.add(new OptionalParameter<T>(factory, result, this.courier));
        return result;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final List<String> transformed = this.transform(args);
        final ExecutionRequest arguments = new ExecutionRequest(sender, command, label, transformed, this.parameters);

        try {
            return this.execute(arguments);

        } catch (final ArgumentParseException e) {
            e.submit(this.courier, RecipientList.Sender.create(sender));
            return false;
        }
    }

    protected List<String> transform(final String... args) {
        this.tokenizer.reset(JoinList.join(Arrays.asList(args)));
        return Arrays.asList(this.tokenizer.getTokenArray());
    }

    /**
     * @return true if command completed successfully
     * @throws ArgumentParseException when an argument can not be parsed
     */
    protected abstract boolean execute(final ExecutionRequest request) throws ArgumentParseException;

}
