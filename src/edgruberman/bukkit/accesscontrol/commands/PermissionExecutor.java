package edgruberman.bukkit.accesscontrol.commands;

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
import edgruberman.bukkit.accesscontrol.Context.CommandContext;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.Registrar.Registration;
import edgruberman.bukkit.accesscontrol.User;

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
                context = new PlayerExecutionContext(target.getPlayer(), this.registrations);

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

            context = new ArgumentExecutionContext(cc, this.registrations);
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

}
