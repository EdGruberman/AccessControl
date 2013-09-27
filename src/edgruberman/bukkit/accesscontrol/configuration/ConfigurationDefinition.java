package edgruberman.bukkit.accesscontrol.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.configuration.file.FileConfiguration;

import edgruberman.bukkit.accesscontrol.versioning.Version;

/** list of instructions to apply based on version */
public final class ConfigurationDefinition {

    private final TreeMap<Version, List<ConfigurationInstruction>> instructions = new TreeMap<Version, List<ConfigurationInstruction>>();

    public Version getLatest() {
        return this.instructions.lastKey();
    }

    public List<ConfigurationInstruction> createInstructions(final Version introduced) {
        final List<ConfigurationInstruction> result = new ArrayList<ConfigurationInstruction>();
        this.instructions.put(introduced, result);
        return result;
    }

    /**
     * @return instructions that should be applied to a configuration file
     * with specified version to bring it up to date
     */
    public List<ConfigurationInstruction> after(final Version version) {
        final List<ConfigurationInstruction> result = new ArrayList<ConfigurationInstruction>();

        final Set<Entry<Version, List<ConfigurationInstruction>>> applicable;
        applicable = ( version != null ? this.instructions.tailMap(version, false).entrySet() : this.instructions.entrySet() );

        for (final Map.Entry<Version, List<ConfigurationInstruction>> entry : applicable) {
            result.addAll(entry.getValue());
        }

        return result;
    }

    /**
     * @param existing version of base
     * @returns true when data would be lost if applied to base
     */
    public boolean conflicts(final FileConfiguration base, final Version existing) {
        for (final ConfigurationInstruction instruction : this.after(existing)) {
            if (instruction.conflicts(base)) return true;
        }

        return false;
    }

    /**
     * applies all applicable instructions introduced after existing version
     * @param existing version of base before application
     */
    public void apply(final FileConfiguration base, final Version existing) {
        for (final ConfigurationInstruction instruction : this.after(existing)) {
            instruction.apply(base);
        }
    }

}
