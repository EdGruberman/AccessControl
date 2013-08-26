package edgruberman.bukkit.accesscontrol.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Context.CommandContext;
import edgruberman.bukkit.accesscontrol.Context.CommandContext.ArgumentSection;
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
    public boolean execute(final CommandSender sender, final String permission, final Principal principal, final CommandContext context) {
        if (context.size() < 1) {
            Main.courier.send(sender, "requires-argument", "context", 0);
            return false;
        }

        // use last section in case no argument descriptor is assumed and ordered first when no references are explicitly supplied
        final ArgumentSection section = context.getSection(context.size() - 1);
        final String arguments = section.getRegistration().getReference() + " " + JoinList.join(section.getArguments());

        final Descriptor existing = principal.getPermissions(section.getRegistration().getImplementation());
        if (existing != null) {
            final Boolean previous = existing.setPermission(section.getArguments(), permission, false);

            principal.save();
            principal.apply();

            if (previous != null && !previous) {
                Main.courier.send(sender, "deny.already", principal.getName(), permission, arguments);
            } else {
                Main.courier.send(sender, "deny.success", principal.getName(), permission, arguments, ( previous == null ? 0 : 1 ));
            }
            return true;
        }

        final Descriptor descriptor = section.getRegistration().getFactory().create();
        descriptor.setPermission(section.getArguments(), permission, false);
        principal.addPermissions(descriptor);
        principal.save();
        principal.apply();

        Main.courier.send(sender, "deny.success", principal.getName(), permission, arguments, 0);
        return true;
    }

}
