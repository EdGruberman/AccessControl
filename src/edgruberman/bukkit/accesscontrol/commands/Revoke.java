package edgruberman.bukkit.accesscontrol.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Descriptor;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.Registrar.Registration;
import edgruberman.bukkit.accesscontrol.util.JoinList;

public class Revoke extends PermissionExecutor {

    public Revoke(final Authority authority, final List<Registration> registrations) {
        super(authority, registrations);
    }

    // usage: /<command> permission [name] [type] [context]
    @Override
    public boolean execute(final CommandSender sender, final String permission, final Principal principal, final ExecutionContext context) {
        final String arguments = context.getPrimaryRegistration().getReference() + " " + JoinList.join(context.getPrimaryArguments());

        final Descriptor existing = principal.getPermissions(context.getPrimaryRegistration().getImplementation());
        if (existing != null) {
            final Boolean previous = existing.unsetPermission(context.getPrimaryArguments(), permission);

            // remove descriptor if removed last permission and now empty
            if (previous != null && existing.permissions(context.getPrimaryArguments()).size() == 0) {
                principal.removePermissions(existing.getClass());
            }

            principal.save();
            principal.apply();

            if (previous == null) {
                Main.courier.send(sender, "revoke.already", principal.getName(), permission, arguments);
            } else {
                Main.courier.send(sender, "revoke.success", principal.getName(), permission, arguments, ( previous == false ? -1 : 1 ));
            }
            return true;
        }

        Main.courier.send(sender, "revoke.already", principal.getName(), permission, arguments);
        return true;
    }

}
