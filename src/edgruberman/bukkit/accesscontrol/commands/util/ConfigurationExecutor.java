package edgruberman.bukkit.accesscontrol.commands.util;

import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public abstract class ConfigurationExecutor extends Executor {

    protected ConfigurationCourier courier;

    protected ConfigurationExecutor(final ConfigurationCourier courier) {
        this.courier = courier;
    }

    protected <P extends Parameter<T>, T> Parameter.Factory<P, T, ?> setSyntax(final Parameter.Factory<P, T, ?> factory) {
        // concatenate values for limited
        if (factory instanceof LimitedParameter.Factory) {
            final LimitedParameter.Factory<?, ?, ?> limited = (LimitedParameter.Factory<?, ?, ?>) factory;
            final String acceptable = JoinList.join(limited.acceptable, this.courier.getBase(), "argument-syntax-limited-");
            factory.setSyntax(acceptable);

        // use name for all others
        } else {
            final String name = this.courier.format("argument-syntax-name", factory.name);
            factory.setSyntax(name);
        }

        // reformat for requirement
        final String key = ( factory.required ? "argument-syntax-required" : "argument-syntax-optional" );
        final String requirement = this.courier.format(key, factory.syntax);
        factory.setSyntax(requirement);

        return factory;
    }

    @Override
    protected <P extends Parameter<T>, T> P addParameter(final Parameter.Factory<P, T, ?> factory) {
        return super.addParameter(this.setSyntax(factory));
    }

    @Override
    protected boolean execute(final ExecutionRequest request) throws CancellationContingency {
        try {
            return this.executeImplementation(request);

        } catch (final MissingArgumentContingency m) {
            this.courier.send(request.getSender(), "argument-missing"
                    , new Object[] {
                            m.getParameter().getName()
                            , m.getParameter().getSyntax()
                    });
            return false;

        } catch (final UnknownArgumentContingency u) {
            this.courier.send(request.getSender(), "argument-unknown"
                    , new Object[] {
                            u.getParameter().getName()
                            , u.getParameter().getSyntax()
                            , JoinList.join(u.getRequest().getArguments(u.getParameter()))
                    });
            return false;
        }
    };

    /** @return true if command processing completed successfully */
    protected abstract boolean executeImplementation(final ExecutionRequest request) throws CancellationContingency;

}
