package edgruberman.bukkit.accesscontrol;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.accesscontrol.commands.PlayerAddPermission;
import edgruberman.bukkit.accesscontrol.commands.PlayerCheck;
import edgruberman.bukkit.accesscontrol.commands.PlayerRemovePermission;

public final class Main extends JavaPlugin {

    private AccountManager manager = null;
    private YamlConfiguration config = null;

    @Override
    public void onEnable() {
        this.setLoggingLevel(this.getConfig().getString("logLevel", "INFO"));
        this.start(this, this.getConfig());

        new PlayerCheck(this, "accesscontrol:player.check", this.manager);
        new PlayerAddPermission(this, "accesscontrol:player.addpermission", this.manager, this.getCommand("accesscontrol:player.check"));
        new PlayerRemovePermission(this, "accesscontrol:player.removepermission", this.manager, this.getCommand("accesscontrol:player.check"));
    }

    @Override
    public void onDisable() {
        this.manager.disable();
        this.save(this.getConfig());
        this.manager = null;
        this.config = null;
    }

    @Override
    public FileConfiguration getConfig() {
        if (this.config == null) this.reloadConfig();
        return this.config;
    }

    @Override
    public void reloadConfig() {
        final File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) this.saveDefaultConfig();

        this.config = new YamlConfiguration();
        this.config.options().pathSeparator('|');
        try {
            this.config.load(configFile);
        } catch (final Exception e) {
            this.getLogger().severe("Unable to load configuration file: " + configFile.getPath() + "; " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public AccountManager getAccountManager() {
        return this.manager;
    }

    public void start(final Plugin context, final ConfigurationSection config) {
        this.manager = new AccountManager(context);

        final ConfigurationSection groups = config.getConfigurationSection("groups");
        if (groups != null)
            for (final String name : groups.getKeys(false)) {
                final Group group = this.manager.createGroup(name);
                this.loadPrincipal(group, groups.getConfigurationSection(name));
                if (groups.getConfigurationSection(name).getBoolean("default")) group.setDefault(true);
            }
        this.getLogger().config("Loaded " + this.manager.groups.size() + " groups");

        final ConfigurationSection users = config.getConfigurationSection("users");
        if (users != null)
            for (final String name : users.getKeys(false)) {
                final User user = this.manager.createUser(name);
                this.loadPrincipal(user, users.getConfigurationSection(name));
            }
        this.getLogger().config("Loaded " + this.manager.users.size() + " users");

        this.manager.enable();
    }

    private void loadPrincipal(final Principal principal, final ConfigurationSection config) {
        if (config == null) return;

        final ConfigurationSection permissions = config.getConfigurationSection("permissions");
        final List<String> memberships = config.getStringList("memberships");
        final ConfigurationSection worlds = config.getConfigurationSection("worlds");
        if (permissions == null && memberships == null && worlds == null) return;

        // Server Permissions
        if (permissions != null)
            for (final String permission : permissions.getKeys(false)) {
                Boolean value = null;
                if (permissions.isBoolean(permission)) value = permissions.getBoolean(permission);
                principal.addPermission(permission, value);
            }

        // Server Memberships
        if (memberships != null)
            for (final String name : memberships)
                principal.addMembership(this.manager.createGroup(name));

        // World Permissions
        if (worlds != null)
            for (final String name : worlds.getKeys(false)) {
                final ConfigurationSection world = worlds.getConfigurationSection(name);
                if (world != null) {
                    final ConfigurationSection worldPermissions = world.getConfigurationSection("permissions");
                    if (worldPermissions != null)
                        for (final String permission : worldPermissions.getKeys(false)) {
                            Boolean value = null;
                            if (worldPermissions.isBoolean(permission)) value = worldPermissions.getBoolean(permission);
                            principal.addPermission(permission, value);
                        }
                }
            }
    }

    void save(final ConfigurationSection config) {
        final ConfigurationSection groups = config.createSection("groups");
        for (final Group group : this.manager.groups.values())
            this.savePrincipal(group, groups);

        final ConfigurationSection users = config.createSection("users");
        for (final User user : this.manager.users.values())
            if (!user.isTemporary())
                this.savePrincipal(user, users);

        this.saveConfig();
    }

    private void savePrincipal(final Principal principal, final ConfigurationSection config) {
        final ConfigurationSection section = config.createSection(principal.name);

        if (principal instanceof Group)
            if (((Group) principal).isDefault())
                section.set("default", true);

        section.set("permissions", principal.permissions.get(null));

        final List<String> memberships = new ArrayList<String>();
        for (final Group group : principal.memberships) memberships.add(group.name);
        if (memberships.size() != 0) section.set("memberships", memberships);

        // TODO save world permissions
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
