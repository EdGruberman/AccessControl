package edgruberman.bukkit.accesscontrol.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Context;
import edgruberman.bukkit.accesscontrol.Context.CommandContext;
import edgruberman.bukkit.accesscontrol.Context.CommandContext.ArgumentSection;
import edgruberman.bukkit.accesscontrol.Descriptor;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.Registrar.Registration;
import edgruberman.bukkit.accesscontrol.User;
import edgruberman.bukkit.accesscontrol.util.TokenizedExecutor;

public abstract class PermissionExecutor extends TokenizedExecutor {

    protected final Authority authority;
    protected final List<Registration> registrations;

    public PermissionExecutor(final Authority authority, final List<Registration> registrations) {
        this.authority = authority;
        this.registrations = registrations;
    }

    // usage: /<command> permission [name] [type] [context]
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < 1) {
            Main.courier.send(sender, "requires-argument", "permission", 0);
            return false;
        }

        if (args.size() < 2 && !(sender instanceof Player)) {
            Main.courier.send(sender, "requires-argument", "name", 0);
            return false;
        }


        // identify target principal
        final String name = ( args.size() >= 2 ? args.get(1).toLowerCase(Locale.ENGLISH) : sender.getName().toLowerCase(Locale.ENGLISH) );
        final String type = ( (args.size() >= 3) ? args.get(2).toLowerCase(Locale.ENGLISH) : User.class.getSimpleName().toLowerCase(Locale.ENGLISH) );

        // explicit group, must exist
        Principal principal = null;
        if (type.equals(Group.class.getSimpleName().toLowerCase(Locale.ENGLISH))) {
            principal = this.authority.getGroup(name);
            if (principal == null) {
                Main.courier.send(sender, "unknown-value", "name", name);
                return false;
            }

        // explicit user, create if new
        } else if (type.equals(User.class.getSimpleName().toLowerCase(Locale.ENGLISH))) {
            principal = this.authority.getUser(name);
            if (principal == null) principal = this.authority.createUser(name);

        // unknown, cancel out
        } else {
            Main.courier.send(sender, "unknown-value", "type", type);
            return false;
        }


        // use player context when no explicit descriptor references provided, type is a user, and player is online
        final List<String> contextArgs = ( args.size() >= 4 ? args.subList(3, args.size()) : Collections.<String>emptyList() );
        ExecutionContext context = null;
        if (contextArgs.size() == 0 && type.equals(User.class.getSimpleName().toLowerCase(Locale.ENGLISH))) {
            final OfflinePlayer target = Bukkit.getOfflinePlayer(name);
            if (target.isOnline()) {
                context = new PlayerExecutionContext(target.getPlayer());

                // cancel when unable to identify primary registration based on no arguments
                if (context.registration() == null) {
                    Main.courier.send(sender, "requires-argument", "context", 0);
                    return false;
                }
            }
        }


        // otherwise attempt to use a standard command context
        if (context == null) {
            final CommandContext cc = new CommandContext(this.registrations, contextArgs);

            // cancel when unable to identify any descriptors (none supplied, or no registered descriptors match)
            if (cc.size() < 1) {
                Main.courier.send(sender, "requires-argument", "context", 0);
                return false;
            }

            context = new ArgumentExecutionContext(cc);
        }


        // permission
        final String permission = args.get(0).toLowerCase(Locale.ENGLISH);


        return this.execute(sender, permission, principal, context);
    }

    public abstract boolean execute(CommandSender sender, String permission, Principal principal, ExecutionContext context);



    protected static String properName(final Principal principal) {
        if (!principal.getClass().equals(User.class)) return principal.getName();
        return Bukkit.getOfflinePlayer(principal.getName()).getName();
    }





    abstract class ExecutionContext implements Context {

        protected final Context context;

        protected ExecutionContext(final Context context) {
            this.context = context;
        }

        @Override
        public Map<String, Boolean> permissions(final Descriptor descriptor) {
            return this.context.permissions(descriptor);
        }

        /** @return reference and arguments used for verbose context for primary registration */
        public List<String> describe() {
            return this.describe(this.registration().getImplementation());
        }

        /** @return reference and arguments used for verbose context in commands */
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
            for (final Registration registration : PermissionExecutor.this.registrations) {
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

        PlayerExecutionContext(final Player context) {
            super(new PlayerContext(context));
            this.player = context;

            // identify first no argument registration to assume for primary
            Registration found = null;
            for (final Registration registration : PermissionExecutor.this.registrations) {
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

        ArgumentExecutionContext(final CommandContext context) {
            super(context);
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

}
