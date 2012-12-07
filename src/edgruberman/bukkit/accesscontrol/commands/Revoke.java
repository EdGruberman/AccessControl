package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.accesscontrol.Main;

public class Revoke implements CommandExecutor {

    private final Grant grant;

    public Revoke(final Grant grant) {
        this.grant = grant;
    }

    // usage: /<command> <Permission>[ (<Player>|<Principal>[ <World>])]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            Main.courier.send(sender, "requires-argument", "<Permission>");
            return false;
        }

        if (!(sender instanceof Player) && args.length < 2) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final String principal = ( args.length >= 3 ? args[2] : null );
        final String world = ( args.length >= 4 ? args[3] : null );
        this.grant.set(sender, args[0], false, principal, world);
        return true;
    }

}
