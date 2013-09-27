package edgruberman.bukkit.accesscontrol;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import edgruberman.bukkit.accesscontrol.commands.Reload;
import edgruberman.bukkit.accesscontrol.commands.Revoke;
import edgruberman.bukkit.accesscontrol.commands.Trace;
import edgruberman.bukkit.accesscontrol.configuration.ConfigurationDefinition;
import edgruberman.bukkit.accesscontrol.configuration.ConfigurationInstruction;
import edgruberman.bukkit.accesscontrol.configuration.HeaderInstruction;
import edgruberman.bukkit.accesscontrol.configuration.PutKeyInstruction;
import edgruberman.bukkit.accesscontrol.configuration.RemoveKeyInstruction;
import edgruberman.bukkit.accesscontrol.descriptors.Server;
import edgruberman.bukkit.accesscontrol.descriptors.World;
import edgruberman.bukkit.accesscontrol.events.AuthorityInitializeEvent;
import edgruberman.bukkit.accesscontrol.events.AuthorityReleaseEvent;
import edgruberman.bukkit.accesscontrol.events.DescriptorRegistrationEvent;
import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.accesscontrol.repositories.YamlRepository;
import edgruberman.bukkit.accesscontrol.util.StandardPlugin;
import edgruberman.bukkit.accesscontrol.versioning.StandardVersion;

public final class Main extends StandardPlugin implements Listener {

    public static final String REFERENCE_SERVER = "server";
    public static final String REFERENCE_WORLD = "world";

    private static Authority authority = null;

    public static Authority getAuthority() {
        return Main.authority;
    }



    @Override
    public void onLoad() {
        this.putDefinition(StandardPlugin.CONFIGURATION_FILE, this.defineConfig());
        this.putDefinition("language.yml", this.defineLanguage());
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



    // ---- configuration ----

    private ConfigurationDefinition defineConfig() {
        final ConfigurationDefinition result = new ConfigurationDefinition();

        final List<ConfigurationInstruction> v8_0_0b100 = result.createInstructions(StandardVersion.parse("8.0.0b100"));
        v8_0_0b100.add(HeaderInstruction.create(
                "\n"
                + "---- comments ----\n"
                + "permission-order: order of application; reverse order of precedence; last overrides first\n"
                + "\n"
                + "---- values ----"
        ));

        v8_0_0b100.add(PutKeyInstruction.create(StandardPlugin.DEFAULT_LOG_LEVEL.getName(), StandardPlugin.KEY_LOG_LEVEL));
        v8_0_0b100.add(PutKeyInstruction.create(Arrays.asList("=players"), "default-groups"));
        v8_0_0b100.add(PutKeyInstruction.create(Arrays.asList(Main.REFERENCE_SERVER, Main.REFERENCE_WORLD), "permission-order"));
        v8_0_0b100.add(PutKeyInstruction.create("server.user.{0}", "implicit-permission", "user"));
        v8_0_0b100.add(PutKeyInstruction.create("server.group.{0}", "implicit-permission", "group"));
        v8_0_0b100.add(PutKeyInstruction.create(5, "create-maximum"));

        v8_0_0b100.add(RemoveKeyInstruction.create("version"));

        return result;
    }

    private ConfigurationDefinition defineLanguage() {
        final ConfigurationDefinition result = new ConfigurationDefinition();

        final List<ConfigurationInstruction> v8_0_0b100 = result.createInstructions(StandardVersion.parse("8.0.0b100"));
        v8_0_0b100.add(HeaderInstruction.create(
                "\n"
                + "---- arguments ----\n"
                + "check: 0 = generated, 1 = permission, 2 = player, 3 = value(0=false|1=true), 4 = set(0=default|1=set), 5 = source\n"
                + "effective-permission: 0 = generated, 1 = permission, 2 = player, 3 = value(0=false|1=true), 4 = source\n"
                + "effective-summary: 0 = generated, 1 = current page, 2 = total pages\n"
                + "effective-none: 0 = generated, 1 = match\n"
                + "trace-context: 0 = generated, 1 = context, 2 = name, 3 = type(0=user|1=group)\n"
                + "trace-assignment: 0 = generated, 1 = permission, 2 = principal name, 3 = principal type(0=user|1=group), 4 = value(0=false|1=true)\n"
                + "  , 5 = direct(0=inherited|1=direct), 6 = inherited name, 7 = inherited type(-1=direct|0=user|1=group), 8 = context, 9 = relationship(0=permission|1=parent)\n"
                + "trace-relationship: 0 = generated, 1 = permission, 2 = principal name, 3 = principal type(0=user|1=group), 4 = parent name\n"
                + "  , 5 = parent value(0=false|1=true), 6 = child name, 7 = child value(0=false|1=true), 8 = relationship(0=permission|1=parent)\n"
                + "trace-default: 0 = generated, 1 = permission, 2 = name, 3 = type(0=user|1=group), 4 = value(0=false|1=true), 5 = default\n"
                + "default: 0 = generated, 1 = permission, 2 = player, 3 = value(0=false|1=true), 4 = default, 5 = registered(0=false|1=true)\n"
                + "deny-success: 0 = generated, 1 = permission, 2 = name, 3 = type(0=user|1=group), 4 = context, 5 = previous(0=default|1=granted)\n"
                + "deny-already: 0 = generated, 1 = permission, 2 = name, 3 = type(0=user|1=group), 4 = context\n"
                + "grant-success: 0 = generated, 1 = permission, 2 = name, 3 = type(0=user|1=group), 4 = context, 5 = previous(-1=denied|0=default)\n"
                + "grant-already: 0 = generated, 1 = permission, 2 = name, 3 = type(0=user|1=group), 4 = context\n"
                + "revoke-success: 0 = generated, 1 = permission, 2 = name, 3 = type(0=user|1=group), 4 = context, 5 = previous(-1=denied|1=granted)\n"
                + "revoke-already: 0 = generated, 1 = permission, 2 = name, 3 = type(0=user|1=group), 4 = context\n"
                + "reload: 0 = generated, 1 = plugin name\n"
                + "requires-player: 0 = generated, 1 = label\n"
                + "argument-missing: 0 = generated, 1 = name 2 = syntax\n"
                + "argument-unknown: 0 = generated, 1 = name, 2 = syntax, 3 = value\n"
                + "argument-syntax-name: 0 = name\n"
                + "argument-syntax-limited-item: 0 = value\n"
                + "argument-syntax-required: 0 = argument\n"
                + "argument-syntax-optional: 0 = argument\n"
                + "\n"
                + "---- patterns ----"
        ));

        v8_0_0b100.add(PutKeyInstruction.create("§", "format-code"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> {2} {4,choice,0#§ddefaults|1#§3sets} §f{1} {3,choice,0#§cfalse|1#§atrue} §8(§7{5}§8)", "check"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> {2} §3sets §f{1} {3,choice,0#§cfalse|1#§atrue} §8(§7{4}§8)", "effective-permission"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §7Page §f{1}§7 of {2}", "effective-summary"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §eNo permissions found§7 that match§8: §f{1}", "effective-none"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §8-- §7Trace Context for §f{2} §7{3,choice,0#user|1#group}§8: §f{1}", "trace-context"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> {9,choice,0#§f|1#§7}{1}§8: {4,choice,0#§cfalse|1#§atrue} §7is {5,choice,0#§6inherited|1#§3directly applied}{5,choice,0# §7from §f{6} |1#}§7{7,choice,-1#|0#user|1#group} in §f{8}", "trace-assignment"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §8^^ §7{4}§8: {5,choice,0#§cfalse|1#§atrue} §7sets {8,choice,0#§f|1#§7}{6}§8: {7,choice,0#§cfalse|1#§atrue}", "trace-relationship"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §f{1} §is not assigned §8(§7Default§8: §d{5} §8= {4,choice,0#§cfalse|1#§atrue}§8)", "trace-default"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §7Default of §f{1} §7is §d{4} §8(§f{2} §8= {3,choice,0#§cfalse|1#§atrue}§8, §7{5,choice,0#Unregistered|1#Registered}§8)", "default"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §2Denied §f{2} §7{3,choice,0#user|1#group} §f{1} §7in §f{4} §8(§7Previously {5,choice,0#default|1#granted}§8)", "deny-success"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> {2} §7{3,choice,0#user|1#group} §ealready denied §f{1} §7in §f{4}", "deny-already"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §2Granted §f{2} §7{3,choice,0#user|1#group} §f{1} §7in §f{4} §8(§7Previously {5,choice,-1#denied|0#default}§8)", "grant-success"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> {2} §7{3,choice,0#user|1#group} §ealready granted §f{1} §7in §f{4}", "grant-already"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §2Revoked §7{5,choice,-1#deny|1#grant} on §f{2} §7{3,choice,0#user|1#group} §7for §f{1} §7in §f{4}", "revoke-success"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> {2} §7{3,choice,0#user|1#group} §ealready defaults §f{1} §7in §f{4}", "revoke-already"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §2Reloaded §7{1} plugin", "reload"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §cOnly in-game players§7 can use the §b/{1} §7command", "requires-player"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §cMissing §7required argument§8: {2}", "argument-missing"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §cUnknown§7 argument for {2}§8: §f{3}", "argument-unknown"));
        v8_0_0b100.add(PutKeyInstruction.create("§3§o{0}", "argument-syntax-name"));
        v8_0_0b100.add(PutKeyInstruction.create("§b{0}", "argument-syntax-limited-item"));
        v8_0_0b100.add(PutKeyInstruction.create("§3|", "argument-syntax-limited-delimiter"));
        v8_0_0b100.add(PutKeyInstruction.create("{0}", "argument-syntax-required"));
        v8_0_0b100.add(PutKeyInstruction.create("§3[{0}§3]", "argument-syntax-optional"));

        v8_0_0b100.add(RemoveKeyInstruction.create("version"));

        return result;
    }

}
