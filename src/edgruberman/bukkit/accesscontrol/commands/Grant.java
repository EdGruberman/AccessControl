package edgruberman.bukkit.accesscontrol.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Descriptor;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.Registrar.Registration;
import edgruberman.bukkit.accesscontrol.User;
import edgruberman.bukkit.accesscontrol.util.JoinList;

public class Grant extends PermissionExecutor {

    public Grant(final Authority authority, final List<Registration> registrations) {
        super(authority, registrations);
    }

    // usage: /<command> permission [name] [type] [context]
    @Override
    public boolean execute(final CommandSender sender, final String permission, final Principal principal, final ExecutionContext context) {
        Descriptor descriptor = principal.getPermissions(context.registration().getImplementation());
        final boolean existing = (descriptor != null);
        if (!existing) descriptor = context.registration().getFactory().create();

        if (context.registration().getFactory().required().size() > context.arguments().size()) {
            Main.courier.send(sender, "requires-argument", context.registration().getFactory().required().get(context.arguments().size() + 1), 0);
            return false;
        }

        final Boolean previous = descriptor.setPermission(context.arguments(), permission, true);
        if (!existing) principal.addPermissions(descriptor);

        principal.save();
        principal.apply();

        if (previous != null && previous) {
            Main.courier.send(sender, "grant-already", permission, PermissionExecutor.properName(principal)
                    , principal.getClass().equals(User.class)?0:1, JoinList.join(context.describe()));
        } else {
            Main.courier.send(sender, "grant-success", permission, PermissionExecutor.properName(principal)
                    , principal.getClass().equals(User.class)?0:1, JoinList.join(context.describe()), ( previous == null ? 0 : -1 ));
        }

        return true;
    }

}
