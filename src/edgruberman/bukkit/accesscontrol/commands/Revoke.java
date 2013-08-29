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

public class Revoke extends PermissionExecutor {

    public Revoke(final Authority authority, final List<Registration> registrations) {
        super(authority, registrations);
    }

    // usage: /<command> permission [name] [type] [context]
    @Override
    public boolean execute(final CommandSender sender, final String permission, final Principal principal, final ExecutionContext context) {
        final Descriptor existing = principal.getPermissions(context.registration().getImplementation());

        if (existing == null) {
            Main.courier.send(sender, "revoke.already", PermissionExecutor.properName(principal), permission, JoinList.join(context.describe()));
            return true;
        }

        if (context.registration().getFactory().required().size() > context.arguments().size()) {
            Main.courier.send(sender, "requires-argument", context.registration().getFactory().required().get(context.arguments().size() + 1), 0);
            return false;
        }

        final Boolean previous = existing.unsetPermission(context.arguments(), permission);

        // remove descriptor if removed last permission and now empty
        if (previous != null && existing.permissions(context.arguments()).size() == 0) {
            principal.removePermissions(existing.getClass());
        }

        principal.save();
        principal.apply();

        if (previous == null) {
            Main.courier.send(sender, "revoke-already", permission, PermissionExecutor.properName(principal)
                    , principal.getClass().equals(User.class)?0:1, JoinList.join(context.describe()));
        } else {
            Main.courier.send(sender, "revoke-success", permission, PermissionExecutor.properName(principal)
                    , principal.getClass().equals(User.class)?0:1, JoinList.join(context.describe()), ( previous == false ? -1 : 1 ));
        }

        return true;
    }

}
