package edgruberman.bukkit.accesscontrol.commands;

import java.io.File;
import java.util.Locale;

import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Group;
import edgruberman.bukkit.accesscontrol.Principal;
import edgruberman.bukkit.accesscontrol.User;
import edgruberman.bukkit.accesscontrol.commands.util.ArgumentContingency;
import edgruberman.bukkit.accesscontrol.commands.util.CancellationContingency;
import edgruberman.bukkit.accesscontrol.commands.util.ConfigurationExecutor;
import edgruberman.bukkit.accesscontrol.commands.util.ExecutionRequest;
import edgruberman.bukkit.accesscontrol.commands.util.IntegerParameter;
import edgruberman.bukkit.accesscontrol.commands.util.OfflinePlayerParameter;
import edgruberman.bukkit.accesscontrol.commands.util.PrincipalClassParameter;
import edgruberman.bukkit.accesscontrol.commands.util.UnknownArgumentContingency;
import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.accesscontrol.util.BufferedYamlConfiguration;

public final class Limit extends ConfigurationExecutor {

    private static final long SAVE_RATE = 30000;

    private final Authority authority;
    private final BufferedYamlConfiguration repository;
    private final int defaultLimit;

    private final OfflinePlayerParameter name;
    private final PrincipalClassParameter type;
    private final IntegerParameter limit;

    public Limit(final ConfigurationCourier courier, final Server server, final Authority authority, final Plugin plugin, final File repository, final int defaultLimit) {
        super(courier);
        this.authority = authority;

        try {
            this.repository = new BufferedYamlConfiguration(plugin, repository, Limit.SAVE_RATE).load();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        this.defaultLimit = defaultLimit;

        this.name = this.addRequired(OfflinePlayerParameter.Factory.create("name", server));
        this.type = this.addOptional(PrincipalClassParameter.Factory.create("type"));
        this.limit = this.addOptional(IntegerParameter.Factory.create("limit"));
    }

    // usage: /<command> name [group|user] [limit]
    @Override
    protected boolean executeImplementation(final ExecutionRequest request) throws CancellationContingency {
        final Principal target = this.parsePrincipal(request);

        // show allocation
        final Integer current = this.getLimit(target);
        if (!request.isExplicit(this.limit)) {
            this.courier.send(request.getSender(), "limit-current", target.getName(), target.getClass().equals(User.class)?0:1, current, this.defaultLimit);
            return true;
        }

        // modify allocation
        final Integer requested = request.parse(this.limit);
        this.putLimit(target, ( requested >= 0 ? requested : null ));
        this.courier.send(request.getSender(), "limit-modified", target.getName(), target.getClass().equals(User.class)?0:1, current, this.defaultLimit, requested);
        return true;
    }

    protected Principal parsePrincipal(final ExecutionRequest request) throws ArgumentContingency {
        final String name = request.parse(this.name).getName().toLowerCase(Locale.ENGLISH);
        final Class<? extends Principal> type = request.parse(this.type);

        // group, must exist
        Principal result = null;
        if (!request.isExplicit(this.type) || type.equals(Group.class)) {
            result = this.authority.getGroup(name);
            if (result == null && type != null) throw new UnknownArgumentContingency(request, this.name);
        }

        // otherwise, assume user
        if (result == null) result = this.authority.createUser(name);

        return result;
    }

    public int getLimit(final Principal principal) {
        final String path = principal.getClass().getSimpleName().toLowerCase(Locale.ENGLISH);
        final ConfigurationSection sectionType = this.repository.getConfigurationSection(path);
        if (sectionType != null) {
            final String key = principal.getName().toLowerCase(Locale.ENGLISH);
            if (sectionType.isSet(key)) return sectionType.getInt(key);
        }

        final Integer result = this.inheritedLimit(principal);

        return ( result != null ? result : this.defaultLimit );
    }

    private Integer inheritedLimit(final Principal principal) {
        for (final Group inherited : principal.memberships()) {
            final Integer result = this.getLimit(inherited);
            if (result != null) return result;
        }

        return null;
    }

    private void putLimit(final Principal principal, final Integer limit) {
        final String path = principal.getClass().getSimpleName().toLowerCase(Locale.ENGLISH);

        if (!this.repository.isConfigurationSection(path)) {
            if (limit == null) return;
            this.repository.createSection(path);
        }
        final ConfigurationSection section = this.repository.getConfigurationSection(path);

        final String key = principal.getName().toLowerCase(Locale.ENGLISH);
        section.set(key, limit);

        // remove empty sections
        if (limit == null && section.getKeys(false).isEmpty()) this.repository.set(path, null);

        this.repository.queueSave();
    }

}
