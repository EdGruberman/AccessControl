package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public class Reload implements CommandExecutor {

    private final ConfigurationCourier courier;
    private final Plugin plugin;

    public Reload(final ConfigurationCourier courier, final Plugin plugin) {
        this.courier = courier;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        this.plugin.onDisable();
        this.plugin.onEnable();
        this.courier.send(sender, "reload", this.plugin.getName());
        return true;
    }

}
