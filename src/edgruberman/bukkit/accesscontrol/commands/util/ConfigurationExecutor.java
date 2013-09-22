package edgruberman.bukkit.accesscontrol.commands.util;

import org.bukkit.configuration.ConfigurationSection;

import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

/**
 * supplies {@link ConfigurationCourier} reference,
 * manages display of standard exceptions
 * and formats parameter syntax according to configuration
 * @version 1.1.0
 */
public abstract class ConfigurationExecutor extends Executor {

    protected ConfigurationCourier courier;

    protected ConfigurationExecutor(final ConfigurationCourier courier) {
        this.courier = courier;
    }

    protected void setSyntax(final Parameter.Factory<?, ?, ?> factory) {
        // concatenate known values for limited
        if (factory instanceof LimitedParameter.Factory) {
            final LimitedParameter.Factory<?, ?, ?> limited = (LimitedParameter.Factory<?, ?, ?>) factory;
            final String known = this.joinFactory().elements(limited.known).prefix("argument-syntax-known-").config(this.courier.getBase()).join();
            factory.setSyntax(known);

        // use name for all others
        } else {
            final String name = this.courier.format("argument-syntax-name", factory.name);
            factory.setSyntax(name);
        }

        // reformat for requirement
        final String key = ( factory.required ? "argument-syntax-required" : "argument-syntax-optional" );
        final String requirement = this.courier.format(key, factory.syntax);
        factory.setSyntax(requirement);
    }

    @Override
    protected <P extends Parameter<T>, T> P addParameter(final Parameter.Factory<P, T, ?> factory) {
        this.setSyntax(factory);
        return super.addParameter(factory);
    }

    @Override
    protected boolean validate(final ExecutionRequest request) throws CancellationContingency {
        try {
            return super.validate(request);

        } catch (final SenderRejectedContingency r) {
            final JoinList<String> valid = this.<String>joinFactory().prefix("sender-rejected-valid-").config(this.courier.getBase()).build();
            for (final Class<?> sender : r.getValid()) valid.add(sender.getSimpleName());
            this.courier.send(request.getSender(), "sender-rejected", request.getSender().getClass().getSimpleName(), valid, request.getLabel());
            return false;
        }
    }

    @Override
    protected boolean execute(final ExecutionRequest request) throws CancellationContingency {
        try {
            return this.executeImplementation(request);

        } catch (final MissingArgumentContingency m) {
            this.courier.send(request.getSender(), "argument-missing", m.getParameter().getName(), m.getParameter().getSyntax());
            return false;

        } catch (final UnknownArgumentContingency u) {
            this.courier.send(request.getSender(), "argument-unknown", u.getParameter().getName(), u.getParameter().getSyntax(), u.getArgument());
            return false;
        }
    };

    /** @return true if command processing completed successfully */
    protected abstract boolean executeImplementation(final ExecutionRequest request) throws CancellationContingency;

    protected <Y> ConfigurationJoinListFactory<Y> joinFactory() {
        return new ConfigurationJoinListFactory<Y>();
    }





    protected class ConfigurationJoinListFactory<T> extends JoinList.Factory<T, ConfigurationJoinListFactory<T>> {

        public static final String DEFAULT_PREFIX = "";

        public static final String CONFIG_KEY_FORMAT = "format";
        public static final String CONFIG_KEY_ITEM = "item";
        public static final String CONFIG_KEY_DELIMITER = "delimiter";

        protected String prefix = ConfigurationJoinListFactory.DEFAULT_PREFIX;
        protected ConfigurationSection config = ConfigurationExecutor.this.courier.getBase();

        public ConfigurationJoinListFactory<T> prefix(final String prefix) {
            this.prefix = prefix;
            return this.cast();
        }

        public ConfigurationJoinListFactory<T> config(final ConfigurationSection config) {
            this.config = config;
            return this.cast();
        }

        @Override
        public ConfigurationJoinListFactory<T> cast() {
            return this;
        }

        @Override
        public JoinList<T> build() {
            if (this.config != null) {
                this.format = this.config.getString(this.prefix + ConfigurationJoinListFactory.CONFIG_KEY_FORMAT, this.format);
                this.item = this.config.getString(this.prefix + ConfigurationJoinListFactory.CONFIG_KEY_ITEM, this.item);
                this.delimiter = this.config.getString(this.prefix + ConfigurationJoinListFactory.CONFIG_KEY_DELIMITER, this.delimiter);
            }

            return super.build();
        }

    }

}
