package edgruberman.bukkit.accesscontrol;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import edgruberman.bukkit.accesscontrol.commands.Check;
import edgruberman.bukkit.accesscontrol.commands.References;
import edgruberman.bukkit.accesscontrol.commands.Reload;
import edgruberman.bukkit.accesscontrol.commands.group.Create;
import edgruberman.bukkit.accesscontrol.commands.group.Creator;
import edgruberman.bukkit.accesscontrol.commands.group.Members;
import edgruberman.bukkit.accesscontrol.commands.group.controller.Add;
import edgruberman.bukkit.accesscontrol.commands.group.controller.Demote;
import edgruberman.bukkit.accesscontrol.commands.group.controller.Destroy;
import edgruberman.bukkit.accesscontrol.commands.group.controller.Promote;
import edgruberman.bukkit.accesscontrol.commands.group.controller.Remove;
import edgruberman.bukkit.accesscontrol.commands.permission.Deny;
import edgruberman.bukkit.accesscontrol.commands.permission.Grant;
import edgruberman.bukkit.accesscontrol.commands.permission.Revoke;
import edgruberman.bukkit.accesscontrol.messaging.ConfigurationCourier;
import edgruberman.bukkit.accesscontrol.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static final Character CONFIG_PATH_SEPARATOR = '¯';

    public static ConfigurationCourier courier;

    private static AccountManager manager;

    /** used for deserialization */
    static AccountManager getDefaultAccountManager() {
        return Main.manager;
    }



    @Override
    public void onLoad() { this.putConfigMinimum("7.0.1a107"); }

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = ConfigurationCourier.create(this).setPath("language").setColorCode("color-code").build();

        Main.manager = new AccountManager(this, this.getConfig().getString("group-name-build"), this.getConfig().getString("group-name-match"), this.getConfig().getInt("group-name-length"));
        Main.manager.load(this.loadConfig("users.yml", Main.CONFIG_PATH_SEPARATOR, null), this.loadConfig("groups.yml", Main.CONFIG_PATH_SEPARATOR, null));
        Bukkit.getPluginManager().registerEvents(Main.manager, this);
        this.getLogger().log(Level.CONFIG, "Loaded {0} groups and {1} users", new Object[] { Main.manager.groups.size(), Main.manager.users.size() });

        // general commands
        this.getCommand("accesscontrol:check").setExecutor(new Check(this, Main.manager));
        this.getCommand("accesscontrol:principal").setExecutor(new edgruberman.bukkit.accesscontrol.commands.Principal(Main.manager));
        this.getCommand("accesscontrol:references").setExecutor(new References(Main.manager));
        this.getCommand("accesscontrol:reload").setExecutor(new Reload(this));

        // group commands
        final CommandExecutor creator = new Creator(Main.manager, this.getConfig().getInt("group-create-max"));
        this.getCommand("accesscontrol:creator").setExecutor(creator);
        this.getCommand("accesscontrol:create").setExecutor(new Create(this, Main.manager, this.getConfig().getInt("group-create-max"), creator));
        this.getCommand("accesscontrol:members").setExecutor(new Members(Main.manager, creator));

        // restricted group controller commands
        this.getCommand("accesscontrol:add").setExecutor(new Add(this, Main.manager));
        this.getCommand("accesscontrol:demote").setExecutor(new Demote(this, Main.manager));
        this.getCommand("accesscontrol:destroy").setExecutor(new Destroy(this, Main.manager));
        this.getCommand("accesscontrol:promote").setExecutor(new Promote(this, Main.manager));
        this.getCommand("accesscontrol:remove").setExecutor(new Remove(this, Main.manager));

        // permission commands
        this.getCommand("accesscontrol:deny").setExecutor(new Deny(this, Main.manager));
        this.getCommand("accesscontrol:grant").setExecutor(new Grant(this, Main.manager));
        this.getCommand("accesscontrol:revoke").setExecutor(new Revoke(this, Main.manager));
    }

    @Override
    public void onDisable() {
        Main.manager.clear(); Main.manager = null;
        Main.courier = null;
    }

    public void save() {
        this.saveConfig("users.yml", Main.manager.toConfigurationUsers());
        this.saveConfig("groups.yml", Main.manager.toConfigurationGroups());
    }

    private void saveConfig(final String fileName, final Configuration config) {
        final YamlConfiguration yaml = new YamlConfiguration();
        for (final String key : config.getKeys(false)) yaml.set(key, config.get(key));
        final File configFile = new File(this.getDataFolder(), fileName);
        try {
            yaml.save(configFile);
        } catch (final Exception e) {
            this.getLogger().severe("Unable to save configuration file: " + configFile.getPath() + "; " + e);
        }
    }

}
