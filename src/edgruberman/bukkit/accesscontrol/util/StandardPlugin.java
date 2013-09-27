package edgruberman.bukkit.accesscontrol.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.accesscontrol.configuration.ConfigurationDefinition;
import edgruberman.bukkit.accesscontrol.configuration.VersionedYamlConfiguration;
import edgruberman.bukkit.accesscontrol.versioning.StandardVersion;
import edgruberman.bukkit.accesscontrol.versioning.Version;

/**
 * @author EdGruberman (ed@rjump.com)
 * @version 3.0.0
 */
public abstract class StandardPlugin extends JavaPlugin {

    /** default logging level used if none found in plugin config file */
    public static final Level DEFAULT_LOG_LEVEL = Level.INFO;

    /** plugin configuration file entry that contains logging level */
    public static final String KEY_LOG_LEVEL = "log-level";

    /** encoding of configuration files embedded in JAR */
    public static final Charset CONFIGURATION_SOURCE = Charset.forName("UTF-8");

    /** encoding of extracted configuration files in file system */
    public static final Charset CONFIGURATION_TARGET = Charset.defaultCharset();

    /** conflicting configuration files are renamed using this format */
    public static final String CONFIGURATION_ARCHIVE = "{0} - Archive version {1} - {2,date,yyyyMMddHHmmss}.yml"; // 0 = name, 1 = version, 2 = date

    /** default plugin configuration file name relative to data folder */
    public static final String CONFIGURATION_FILE = "config.yml";

    private Version version = null;
    private VersionedYamlConfiguration config = null;
    private final Map<String, ConfigurationDefinition> definitions = new HashMap<String, ConfigurationDefinition>();

    public Version getVersion() {
        if (this.version == null) {
            this.version = StandardVersion.parse(this.getDescription().getVersion());
        }

        return this.version;
    }

    protected void putDefinition(final String resource, final ConfigurationDefinition definition) {
        this.definitions.put(resource, definition);
    }

    @Override
    public VersionedYamlConfiguration getConfig() {
        if (this.config == null) this.reloadConfig();
        return this.config;
    }

    @Override
    public void reloadConfig() {
        this.config = this.loadConfig(StandardPlugin.CONFIGURATION_FILE);
        this.setLogLevel(this.getConfig().getString(StandardPlugin.KEY_LOG_LEVEL));
        this.getLogger().log(Level.FINEST, "YAML configuration file encoding: {0}", StandardPlugin.CONFIGURATION_TARGET);
    }

    /**
     * {@inheritDoc}
     * <p>
     * if file exists, it will be migrated if appropriate
     */
    @Override
    public void saveDefaultConfig() {
        // inherent migration will save as appropriate
        this.loadConfig(StandardPlugin.CONFIGURATION_FILE);
    }

    /**
     * existing file archived and migrated as necessary
     * @param resource file name relative to plugin data folder
     * @return empty configuration if file does not exist
     */
    protected VersionedYamlConfiguration loadConfig(final String resource) {
        return this.loadConfig(resource, this.definitions.get(resource));
    }

    /**
     * existing file archived and migrated as necessary
     * @param name file name relative to plugin data folder
     * @param definition applied if later version than existing;
     * null to prevent migration
     * @return empty configuration if file does not exist
     */
    protected VersionedYamlConfiguration loadConfig(final String name, final ConfigurationDefinition definition) {
        final VersionedYamlConfiguration result = new VersionedYamlConfiguration();

        // parse existing file into YAML configuration object
        final File file = new File(this.getDataFolder(), name);
        if (file.exists()) {
            try {
                result.load(file);
            } catch (final InvalidConfigurationException e) {
                throw new IllegalStateException("unable to load configuration file: " + file.getPath() + " (ensure file is encoded as " + StandardPlugin.CONFIGURATION_TARGET + ")", e);
            } catch (final Exception e) {
                throw new RuntimeException("unable to load configuration file: " + file.getPath(), e);
            }
        }

        // return result if no version checking required
        if (definition == null) return result;

        // same or newer versions can be returned as is
        if (result.getVersion().compareTo(definition.getLatest()) >= 0) return result;

        // older versions should be migrated
        return this.migrateConfig(name, result, definition);
    }

    protected VersionedYamlConfiguration migrateConfig(final String name, final VersionedYamlConfiguration config, final ConfigurationDefinition definition) {
        if (config.conflicts(definition)) this.archiveConfig(name, config);
        config.apply(definition);
        config.setVersion(this.getVersion());

        // save config to file system
        final File file = new File(this.getDataFolder(), name);
        try {
            config.save(file);
        } catch (final IOException e) {
            throw new RuntimeException("unable to save configuration file: " + file.getPath(), e);
        }

        return config;
    }

    /** make a backup copy of an existing configuration file */
    protected void archiveConfig(final String name, final VersionedYamlConfiguration config) { //final String resource, final Version version) {
        final File archive = new File(this.getDataFolder(), MessageFormat.format(StandardPlugin.CONFIGURATION_ARCHIVE, name.replaceAll("(?i)\\.yml$", ""), config.getVersion(), new Date()));
        final File existing = new File(this.getDataFolder(), name);

        if (!existing.renameTo(archive)) {
            throw new IllegalStateException(MessageFormat.format("unable to archive configuration file \"{0}\" with version \"{1}\" to \"{2}\""
                    , existing.getPath(), config.getVersion(), archive.getPath()));
        }

        this.getLogger().log(Level.WARNING, "Archived configuration file \"{0}\" with version \"{1}\" to \"{2}\""
                , new Object[] { existing.getPath(), config.getVersion(), archive.getPath() });
    }

    public void setLogLevel(final String name) {
        Level level;
        try { level = Level.parse(name); } catch (final Exception e) {
            level = StandardPlugin.DEFAULT_LOG_LEVEL;
            this.getLogger().warning("Log level defaulted to " + level.getName() + "; Unrecognized java.util.logging.Level: " + name + "; " + e);
        }

        // only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it
        for (final Handler h : this.getLogger().getParent().getHandlers()) {
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);
        }

        this.getLogger().setLevel(level);
        this.getLogger().log(Level.CONFIG, "Log level set to: {0} ({1,number,#})"
                , new Object[] { this.getLogger().getLevel(), this.getLogger().getLevel().intValue() });
    }

}
