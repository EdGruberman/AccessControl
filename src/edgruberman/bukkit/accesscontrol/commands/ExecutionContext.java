package edgruberman.bukkit.accesscontrol.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import edgruberman.bukkit.accesscontrol.Context;
import edgruberman.bukkit.accesscontrol.Context.CommandContext.ArgumentSection;
import edgruberman.bukkit.accesscontrol.Descriptor;
import edgruberman.bukkit.accesscontrol.Registrar.Registration;

public abstract class ExecutionContext implements Context {

    protected final Context context;
    protected final List<Registration> registrations;

    protected ExecutionContext(final Context context, final List<Registration> registrations) {
        this.context = context;
        this.registrations = registrations;
    }

    @Override
    public Map<String, Boolean> permissions(final Descriptor descriptor) {
        return this.context.permissions(descriptor);
    }

    /** @return reference and arguments used for verbose context for primary registration */
    public List<String> describe() {
        return this.describe(this.registration().getImplementation());
    }

    /** @return reference and arguments used for verbose context in commands; empty when no context found for implementation */
    public List<String> describe(final Class<? extends Descriptor> implementation) {
        final List<String> result = new ArrayList<String>();

        final List<String> arguments = this.arguments(implementation);
        if (arguments != null) {
            result.add(this.registration(implementation).getReference());
            result.addAll(arguments);
        }

        return result;
    }

    /** @return descriptor registration to use when command only affects a single descriptor; null when unable to determine */
    public abstract Registration registration();

    /** @return registration for the specified implementation; null when none apply */
    public Registration registration(final Class<? extends Descriptor> implementation) {
        for (final Registration registration : this.registrations) {
            if (!registration.getImplementation().equals(implementation)) continue;
            return registration;
        }
        return null;
    }

    /** @return arguments associated with primary registration */
    public abstract List<String> arguments();

    /** @return command arguments that represent player context for the specified implementation; null when no registration applies */
    public abstract List<String> arguments(final Class<? extends Descriptor> implementation);

}





class PlayerExecutionContext extends ExecutionContext {

    private final Player player;
    private final Registration primary;

    PlayerExecutionContext(final Player context, final List<Registration> registrations) {
        super(new PlayerContext(context), registrations);
        this.player = context;

        // identify first no argument registration to assume for primary
        Registration found = null;
        for (final Registration registration : this.registrations) {
            if (registration.getFactory().required().size() == 0) {
                found = registration;
                break;
            }
        }
        this.primary = found;
    }

    @Override
    public Registration registration() {
        return this.primary;
    }

    @Override
    public List<String> arguments() {
        return this.primary.getFactory().arguments(this.player);
    }

    @Override
    public List<String> arguments(final Class<? extends Descriptor> implementation) {
        final Registration registration = this.registration(implementation);
        if (registration == null) return null;
        return registration.getFactory().arguments(this.player);
    }

}





class ArgumentExecutionContext extends ExecutionContext {

    private final ArgumentSection primary;

    ArgumentExecutionContext(final CommandContext context, final List<Registration> registrations) {
        super(context, registrations);
        this.primary = context.getSection(context.size() - 1);
    }

    @Override
    public Registration registration() {
        return this.primary.getRegistration();
    }

    @Override
    public List<String> arguments() {
        return this.primary.getArguments();
    }

    @Override
    public List<String> arguments(final Class<? extends Descriptor> implementation) {
        final CommandContext cc = (CommandContext) this.context;
        final ArgumentSection section = cc.getSection(implementation);
        if (section == null) return null;
        return section.getArguments();
    }

}
