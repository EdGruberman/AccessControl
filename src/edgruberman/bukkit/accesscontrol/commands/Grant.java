package edgruberman.bukkit.accesscontrol.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Descriptor;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.Registrar.Registration;
import edgruberman.bukkit.accesscontrol.util.JoinList;

public class Grant extends PermissionExecutor {

    public Grant(final Authority authority, final List<Registration> registrations) {
        super(authority, registrations);
    }

    // usage: /<command> permission [name] [type] [context]
    @Override
    public boolean execute(final CommandSender sender, final String permission, final Principal principal, final ExecutionContext context) {
        // use last section in case no argument descriptor is assumed and ordered first when no references are explicitly supplied
        final String arguments = context.getPrimaryRegistration().getReference() + " " + JoinList.join(context.getPrimaryArguments());

        final Descriptor existing = principal.getPermissions(context.getPrimaryRegistration().getImplementation());
        if (existing != null) {
            final Boolean previous = existing.setPermission(context.getPrimaryArguments(), permission, true);

            principal.save();
            principal.apply();

            if (previous != null && previous) {
                Main.courier.send(sender, "grant.already", principal.getName(), permission, arguments);
            } else {
                Main.courier.send(sender, "grant.success", principal.getName(), permission, arguments, ( previous == null ? 0 : -1 ));
            }
            return true;
        }

        final Descriptor descriptor = context.getPrimaryRegistration().getFactory().create();
        descriptor.setPermission(context.getPrimaryArguments(), permission, true);
        principal.addPermissions(descriptor);
        principal.save();
        principal.apply();

        Main.courier.send(sender, "grant.success", principal.getName(), permission, arguments, 0);
        return true;
    }

}
