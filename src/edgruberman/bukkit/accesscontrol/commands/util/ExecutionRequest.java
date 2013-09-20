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
    private final List<Parameter<?>> parameters;

    ExecutionRequest(final CommandSender sender, final Command command, final String label, final List<String> arguments, final List<Parameter<?>> parameters) {
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.arguments = arguments;
        this.parameters = parameters;
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

    /** @return null if argument at index was not supplied */
    public String getArgument(final Parameter<?> parameter) {
        return this.getArgument(parameter.getIndex());
    }

    /** @return null if argument at index was not supplied */
    public String getArgument(final int index) {
        if (index < 0 || index >= this.arguments.size()) return null;
        return this.arguments.get(index);
    }

    public <T> T parse(final Parameter<T> parameter) throws ArgumentContingency {
        @SuppressWarnings("unchecked")
        final T result = (T) this.parse(parameter.getIndex());
        return result;
    }

    public Object parse(final int index) throws ArgumentContingency {
        return this.parameters.get(index).parse(this);
    }

    /** @return true when argument was supplied for parameter */
    public boolean isExplicit(final Parameter<?> parameter) {
        return parameter.getIndex() < this.arguments.size();
    }

}
