package edgruberman.bukkit.accesscontrol.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.Main;

public class Reload implements CommandExecutor {

    private final Plugin plugin;
    private final AccountManager manager;

    public Reload(final JavaPlugin plugin, final String label, final AccountManager manager) {
        this.plugin = plugin;
        final PluginCommand command = plugin.getCommand(label);
        command.setExecutor(this);
        this.manager = manager;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        this.manager.unload();
        this.manager.load(((Main) this.plugin).loadConfig("users.yml"), ((Main) this.plugin).loadConfig("groups.yml"));
        sender.sendMessage("Reloaded users and groups");
        return true;
    }

}
