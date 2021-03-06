package edgruberman.bukkit.accesscontrol;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import edgruberman.bukkit.accesscontrol.commands.Check;
import edgruberman.bukkit.accesscontrol.commands.Default;
import edgruberman.bukkit.accesscontrol.commands.Deny;
import edgruberman.bukkit.accesscontrol.commands.Effective;
import edgruberman.bukkit.accesscontrol.commands.Grant;
import edgruberman.bukkit.accesscontrol.commands.Limit;
import edgruberman.bukkit.accesscontrol.commands.Reload;
import edgruberman.bukkit.accesscontrol.commands.Revoke;
import edgruberman.bukkit.accesscontrol.commands.Trace;
import edgruberman.bukkit.accesscontrol.descriptors.Server;
import edgruberman.bukkit.accesscontrol.descriptors.World;
import edgruberman.bukkit.accesscontrol.events.AuthorityInitializeEvent;
import edgruberman.bukkit.accesscontrol.events.AuthorityReleaseEvent;
import edgruberman.bukkit.accesscontrol.events.DescriptorRegistrationEvent;
import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.accesscontrol.repositories.YamlRepository;
import edgruberman.bukkit.accesscontrol.util.StandardPlugin;

public final class Main extends StandardPlugin implements Listener {

    public static final String REFERENCE_SERVER = "server";
    public static final String REFERENCE_WORLD = "world";

    private static Authority authority = null;

    public static Authority getAuthority() {
        return Main.authority;
    }



    @Override
    public void onLoad() {
        this.putDefinition(StandardPlugin.CONFIGURATION_FILE, Configuration.getDefinition(StandardPlugin.CONFIGURATION_FILE));
        this.putDefinition("language.yml", Configuration.getDefinition("language.yml"));
    }

    @Override
    public void onEnable() {
        this.reloadConfig();
        final ConfigurationCourier courier = ConfigurationCourier.Factory.create(this).setBase(this.loadConfig("language.yml")).setFormatCode("format-code").build();

        // default descriptors
        this.getServer().getPluginManager().registerEvents(this, this);

        // descriptor registrar
        final Registrar registrar = new Registrar(this, this.getConfig().getStringList("permission-order"));

        // repository
        final String implicitUser = this.getConfig().getConfigurationSection("implicit-permission").getString("user");
        final String implicitGroup = this.getConfig().getConfigurationSection("implicit-permission").getString("group");
        final File users = new File(this.getDataFolder(), "users.yml");
        final File groups = new File(this.getDataFolder(), "groups.yml");
        Repository repository;
        try {
            repository = new YamlRepository(this, users, groups, registrar.getRegistrations(), registrar.getSorter(), this.getServer().getPluginManager(), implicitUser, implicitGroup);
        } catch (final InvalidConfigurationException e) {
            this.setEnabled(false);
            this.getLogger().log(Level.SEVERE, "Disabling plugin; Unable to load repository: " + e);
            this.getLogger().log(Level.FINE, "Exception detail", e);
            return;
        }

        // authority
        Main.authority = new Authority(this, repository, registrar.getSorter(), implicitUser, implicitGroup, this.getConfig().getStringList("default-groups"));
        this.getServer().getPluginManager().registerEvents(Main.authority, this);
        this.getLogger().log(Level.CONFIG, "Loaded {0} groups and {1} users", new Object[] { Main.authority.getGroups().size(), Main.authority.getUsers().size() });

        // analysis commands
        this.getCommand("accesscontrol:check").setExecutor(new Check(courier, this.getServer()));
        this.getCommand("accesscontrol:effective").setExecutor(new Effective(courier, this.getServer()));
        this.getCommand("accesscontrol:trace").setExecutor(new Trace(courier, Main.authority, registrar.registrations(), this.getServer()));
        this.getCommand("accesscontrol:default").setExecutor(new Default(courier, this.getServer()));

        // permission commands
        this.getCommand("accesscontrol:deny").setExecutor(new Deny(courier, Main.authority, registrar.registrations(), this.getServer()));
        this.getCommand("accesscontrol:grant").setExecutor(new Grant(courier, Main.authority, registrar.registrations(), this.getServer()));
        this.getCommand("accesscontrol:revoke").setExecutor(new Revoke(courier, Main.authority, registrar.registrations(), this.getServer()));

        // group commands
        final File limits = new File(this.getDataFolder(), "limits.yml");
        final int defaultLimit = this.getConfig().getInt("create-limit-default");
        this.getCommand("accesscontrol:limit").setExecutor(new Limit(courier, this.getServer(), Main.authority, this, limits, defaultLimit));

        // general commands
        this.getCommand("accesscontrol:reload").setExecutor(new Reload(courier, this));
    }

    @Override
    public void onDisable() {
        if (Main.authority != null) {
            HandlerList.unregisterAll(Main.authority);
            Main.authority.release();
            Main.authority = null;
        }

        HandlerList.unregisterAll((Listener) this);
    }



    // ---- default descriptors ----

    private final List<Listener> applicators = new ArrayList<Listener>();

    @EventHandler
    public void onDescriptorRegistration(final DescriptorRegistrationEvent registration) {
        registration.register(Main.REFERENCE_SERVER, Server.class, new Server.Factory());
        registration.register(Main.REFERENCE_WORLD, World.class, new World.Factory());
    }

    @EventHandler
    public void onAuthorityInitialize(final AuthorityInitializeEvent intialization) {
        final Listener serverApplicator = new Server.PermissionApplicator(intialization.getAuthority());
        this.getServer().getPluginManager().registerEvents(serverApplicator, this);
        this.applicators.add(serverApplicator);

        final Listener worldApplicator = new World.PermissionApplicator(intialization.getAuthority());
        this.getServer().getPluginManager().registerEvents(worldApplicator, this);
        this.applicators.add(worldApplicator);
    }

    @EventHandler
    public void onAuthorityRelease(final AuthorityReleaseEvent release) {
        for (final Listener listener : this.applicators) HandlerList.unregisterAll(listener);
        this.applicators.clear();
    }

}
