package edgruberman.bukkit.accesscontrol.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Context;
import edgruberman.bukkit.accesscontrol.Descriptor;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.Registrar.Registration;
import edgruberman.bukkit.accesscontrol.User;
import edgruberman.bukkit.accesscontrol.commands.util.ExecutionContext;
import edgruberman.bukkit.accesscontrol.commands.util.ExecutionRequest;
import edgruberman.bukkit.accesscontrol.commands.util.JoinList;
import edgruberman.bukkit.accesscontrol.commands.util.PermissionExecutor;
import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public class Trace extends PermissionExecutor {

    private final Server server;

    public Trace(final ConfigurationCourier courier, final Authority authority, final List<Registration> registrations, final Server server) {
        super(courier, authority, registrations, server);
        this.server = server;
    }

    // usage: /<command> permission [name] [type] [context]
    @Override
    public boolean execute(final ExecutionRequest request, final String permission, final Principal principal, final ExecutionContext context) {
        // context
        final List<String> description = new JoinList<String>();
        for (final Registration registration : this.registrations) description.addAll(context.describe(registration.getImplementation()));
        this.courier.send(request.getSender(), "trace-context", description, PermissionExecutor.properName(principal), principal.getClass().equals(User.class)?0:1);

        // trace
        final List<PermissionAssignment> assignments = this.trace(principal, context, permission);
        for (final PermissionAssignment assignment : assignments) {
            // 1 = permission, 2 = principal name, 3 = principal type(0=user|1=group), 4 = value(0=false|1=true), 5 = direct(0=inherited|1=direct)
            // 6 = inherited name, 7 = inherited type(-1=direct|0=user|1=group), 8 = context, 9 = relationship(0=permission|1=parent)
            this.courier.send(request.getSender(), "trace-assignment"
                    , assignment.permission
                    , PermissionExecutor.properName(principal)
                    , principal.getClass().equals(User.class)?0:1
                    , assignment.value?1:0
                    , assignment.source.equals(principal)?1:0
                    , PermissionExecutor.properName(assignment.source)
                    , ( assignment.source.equals(principal) ? -1 : assignment.source.getClass().equals(User.class)?0:1 )
                    , JoinList.join(context.describe(assignment.descriptor.getClass()))
                    , assignment.permission.equals(permission)?0:1
                    );

            this.iterateRelationships(request.getSender(), principal, assignment, permission, assignment.permission, assignment.value);
        }

        // default
        if (assignments.size() == 0) {
            final OfflinePlayer target = ( principal.getClass().equals(User.class) ? this.server.getOfflinePlayer(principal.getName()) : null );
            final Permission instance = this.server.getPluginManager().getPermission(permission);
            final PermissionDefault defaultPermission = ( instance != null ? instance.getDefault() : Permission.DEFAULT_PERMISSION );
            final boolean result = defaultPermission.getValue( target != null ? target.isOp() : false );
            this.courier.send(request.getSender(), "trace-default", permission, PermissionExecutor.properName(principal), principal.getClass().equals(User.class)?0:1, result?1:0, defaultPermission.name());
        }

        if (!principal.isPersistent()) principal.delete();
        return true;
    }

    private void iterateRelationships(final CommandSender sender, final Principal principal, final PermissionAssignment assignment, final String target, final String parent, final boolean parentValue) {
        if (target.equals(parent)) return;

        for (final Map.Entry<String, Boolean> child : this.children(parent).entrySet()) {
            if (!target.equals(child.getKey()) && !this.childMatches(target, this.children(child.getKey()))) continue;

            final boolean childValue = ( parentValue ? child.getValue() : !child.getValue() );

            // 1 = permission, 2 = principal name, 3 = principal type(0=user|1=group), 4 = parent name, 5 = parent value(0=false|1=true)
            // 6 = child name, 7 = child value(0=false|1=true), 8 = relationship(0=permission|1=parent)
            this.courier.send(sender, "trace-relationship"
                    , assignment.permission
                    , PermissionExecutor.properName(principal)
                    , principal.getClass().equals(User.class)?0:1
                    , parent
                    , parentValue?1:0
                    , child.getKey()
                    , childValue?1:0
                    , target.equals(child.getKey())?0:1
                    );

            this.iterateRelationships(sender, principal, assignment, target, child.getKey(), childValue);
        }
    }

    // calculate assignments related to permission; farthest inherited first to closest inherited, then direct
    // mirrors Principal.permissions(Context) logic
    private List<PermissionAssignment> trace(final Principal target, final Context context, final String permission) {
        final List<PermissionAssignment> result = new ArrayList<PermissionAssignment>();

        // inherited
        for (final Group inherited : target.memberships()) {
            result.addAll(this.assignments(inherited, context, permission));
        }

        // direct
        result.addAll(this.assignments(target, context, permission));

        // implicit
        if (target.getImplicit().getName().equals(permission)) {
            result.add(new PermissionAssignment(target, true));
        }

        return result;
    }

    private List<PermissionAssignment> assignments(final Principal source, final Context context, final String permission) {
        final List<PermissionAssignment> result = new ArrayList<PermissionAssignment>();

        for (final Descriptor descriptor : source.getPermissions()) {
            for (final Map.Entry<String, Boolean> entry : context.permissions(descriptor).entrySet()) {
                if (!entry.getKey().equals(permission) && !this.childMatches(permission, this.children(entry.getKey()))) continue;
                result.add(new PermissionAssignment(source, entry.getKey(), entry.getValue(), descriptor));
                break;
            }
        }

        return result;
    }

    private boolean childMatches(final String permission, final Map<String, Boolean> children) {
        for (final Map.Entry<String, Boolean> child : children.entrySet()) {
            if (child.getKey().equals(permission)) return true;
            if (this.childMatches(permission, this.children(child.getKey()))) return true;
        }
        return false;
    }

    private Map<String, Boolean> children(final String permission) {
        final Permission parent = Trace.this.server.getPluginManager().getPermission(permission);
        if (parent == null) return Collections.emptyMap();
        return parent.getChildren();
    }



    private class PermissionAssignment {

        private final Principal source;
        private final String permission;
        private final Boolean value;
        private final Descriptor descriptor;

        private PermissionAssignment(final Principal source, final Boolean value) {
            this(source, source.getImplicit().getName(), value, null);
        }

        private PermissionAssignment(final Principal source, final String permission, final Boolean value, final Descriptor descriptor) {
            this.source = source;
            this.permission = permission;
            this.value = value;
            this.descriptor = descriptor;
        }

    }

}
