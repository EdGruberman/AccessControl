package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ExecutionRequest {

    private final CommandSender sender;
    private final Command command;
    private final String label;
    private final List<String> arguments;

    ExecutionRequest(final CommandSender sender, final Command command, final String label, final List<String> arguments) {
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.arguments = arguments;
    }

    public CommandSender getSender() {
        return this.sender;
    }

    public Command getCommand() {
        return this.command;
    }

    public String getLabel() {
        return this.label;
    }

    public List<String> getArguments() {
        return Collections.unmodifiableList(this.arguments);
    }

    /** @return empty if no arguments were not supplied */
    public List<String> getArguments(final Parameter<?> parameter) {
        final int begin = parameter.getBegin();
        if (begin >= this.arguments.size()) return Collections.emptyList();
        final int end = Math.min(parameter.getEnd(), this.arguments.size());
        return this.arguments.subList(begin, end);
    }

    /** @return null if argument at index was not supplied */
    public String getArgument(final int index) {
        if (index < 0 || index >= this.arguments.size()) return null;
        return this.arguments.get(index);
    }

    /** @throws ArgumentContingency when the value can not be parsed */
    public <T> T parse(final Parameter<T> parameter) throws ArgumentContingency {
        if (parameter.required && !this.isExplicit(parameter)) throw new MissingArgumentContingency(this, parameter);
        final T result = parameter.parse(this);
        return ( result != null ? result : parameter.getDefaultValue() );
    }

    /** @return true when at least one argument was supplied for parameter */
    public boolean isExplicit(final Parameter<?> parameter) {
        return parameter.getBegin() < this.arguments.size();
    }

}
