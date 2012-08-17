package edgruberman.bukkit.accesscontrol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.accesscontrol.commands.Check;
import edgruberman.bukkit.accesscontrol.commands.Reload;
import edgruberman.bukkit.accesscontrol.commands.Set;
import edgruberman.bukkit.accesscontrol.commands.Unset;
import edgruberman.bukkit.accesscontrol.messaging.ConfigurationCourier;
import edgruberman.bukkit.accesscontrol.messaging.Courier;

public final class Main extends JavaPlugin {

    private static final Version MINIMUM_CONFIGURATION = new Version("5.0.0");

    public static Courier courier;

    public AccountManager manager = null;

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = ConfigurationCourier.Factory.create(this).setBase("messages").build();

        this.manager = new AccountManager(this, this.getConfig().getBoolean("setPlayerName"));
        this.manager.load(this.loadConfig("users.yml"), this.loadConfig("groups.yml"));
        this.getLogger().config("Loaded " + this.manager.groups.size() + " groups and "+ this.manager.users.size() + " users");

        this.getCommand("accesscontrol:check").setExecutor(new Check(this, this.manager));
        this.getCommand("accesscontrol:set").setExecutor(new Set(this, this.manager));
        this.getCommand("accesscontrol:unset").setExecutor(new Unset(this, this.manager));
        this.getCommand("accesscontrol:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        this.manager.unload();
        this.manager = null;
    }

    @Override
    public void reloadConfig() {
        this.saveDefaultConfig();
        super.reloadConfig();
        this.setLogLevel(this.getConfig().getString("logLevel"));

        final Version version = new Version(this.getConfig().isSet("version") ? this.getConfig().getString("version") : null);
        if (version.compareTo(Main.MINIMUM_CONFIGURATION) >= 0) return;

        this.archiveConfig("config.yml", version);
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    @Override
    public void saveDefaultConfig() {
        this.extractConfig("config.yml", false);
    }

    private void archiveConfig(final String resource, final Version version) {
        final String backupName = "%1$s - Archive version %2$s - %3$tY%3$tm%3$tdT%3$tH%3$tM%3$tS.yml";
        final File backup = new File(this.getDataFolder(), String.format(backupName, resource.replaceAll("(?i)\\.yml$", ""), version, new Date()));
        final File existing = new File(this.getDataFolder(), resource);

        if (!existing.renameTo(backup))
            throw new IllegalStateException("Unable to archive configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");

        this.getLogger().warning("Archived configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");
    }

    private void extractConfig(final String resource, final boolean replace) {
        final Charset source = Charset.forName("UTF-8");
        final Charset target = Charset.defaultCharset();
        if (target.equals(source)) {
            this.saveResource(resource, replace);
            return;
        }

        final File config = new File(this.getDataFolder(), resource);
        if (config.exists()) return;

        final char[] cbuf = new char[1024]; int read;
        try {
            final Reader in = new BufferedReader(new InputStreamReader(this.getResource(resource), source));
            final Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config), target));
            while((read = in.read(cbuf)) > 0) out.write(cbuf, 0, read);
            out.close(); in.close();

        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not extract configuration file \"" + resource + "\" to " + config.getPath() + "\";" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void save() {
        final ConfigurationSection config = this.manager.export();
        this.saveConfig("users.yml", config.getConfigurationSection("users"));
        this.saveConfig("groups.yml", config.getConfigurationSection("groups"));
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

    private void setLogLevel(final String name) {
        Level level;
        try { level = Level.parse(name); } catch (final Exception e) {
            level = Level.INFO;
            this.getLogger().warning("Log level defaulted to " + level.getName() + "; Unrecognized java.util.logging.Level: " + name);
        }

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().config("Log level set to: " + this.getLogger().getLevel());
    }

}
