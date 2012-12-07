package edgruberman.bukkit.accesscontrol;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import edgruberman.bukkit.accesscontrol.commands.Add;
import edgruberman.bukkit.accesscontrol.commands.Check;
import edgruberman.bukkit.accesscontrol.commands.Create;
import edgruberman.bukkit.accesscontrol.commands.Creator;
import edgruberman.bukkit.accesscontrol.commands.Demote;
import edgruberman.bukkit.accesscontrol.commands.Destroy;
import edgruberman.bukkit.accesscontrol.commands.Grant;
import edgruberman.bukkit.accesscontrol.commands.Members;
import edgruberman.bukkit.accesscontrol.commands.Promote;
import edgruberman.bukkit.accesscontrol.commands.References;
import edgruberman.bukkit.accesscontrol.commands.Reload;
import edgruberman.bukkit.accesscontrol.commands.Remove;
import edgruberman.bukkit.accesscontrol.commands.Revoke;
import edgruberman.bukkit.accesscontrol.commands.Unset;
import edgruberman.bukkit.accesscontrol.messaging.ConfigurationCourier;
import edgruberman.bukkit.accesscontrol.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static final Character UNLIKELY = '¯';

    public static ConfigurationCourier courier;

    private AccountManager manager;

    @Override
    public void onLoad() { this.putConfigMinimum(CustomPlugin.CONFIGURATION_FILE, "6.0.0a37"); }

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = ConfigurationCourier.create(this).setPath("language").setColorCode("color-code").build();

        this.manager = new AccountManager(this, this.getConfig().getBoolean("set-player-name")
                , this.getConfig().getString("group-name-match"), this.getConfig().getString("group-name-build")
                , this.loadConfig("users.yml", Main.UNLIKELY, null), this.loadConfig("groups.yml", Main.UNLIKELY, null));
        Bukkit.getPluginManager().registerEvents(this.manager, this);
        this.getLogger().log(Level.CONFIG, "Loaded {0} groups and {1} users", new Object[] { this.manager.groups.size(), this.manager.users.size() });

        this.getCommand("accesscontrol:check").setExecutor(new Check(this, this.manager));
        this.getCommand("accesscontrol:principal").setExecutor(new edgruberman.bukkit.accesscontrol.commands.Principal(this.manager));
        this.getCommand("accesscontrol:references").setExecutor(new References(this.manager));
        final Grant grant = new Grant(this, this.manager);
        this.getCommand("accesscontrol:grant").setExecutor(grant);
        this.getCommand("accesscontrol:revoke").setExecutor(new Revoke(grant));
        this.getCommand("accesscontrol:unset").setExecutor(new Unset(this, this.manager));
        this.getCommand("accesscontrol:promote").setExecutor(new Promote(this, this.manager));
        this.getCommand("accesscontrol:demote").setExecutor(new Demote(this, this.manager));
        this.getCommand("accesscontrol:members").setExecutor(new Members(this.manager));
        this.getCommand("accesscontrol:add").setExecutor(new Add(this, this.manager));
        this.getCommand("accesscontrol:remove").setExecutor(new Remove(this, this.manager));
        this.getCommand("accesscontrol:create").setExecutor(new Create(this, this.manager, this.getConfig().getInt("group-create-max")));
        this.getCommand("accesscontrol:creator").setExecutor(new Creator(this.manager));
        this.getCommand("accesscontrol:destroy").setExecutor(new Destroy(this, this.manager));
        this.getCommand("accesscontrol:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        this.manager.clear(); this.manager = null;
    }

    public AccountManager getAccountManager() {
        return this.manager;
    }

    public void save() {
        final ConfigurationSection config = this.manager.export();
        this.saveConfig("users.yml", config.getConfigurationSection("users"));
        this.saveConfig("groups.yml", config.getConfigurationSection("groups"));
    }

    private void saveConfig(final String fileName, final ConfigurationSection root) {
        final YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().indent(4);
        yaml.options().pathSeparator('|');
        for (final String key : root.getKeys(false))
            yaml.set(key, root.get(key));

        final File configFile = new File(this.getDataFolder(), fileName);
        try {
            yaml.save(configFile);
        } catch (final Exception e) {
            this.getLogger().severe("Unable to save configuration file: " + configFile.getPath() + "; " + e);
        }
    }

}
