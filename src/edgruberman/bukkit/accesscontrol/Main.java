package edgruberman.bukkit.accesscontrol;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.accesscontrol.commands.Check;
import edgruberman.bukkit.accesscontrol.commands.Reload;
import edgruberman.bukkit.accesscontrol.commands.Set;
import edgruberman.bukkit.accesscontrol.commands.Unset;

public final class Main extends JavaPlugin {

    private AccountManager manager = null;

    @Override
    public void onEnable() {
        this.setLoggingLevel(this.getConfig().getString("logLevel", "INFO"));
        this.start(this, this.loadConfig("users.yml"), this.loadConfig("groups.yml"));

        new Check(this, "accesscontrol:check");
        new Set(this, "accesscontrol:set", this.manager);
        new Unset(this, "accesscontrol:unset", this.manager);
        new Reload(this, "accesscontrol:reload", this.manager);
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

    public AccountManager getAccountManager() {
        return this.manager;
    }

    public void start(final Plugin context, final MemoryConfiguration users, final MemoryConfiguration groups) {
        this.manager = new AccountManager(context);
        this.manager.load(users, groups);
        context.getLogger().config("Loaded " + this.manager.groups.size() + " groups and "+ this.manager.users.size() + " users");
    }

    public MemoryConfiguration loadConfig(final String fileName) {
        final YamlConfiguration yaml = new YamlConfiguration();

        final File configFile = new File(this.getDataFolder(), fileName);
        if (!configFile.exists()) return yaml;

        yaml.options().pathSeparator('|');
        try {
            yaml.load(configFile);
        } catch (final Exception e) {
            this.getLogger().severe("Unable to load configuration file: " + configFile.getPath() + "; " + e.getClass().getName() + ": " + e.getMessage());
        }

        return yaml;
    }

    private void saveConfig(final String fileName, final ConfigurationSection root) {
        final YamlConfiguration yaml = new YamlConfiguration();
        for (final String key : root.getKeys(false))
            yaml.set(key, root.get(key));

        final File configFile = new File(this.getDataFolder(), fileName);
        try {
            yaml.save(configFile);
        } catch (final Exception e) {
            this.getLogger().severe("Unable to save configuration file: " + configFile.getPath() + "; " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private void setLoggingLevel(final String name) {
        Level level;
        try { level = Level.parse(name); } catch (final Exception e) {
            level = Level.INFO;
            this.getLogger().warning("Defaulting to " + level.getName() + "; Unrecognized java.util.logging.Level: " + name);
        }

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().config("Logging level set to: " + this.getLogger().getLevel());
    }

}
