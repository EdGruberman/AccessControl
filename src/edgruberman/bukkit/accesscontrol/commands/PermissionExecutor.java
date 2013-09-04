package edgruberman.bukkit.accesscontrol.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
        final PrincipalType type = ( (args.size() >= 3) ? PrincipalType.parse(args.get(2).toLowerCase(Locale.ENGLISH)) : null );

        // group, must exist
        Principal principal = null;
        if (type == null || PrincipalType.GROUP.equals(type)) {
            principal = this.authority.getGroup(name);
            if (principal == null && type != null) {
                Main.courier.send(sender, "unknown-value", "name", name);
                return false;
            }
        }

        // otherwise, assume user
        if (principal == null || PrincipalType.USER.equals(type)) {
            principal = this.authority.createUser(name);
        }


        // identify context
        final List<String> contextArgs;
        if (type != null && (args.size() >= 4)) {
            contextArgs = args.subList(3, args.size());

        } else if (type == null && (args.size() >= 3)) {
            contextArgs = args.subList(2, args.size());

        } else {
            contextArgs = Collections.<String>emptyList();
        }

        // use player context when no explicit descriptor references provided, principal is a user, and player is online
        ExecutionContext context = null;
        if (contextArgs.size() == 0 && principal.getClass().equals(User.class)) {
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





    private static class PrincipalType {

        private static final Map<String, PrincipalType> KNOWN = new HashMap<String, PrincipalType>();

        static final PrincipalType USER = new PrincipalType(User.class);
        static final PrincipalType GROUP = new PrincipalType(Group.class);

        static PrincipalType parse(final String argument) {
            return PrincipalType.KNOWN.get(argument);
        }

        private PrincipalType(final Class<? extends Principal> type) {
            PrincipalType.KNOWN.put(type.getSimpleName().toLowerCase(), this);
        }

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
