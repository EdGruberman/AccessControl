package edgruberman.bukkit.accesscontrol.commands;

import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.Principal.PermissionAssignment;
import edgruberman.bukkit.accesscontrol.Registrar.Registration;
import edgruberman.bukkit.accesscontrol.User;
import edgruberman.bukkit.accesscontrol.util.JoinList;

public class Trace extends PermissionExecutor {

    private final Server server;

    public Trace(final Authority authority, final List<Registration> registrations, final Server server) {
        super(authority, registrations);
        this.server = server;
    }

    // usage: /<command> permission [name] [type] [context]
    @Override
    public boolean execute(final CommandSender sender, final String permission, final Principal principal, final ExecutionContext context) {
        // context
        Main.courier.send(sender, "trace.context", JoinList.join(context.describe()), PermissionExecutor.properName(principal), principal.getClass().equals(User.class)?0:1);

        // trace
        final List<PermissionAssignment> assignments = principal.trace(context, permission);
        for (final PermissionAssignment assignment : assignments) {
            Main.courier.send(sender, "trace.assignment", permission, PermissionExecutor.properName(principal), principal.getClass().equals(User.class)?0:1
                    , assignment.getValue()?1:0, assignment.getSource().equals(principal)?1:0, PermissionExecutor.properName(assignment.getSource())
                    , assignment.getSource().getClass().equals(User.class)?0:1, JoinList.join(context.describe(assignment.getDescriptor().getClass())));
        }

        // default
        if (assignments.size() == 0) {
            final OfflinePlayer target = ( principal.getClass().equals(User.class) ? this.server.getOfflinePlayer(principal.getName()) : null );
            final Permission instance = this.server.getPluginManager().getPermission(permission);
            final PermissionDefault defaultPermission = ( instance != null ? instance.getDefault() : Permission.DEFAULT_PERMISSION );
            final boolean result = defaultPermission.getValue( target != null ? target.isOp() : false );
            Main.courier.send(sender, "trace.default", permission, PermissionExecutor.properName(principal), principal.getClass().equals(User.class)?0:1, result?1:0, defaultPermission.name());
        }

        if (!principal.isPersistent()) principal.delete();
        return true;
    }

}
