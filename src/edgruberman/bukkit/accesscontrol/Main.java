package edgruberman.bukkit.accesscontrol;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import edgruberman.bukkit.accesscontrol.Registrar.DescriptorRegistrationEvent;
import edgruberman.bukkit.accesscontrol.commands.Check;
import edgruberman.bukkit.accesscontrol.commands.Default;
import edgruberman.bukkit.accesscontrol.commands.Deny;
import edgruberman.bukkit.accesscontrol.commands.Effective;
import edgruberman.bukkit.accesscontrol.commands.Grant;
import edgruberman.bukkit.accesscontrol.commands.Reload;
import edgruberman.bukkit.accesscontrol.commands.Revoke;
import edgruberman.bukkit.accesscontrol.commands.Trace;
import edgruberman.bukkit.accesscontrol.descriptors.Server;
import edgruberman.bukkit.accesscontrol.descriptors.World;
import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.accesscontrol.repositories.YamlRepository;
import edgruberman.bukkit.accesscontrol.util.CustomPlugin;

public final class Main extends CustomPlugin implements Listener {

    public static ConfigurationCourier courier;

    private static Authority authority = null;

    public static Authority getAuthority() {
        return Main.authority;
    }



    private final List<Listener> applicators = new ArrayList<Listener>();

    @Override
    public void onLoad() {
        this.putConfigMinimum("8.0.0a0");
        this.putConfigMinimum("language.yml", "8.0.0b50");
    }

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = ConfigurationCourier.Factory.create(this).setBase(this.loadConfig("language.yml")).setFormatCode("format-code").build();

        // descriptor registrar
        this.getServer().getPluginManager().registerEvents(this, this);
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
        this.getCommand("accesscontrol:check").setExecutor(new Check(this.getServer()));
        this.getCommand("accesscontrol:effective").setExecutor(new Effective(this.getServer()));
        this.getCommand("accesscontrol:trace").setExecutor(new Trace(Main.authority, registrar.registrations(), this.getServer()));
        this.getCommand("accesscontrol:default").setExecutor(new Default(this.getServer()));

        // permission commands
        this.getCommand("accesscontrol:deny").setExecutor(new Deny(Main.authority, registrar.registrations()));
        this.getCommand("accesscontrol:grant").setExecutor(new Grant(Main.authority, registrar.registrations()));
        this.getCommand("accesscontrol:revoke").setExecutor(new Revoke(Main.authority, registrar.registrations()));

        // general commands
        this.getCommand("accesscontrol:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        if (Main.authority != null) {
            HandlerList.unregisterAll(Main.authority);
            Main.authority.release();
            Main.authority = null;
        }

        HandlerList.unregisterAll((Listener) this);

        Main.courier = null;
    }

    @EventHandler
    public void onDescriptorRegistration(final DescriptorRegistrationEvent registration) {
        registration.register("server", Server.class, new Server.Factory());
        registration.register("world", World.class, new World.Factory());
    }

    @EventHandler
    public void onAuthorityInitialize(final Authority.InitializeEvent intialization) {
        final Listener serverApplicator = new Server.PermissionApplicator(intialization.getAuthority());
        this.getServer().getPluginManager().registerEvents(serverApplicator, this);
        this.applicators.add(serverApplicator);

        final Listener worldApplicator = new World.PermissionApplicator(intialization.getAuthority());
        this.getServer().getPluginManager().registerEvents(worldApplicator, this);
        this.applicators.add(worldApplicator);
    }

    @EventHandler
    public void onAuthorityRelease(final Authority.ReleaseEvent release) {
        for (final Listener listener : this.applicators) HandlerList.unregisterAll(listener);
        this.applicators.clear();
    }

}
