package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Context.CommandContext;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.Registrar.Registration;
import edgruberman.bukkit.accesscontrol.User;
import edgruberman.bukkit.accesscontrol.commands.util.ExecutionContext.ArgumentExecutionContext;
import edgruberman.bukkit.accesscontrol.commands.util.ExecutionContext.PlayerExecutionContext;
import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public abstract class PermissionExecutor extends Executor {

    protected final Authority authority;
    protected final List<Registration> registrations;
    private final LowerCaseParameter permission;
    private final OfflinePlayerParameter name;
    private final PrincipalClassParameter type;
    private final RemainingParameter context;

    protected PermissionExecutor(final ConfigurationCourier courier, final Authority authority, final List<Registration> registrations, final Server server) {
        super(courier);
        this.authority = authority;
        this.registrations = registrations;

        this.permission = this.addRequired(LowerCaseParameter.Factory.create("permission", courier));
        this.name = this.addOptional(OfflinePlayerParameter.Factory.create("name", courier, server));
        this.type = this.addOptional(PrincipalClassParameter.Factory.create("type", courier));
        this.context = this.addOptional(RemainingParameter.Factory.create("context", courier));
    }

    // usage: /<command> permission [name] [type] [context]
    @Override
    protected boolean execute(final ExecutionRequest request) throws ArgumentParseException {
        final String permission = request.parse(this.permission);
        final Principal principal = this.parsePrincipal(request);
        final ExecutionContext context = this.parseContext(request, principal);
        return this.execute(request, permission, principal, context);
    }

    public abstract boolean execute(ExecutionRequest request, String permission, Principal principal, ExecutionContext context) throws ArgumentParseException;

    protected Principal parsePrincipal(final ExecutionRequest request) throws ArgumentParseException {
        final String name = request.parse(this.name).getName().toLowerCase(Locale.ENGLISH);
        final Class<? extends Principal> type = request.parse(this.type);

        // group, must exist
        Principal result = null;
        if (type == null || type.equals(Group.class)) {
            result = this.authority.getGroup(name);
            if (result == null && type != null) throw new ArgumentUnknownException(request, this.name);
        }

        // otherwise, assume user
        if (result == null) result = this.authority.createUser(name);

        return result;
    }

    protected ExecutionContext parseContext(final ExecutionRequest request, final Principal principal) throws ArgumentParseException {
        final List<String> contextArgs = request.parse(this.context);

        // use player context when no explicit descriptor references provided, principal is a user, and player is online
        ExecutionContext result = null;
        if (contextArgs.isEmpty() && principal.getClass().equals(User.class)) {
            final OfflinePlayer target = Bukkit.getOfflinePlayer(principal.getName());
            if (target.isOnline()) {
                result = new PlayerExecutionContext(target.getPlayer(), this.registrations);

                // cancel when unable to identify primary registration based on no arguments
                if (result.registration() == null) throw new ArgumentMissingException(request, this.context);
            }
        }

        // otherwise use a command context
        if (result == null) {
            final CommandContext cc = new CommandContext(this.registrations, contextArgs);

            // cancel when unable to identify any descriptors (none supplied, or no registered descriptors match)
            if (cc.size() < 1) throw new ArgumentMissingException(request, this.context);

            result = new ArgumentExecutionContext(cc, this.registrations);
        }

        return result;
    }



    protected static String properName(final Principal principal) {
        if (!principal.getClass().equals(User.class)) return principal.getName();
        return Bukkit.getOfflinePlayer(principal.getName()).getName();
    }

}
