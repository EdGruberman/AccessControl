package edgruberman.bukkit.accesscontrol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edgruberman.bukkit.accesscontrol.configuration.ConfigurationDefinition;
import edgruberman.bukkit.accesscontrol.configuration.ConfigurationInstruction;
import edgruberman.bukkit.accesscontrol.configuration.HeaderInstruction;
import edgruberman.bukkit.accesscontrol.configuration.PutKeyInstruction;
import edgruberman.bukkit.accesscontrol.configuration.RemoveKeyInstruction;
import edgruberman.bukkit.accesscontrol.configuration.RenameKeyInstruction;
import edgruberman.bukkit.accesscontrol.util.StandardPlugin;
import edgruberman.bukkit.accesscontrol.versioning.StandardVersion;

/** organizes configuration file definitions into a separate class container */
public final class Configuration {

    private static Map<String, ConfigurationDefinition> definitions = new HashMap<String, ConfigurationDefinition>();

    static {
        Configuration.definitions.put(StandardPlugin.CONFIGURATION_FILE, Configuration.defineConfig());
        Configuration.definitions.put("language.yml", Configuration.defineLanguage());
    }

    public static ConfigurationDefinition getDefinition(final String name) {
        return Configuration.definitions.get(name);
    }

    private static ConfigurationDefinition defineConfig() {
        final ConfigurationDefinition result = new ConfigurationDefinition();

        final List<ConfigurationInstruction> v8_0_0b100 = result.createInstructions(StandardVersion.parse("8.0.0b100"));
        final List<ConfigurationInstruction> v8_0_0b112 = result.createInstructions(StandardVersion.parse("8.0.0b112"));

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
        v8_0_0b112.add(RenameKeyInstruction.create(5, "create-maximum", "create-limit-default"));

        v8_0_0b100.add(RemoveKeyInstruction.create("version"));

        return result;
    }

    private static ConfigurationDefinition defineLanguage() {
        final ConfigurationDefinition result = new ConfigurationDefinition();

        final List<ConfigurationInstruction> v8_0_0b100 = result.createInstructions(StandardVersion.parse("8.0.0b100"));
        final List<ConfigurationInstruction> v8_0_0b110 = result.createInstructions(StandardVersion.parse("8.0.0b110"));
        final List<ConfigurationInstruction> v8_0_0b111 = result.createInstructions(StandardVersion.parse("8.0.0b111"));
        final List<ConfigurationInstruction> v8_0_0b112 = result.createInstructions(StandardVersion.parse("8.0.0b112"));

        v8_0_0b112.add(HeaderInstruction.create(
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
                + "limit-current: 0 = generated, 1 = name, 2 = type(0=user|1=group), 3 = current, 4 = default\n"
                + "limit-modified: 0 = generated, 1 = name, 2 = type(0=user|1=group), 3 = previous, 4 = default, 5 = current\n"
                + "reload: 0 = generated, 1 = plugin name\n"
                + "sender-rejected: 0 = generated, 1 = acceptable, 2 = label\n"
                + "sender-rejected-valid-item: 0 = name\n"
                + "requires-player: 0 = generated, 1 = label\n"
                + "argument-missing: 0 = generated, 1 = name 2 = syntax\n"
                + "argument-unknown: 0 = generated, 1 = name, 2 = syntax, 3 = value\n"
                + "argument-syntax-name: 0 = name\n"
                + "argument-syntax-known-item: 0 = value\n"
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
        v8_0_0b110.add(PutKeyInstruction.create("§f-> §f{1} §7is not assigned §8(§7Default§8: §d{5} §8= {4,choice,0#§cfalse|1#§atrue}§8)", "trace-default"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §7Default of §f{1} §7is §d{4} §8(§f{2} §8= {3,choice,0#§cfalse|1#§atrue}§8, §7{5,choice,0#Unregistered|1#Registered}§8)", "default"));

        v8_0_0b100.add(PutKeyInstruction.create("§f-> §2Denied §f{2} §7{3,choice,0#user|1#group} §f{1} §7in §f{4} §8(§7Previously {5,choice,0#default|1#granted}§8)", "deny-success"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> {2} §7{3,choice,0#user|1#group} §ealready denied §f{1} §7in §f{4}", "deny-already"));

        v8_0_0b100.add(PutKeyInstruction.create("§f-> §2Granted §f{2} §7{3,choice,0#user|1#group} §f{1} §7in §f{4} §8(§7Previously {5,choice,-1#denied|0#default}§8)", "grant-success"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> {2} §7{3,choice,0#user|1#group} §ealready granted §f{1} §7in §f{4}", "grant-already"));

        v8_0_0b100.add(PutKeyInstruction.create("§f-> §2Revoked §7{5,choice,-1#deny|1#grant} on §f{2} §7{3,choice,0#user|1#group} §7for §f{1} §7in §f{4}", "revoke-success"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> {2} §7{3,choice,0#user|1#group} §ealready defaults §f{1} §7in §f{4}", "revoke-already"));

        v8_0_0b112.add(PutKeyInstruction.create("§f-> §7Group limit for §f{1} §7{2,choice,0#user|1#group} is §f{3}", "limit-current"));
        v8_0_0b112.add(PutKeyInstruction.create("§f-> §2Changed §7group limit for §f{1} §7{2,choice,0#user|1#group} from §f{3} §7to §f{5}", "limit-modified"));

        v8_0_0b100.add(PutKeyInstruction.create("§f-> §2Reloaded §7{1} plugin", "reload"));

        v8_0_0b111.add(PutKeyInstruction.create("§f-> §cOnly {1} §7can use the §b/{2} §7command", "sender-rejected"));
        v8_0_0b111.add(PutKeyInstruction.create("§c{0}s", "sender-rejected-valid-item"));
        v8_0_0b111.add(PutKeyInstruction.create("§4,", "sender-rejected-valid-delimiter"));

        v8_0_0b100.add(PutKeyInstruction.create("§f-> §cMissing §7required argument§8: {2}", "argument-missing"));
        v8_0_0b100.add(PutKeyInstruction.create("§f-> §cUnknown§7 argument for {2}§8: §f{3}", "argument-unknown"));
        v8_0_0b100.add(PutKeyInstruction.create("§3§o{0}", "argument-syntax-name"));
        v8_0_0b100.add(PutKeyInstruction.create("§b{0}", "argument-syntax-known-item"));
        v8_0_0b100.add(PutKeyInstruction.create("§3|", "argument-syntax-known-delimiter"));
        v8_0_0b100.add(PutKeyInstruction.create("{0}", "argument-syntax-required"));
        v8_0_0b100.add(PutKeyInstruction.create("§3[{0}§3]", "argument-syntax-optional"));

        v8_0_0b100.add(RemoveKeyInstruction.create("version"));

        return result;
    }

}
