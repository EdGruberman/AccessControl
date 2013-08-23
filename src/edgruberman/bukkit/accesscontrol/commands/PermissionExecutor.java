package edgruberman.bukkit.accesscontrol.commands;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Context.CommandContext;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Main;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.Registrar.Registration;
import edgruberman.bukkit.accesscontrol.User;
import edgruberman.bukkit.accesscontrol.util.TokenizedExecutor;

public abstract class PermissionExecutor extends TokenizedExecutor {

    private final Authority authority;
    private final List<Registration> registrations;

    public PermissionExecutor(final Authority authority, final List<Registration> registrations) {
        this.authority = authority;
        this.registrations = registrations;
    }

    // usage: /<command> permission [name] [type] [context]
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < 1) {
            Main.courier.send(sender, "requires-argument", "permission", 0);
            return false;
        }

        if (args.size() < 2 && !(sender instanceof Player)) {
            Main.courier.send(sender, "requires-argument", "name", 0);
            return false;
        }

        final String permission = args.get(0).toLowerCase(Locale.ENGLISH);
        final String name = ( args.size() >= 2 ? args.get(1).toLowerCase(Locale.ENGLISH) : sender.getName().toLowerCase(Locale.ENGLISH) );
        final String type = ( (args.size() >= 3) ? args.get(2).toLowerCase(Locale.ENGLISH) : User.class.getSimpleName().toLowerCase(Locale.ENGLISH) );
        final List<String> contextArgs = ( args.size() >= 4 ? args.subList(3, args.size()) : Collections.<String>emptyList() );
        final CommandContext context = new CommandContext(this.registrations, contextArgs);

        Principal principal = null;
        if (type.equals(Group.class.getSimpleName().toLowerCase(Locale.ENGLISH))) {
            principal = this.authority.getGroup(name);
            if (principal == null) {
                Main.courier.send(sender, "unknown-value", "name type", name + " " + type);
                return false;
            }

        } else if (type.equals(User.class.getSimpleName().toLowerCase(Locale.ENGLISH))) {
            principal = this.authority.getUser(name);
            if (principal == null) principal = this.authority.createUser(name);

        } else {
            principal = this.authority.getUser(name);
            if (principal == null) principal = this.authority.getGroup(name);
            if (principal == null) principal = this.authority.createUser(name);
        }

        return this.execute(sender, permission, principal, context);
    }

    public abstract boolean execute(CommandSender sender, String permission, Principal principal, CommandContext context);

}
