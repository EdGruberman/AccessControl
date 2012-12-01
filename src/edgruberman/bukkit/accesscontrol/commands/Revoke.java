package edgruberman.bukkit.accesscontrol.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import edgruberman.bukkit.accesscontrol.Main;

public class Revoke implements CommandExecutor {

    private final Grant grant;

    public Revoke(final Grant grant) {
        this.grant = grant;
    }

    // usage: /<command> <Permission> [ <Principal>[ <World>]][ (+user|+group)]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
        if (args.length < 1) {
            Main.courier.send(sender, "requires-argument", "<Permission>");
            return false;
        }

        if (sender instanceof ConsoleCommandSender && args.length < 2) {
            Main.courier.send(sender, "requires-argument", "<Principal>");
            return false;
        }

        final String principal = ( args.length >= 3 ? args[2] : null );
        final boolean createUser = args[args.length - 1].equalsIgnoreCase("+user");
        final boolean createGroup = args[args.length - 1].equalsIgnoreCase("+group");
        // Strip trailing optional directive if present
        if (args[args.length - 1].equalsIgnoreCase("+user") || args[args.length - 1].equalsIgnoreCase("+group"))
            args = Arrays.copyOf(args, args.length - 1);
        final String world = ( args.length >= 4 ? args[3] : null );
        this.grant.set(sender, args[0], false, principal, world, createUser, createGroup);
        return true;
    }

}
