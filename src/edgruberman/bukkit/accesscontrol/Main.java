package edgruberman.bukkit.accesscontrol;

import java.io.File;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import edgruberman.bukkit.accesscontrol.commands.Check;
import edgruberman.bukkit.accesscontrol.commands.Grant;
import edgruberman.bukkit.accesscontrol.commands.Reload;
import edgruberman.bukkit.accesscontrol.commands.Revoke;
import edgruberman.bukkit.accesscontrol.commands.Unset;
import edgruberman.bukkit.accesscontrol.messaging.ConfigurationCourier;
import edgruberman.bukkit.accesscontrol.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static ConfigurationCourier courier;

    public AccountManager manager = null;

    @Override
    public void onLoad() { this.putConfigMinimum(CustomPlugin.CONFIGURATION_FILE, "5.5.0"); }

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = ConfigurationCourier.create(this).setPath("language").build();

        this.manager = new AccountManager(this, this.getConfig().getBoolean("set-player-name"));
        this.manager.load(this.loadConfig("users.yml", '|', null), this.loadConfig("groups.yml", '|', null));
        this.getLogger().config("Loaded " + this.manager.groups.size() + " groups and "+ this.manager.users.size() + " users");

        this.getCommand("accesscontrol:check").setExecutor(new Check(this, this.manager));
        final Grant grant = new Grant(this, this.manager);
        this.getCommand("accesscontrol:grant").setExecutor(grant);
        this.getCommand("accesscontrol:revoke").setExecutor(new Revoke(grant));
        this.getCommand("accesscontrol:unset").setExecutor(new Unset(this, this.manager));
        this.getCommand("accesscontrol:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        this.manager.unload();
        this.manager = null;
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
            this.getLogger().severe("Unable to save configuration file: " + configFile.getPath() + "; " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

}
