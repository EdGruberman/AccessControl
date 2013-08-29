package edgruberman.bukkit.accesscontrol.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Descriptor;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.Registrar.Registration;
import edgruberman.bukkit.accesscontrol.util.JoinList;

public class Deny extends PermissionExecutor {

    public Deny(final Authority authority, final List<Registration> registrations) {
        super(authority, registrations);
    }

    // usage: /<command> permission [name] [type] [context]
    @Override
    public boolean execute(final CommandSender sender, final String permission, final Principal principal, final ExecutionContext context) {
        final String arguments = context.registration().getReference() + " " + JoinList.join(context.arguments());

        final Descriptor existing = principal.getPermissions(context.registration().getImplementation());
        if (existing != null) {
            final Boolean previous = existing.setPermission(context.arguments(), permission, false);

            principal.save();
            principal.apply();

            if (previous != null && !previous) {
                Main.courier.send(sender, "deny.already", principal.getName(), permission, arguments);
            } else {
                Main.courier.send(sender, "deny.success", principal.getName(), permission, arguments, ( previous == null ? 0 : 1 ));
            }
            return true;
        }

        final Descriptor descriptor = context.registration().getFactory().create();
        descriptor.setPermission(context.arguments(), permission, false);
        principal.addPermissions(descriptor);
        principal.save();
        principal.apply();

        Main.courier.send(sender, "deny.success", principal.getName(), permission, arguments, 0);
        return true;
    }

}
