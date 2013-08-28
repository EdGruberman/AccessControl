package edgruberman.bukkit.accesscontrol.commands;

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

    private final Authority authority;
    private final List<Registration> registrations;

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

            // identify first no argument registration to assume
            Registration primary = null;
            for (final Registration registration : PermissionExecutor.this.registrations) {
                if (registration.getMinimumArguments() == 0) {
                    primary = registration;
                    break;
                }

                // cancel when unable to identify a descriptor registered that accepts no arguments
                if (primary == null) {
                    Main.courier.send(sender, "requires-argument", "context", 0);
                    return false;
                }
            }

            final OfflinePlayer target = Bukkit.getOfflinePlayer(name);
            if (target.isOnline()) context = new ExecutionContext(target.getPlayer(), primary);
        }


        // otherwise attempt to use a standard command context
        if (context == null) {
            final CommandContext cc = new CommandContext(this.registrations, contextArgs);

            // cancel when unable to identify any descriptors (none supplied, or no registered descriptors match)
            if (cc.size() < 1) {
                Main.courier.send(sender, "requires-argument", "context", 0);
                return false;
            }

            context = new ExecutionContext(cc);
        }


        // permission
        final String permission = args.get(0).toLowerCase(Locale.ENGLISH);


        return this.execute(sender, permission, principal, context);
    }

    public abstract boolean execute(CommandSender sender, String permission, Principal principal, ExecutionContext context);





    static class ExecutionContext implements Context {

        private final Context context;
        private final Registration primaryRegistration;
        private final List<String> primaryArguments;

        ExecutionContext(final Player context, final Registration primary) {
            this.context = new PlayerContext(context);

            this.primaryRegistration = primary;
            this.primaryArguments = primary.getFactory().arguments(context);
        }

        ExecutionContext(final CommandContext context) {
            this.context = context;

            final CommandContext cc = context;
            final ArgumentSection section = cc.getSection(cc.size() - 1);

            this.primaryRegistration = section.getRegistration();
            this.primaryArguments = section.getArguments();
        }

        @Override
        public Map<String, Boolean> permissions(final Descriptor descriptor) {
            return this.context.permissions(descriptor);
        }

        /** @return descriptor registration to use when command only affects a single descriptor */
        public Registration getPrimaryRegistration() {
            return this.primaryRegistration;
        }

        /** @return arguments associated with primary registration */
        public List<String> getPrimaryArguments() {
            return this.primaryArguments;
        }

    }

}
