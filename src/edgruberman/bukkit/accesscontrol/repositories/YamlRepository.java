package edgruberman.bukkit.accesscontrol.repositories;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.accesscontrol.Descriptor;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.Registrar;
import edgruberman.bukkit.accesscontrol.Registrar.Registration;
import edgruberman.bukkit.accesscontrol.Repository;
import edgruberman.bukkit.accesscontrol.User;
import edgruberman.bukkit.accesscontrol.util.BufferedYamlConfiguration;

public final class YamlRepository implements Repository {

    private static final long MAX_SAVE_RATE = 3000; // milliseconds
    private static final Character SEPARATOR = '¯'; // can not be used in user or group name to avoid parsing errors

    private final BufferedYamlConfiguration users;
    private final BufferedYamlConfiguration groups;

    private final Map<String, Registration> references;
    private final Map<Class<? extends Descriptor>, Registration> descriptors = new HashMap<Class<? extends Descriptor>, Registration>();

    private final Comparator<Descriptor> sorter;

    private final PluginManager pm;
    private final String implicitUser;
    private final String implicitGroup;

    public YamlRepository(final Plugin plugin, final File users, final File groups
            , final Map<String, Registration> references, final Comparator<Descriptor> sorter
            , final PluginManager pm, final String implicitUser, final String implicitGroup) throws InvalidConfigurationException {

        this.users = this.loadFile(plugin, users, YamlRepository.MAX_SAVE_RATE);
        this.groups = this.loadFile(plugin, groups, YamlRepository.MAX_SAVE_RATE);

        this.references = references;
        for (final Registration registration : references.values()) this.descriptors.put(registration.getImplementation(), registration);

        this.sorter = sorter;

        this.pm = pm;
        this.implicitUser = implicitUser;
        this.implicitGroup = implicitGroup;
    }

    private BufferedYamlConfiguration loadFile(final Plugin plugin, final File file, final long rate) throws InvalidConfigurationException {
        final BufferedYamlConfiguration result = new BufferedYamlConfiguration(plugin, file, rate);
        result.options().pathSeparator(YamlRepository.SEPARATOR);

        try {
            result.load();
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to load source file: " + result.getFile(), e);
        }

        return result;
    }

    private Permission createImplicit(final String format, final String name) {
        final String formatted = MessageFormat.format(format, name);

        Permission result = this.pm.getPermission(formatted);
        if (result != null)  return result;

        result = new Permission(formatted, PermissionDefault.FALSE);
        this.pm.addPermission(result);

        return result;
    }

    @Override
    public User loadUser(final String name) {
        final ConfigurationSection section = this.users.getConfigurationSection(name);
        if (section == null) return null;

        return User.Factory.of(this.sorter)
            .setName(name)
            .setImplicit(this.createImplicit(this.implicitUser, name))
            .addPermissions(this.loadDescriptors(section))
            .build();
    }

    @Override
    public Group loadGroup(final String name) {
        final ConfigurationSection section = this.groups.getConfigurationSection(name);
        if (section == null) return null;

        return Group.Factory.of(this.sorter)
            .setName(name)
            .setImplicit(this.createImplicit(this.implicitGroup, name))
            .addPermissions(this.loadDescriptors(section))
            .setDescription(( section != null ? section.getString("description") : null ))
            .setCreator(( section != null ? section.getString("creator") : null ))
            .build();
    }

    private List<Descriptor> loadDescriptors(final ConfigurationSection principal) {
        final ConfigurationSection permissions = principal.getConfigurationSection("permissions");
        if (permissions == null) return Collections.emptyList();

        final List<Descriptor> result = new ArrayList<Descriptor>();
        for (final String reference : permissions.getKeys(false)) {
            final ConfigurationSection descriptor = permissions.getConfigurationSection(reference);

            final Registrar.Registration registration = this.references.get(reference);
            if (registration == null) continue;
            final Descriptor.Factory factory = registration.getFactory();

            // replace YAML specific objects with generic equivalents
            final Map<String, Object> values = descriptor.getValues(false);
            for (final Map.Entry<String, Object> entry : values.entrySet()) {
                if (entry.getValue() instanceof MemorySection) {
                    final MemorySection value = (MemorySection) entry.getValue();
                    entry.setValue(value.getValues(false));
                }
            }

            final Descriptor deserialized;
            try {
                deserialized = factory.deserialize(values);
            } catch (final Exception e) {
                throw new IllegalStateException("Unable to load Descriptor: " + registration.getImplementation().getName(), e);
            }
            result.add(deserialized);
        }

        return result;
    }

    @Override
    public void save(final Principal principal) {
        if (principal.getClass().equals(Group.class)) {
            this.saveGroup((Group) principal);
        } else {
            this.saveUser((User) principal);
        }
    }

    private void saveUser(final User user) {
        if (!user.isPersistent()) this.deleteUser(user);

        final Map<String, Object> serialized = new LinkedHashMap<String, Object>();
        serialized.put("memberships", this.serializeMemberships(user));
        serialized.put("permissions", this.serializeDescriptors(user));

        this.users.set(user.getName(), serialized);
        this.users.queueSave();
    }

    private void saveGroup(final Group group) {
        if (!group.isPersistent()) this.deleteGroup(group);

        final Map<String, Object> serialized = new LinkedHashMap<String, Object>();
        serialized.put("description", group.getDescription());
        serialized.put("creator", group.getCreator().getName());
        serialized.put("memberships", this.serializeMemberships(group));
        serialized.put("permissions", this.serializeDescriptors(group));

        this.groups.set(group.getName(), serialized);
        this.groups.queueSave();
    }

    private List<String> serializeMemberships(final Principal principal) {
        final List<String> result = new ArrayList<String>();
        for (final Group m : principal.getMemberships()) {
            result.add(m.getName());
        }
        return result;
    }

    private Map<String, Object> serializeDescriptors(final Principal principal) {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (final Descriptor descriptor : principal.getPermissions()) {
            result.put(this.descriptors.get(descriptor.getClass()).getReference(), descriptor.serialize());
        }
        return result;
    }

    @Override
    public void delete(final Principal principal) {
        if (principal.getClass().equals(Group.class)) {
            this.deleteGroup((Group) principal);
        } else {
            this.deleteUser((User) principal);
        }
    }

    private void deleteGroup(final Group group) {
        this.groups.set(group.getName(), null);
        this.groups.queueSave();
    }

    private void deleteUser(final User user) {
        this.users.set(user.getName(), null);
        this.users.queueSave();
    }

    @Override
    public void release() {
        if (this.users.isQueued()) this.users.save();
        if (this.groups.isQueued()) this.groups.save();
    }

    // TODO address circular references
    @Override
    public List<GroupReference> memberships(final Principal principal) {
        final BufferedYamlConfiguration config = ( principal.getClass().equals(Group.class) ? this.groups : this.users );

        final ConfigurationSection section = config.getConfigurationSection(principal.getName());
        if (section == null) return Collections.emptyList();

        final List<GroupReference> result = new ArrayList<GroupReference>();
        for (final String name : section.getStringList("memberships")) result.add(new GroupReference(name));
        return result;
    }

    @Override
    public List<PrincipalReference> members(final Group group) {
        final List<PrincipalReference> result = new ArrayList<PrincipalReference>();

        // users
        for (final String name : this.users.getKeys(false)) {
            final List<String> memberships = this.users.getConfigurationSection(name).getStringList("memberships");
            if (!memberships.contains(group.getName())) continue;
            result.add(new PrincipalReference(User.class, name));
        }

        // groups
        for (final String name : this.groups.getKeys(false)) {
            final List<String> memberships = this.groups.getConfigurationSection(name).getStringList("memberships");
            if (!memberships.contains(group.getName())) continue;
            result.add(new PrincipalReference(Group.class, name));
        }

        return result;
    }

}
