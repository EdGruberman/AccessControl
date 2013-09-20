package edgruberman.bukkit.accesscontrol.commands;

import java.util.List;

import org.bukkit.Server;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Descriptor;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.Registrar.Registration;
import edgruberman.bukkit.accesscontrol.User;
import edgruberman.bukkit.accesscontrol.commands.util.ExecutionContext;
import edgruberman.bukkit.accesscontrol.commands.util.ExecutionRequest;
import edgruberman.bukkit.accesscontrol.commands.util.JoinList;
import edgruberman.bukkit.accesscontrol.commands.util.PermissionExecutor;
import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public class Grant extends PermissionExecutor {

    public Grant(final ConfigurationCourier courier, final Authority authority, final List<Registration> registrations, final Server server) {
        super(courier, authority, registrations, server);
    }

    // usage: /<command> permission [name] [type] [context]
    @Override
    public boolean executePermission(final ExecutionRequest request, final String permission, final Principal principal, final ExecutionContext context) {
        Descriptor descriptor = principal.getPermissions(context.registration().getImplementation());
        final boolean existing = (descriptor != null);
        if (!existing) descriptor = context.registration().getFactory().create();

        if (context.registration().getFactory().required().size() > context.arguments().size()) {
            this.courier.send(request.getSender(), "requires-argument", context.registration().getFactory().required().get(context.arguments().size() + 1), 0);
            return false;
        }

        final Boolean previous = descriptor.setPermission(context.arguments(), permission, true);
        if (!existing) principal.addPermissions(descriptor);

        principal.save();
        principal.apply();

        if (previous != null && previous) {
            this.courier.send(request.getSender(), "grant-already", permission, PermissionExecutor.properName(principal)
                    , principal.getClass().equals(User.class)?0:1, JoinList.join(context.describe()));
        } else {
            this.courier.send(request.getSender(), "grant-success", permission, PermissionExecutor.properName(principal)
                    , principal.getClass().equals(User.class)?0:1, JoinList.join(context.describe()), ( previous == null ? 0 : -1 ));
        }

        return true;
    }

}
