package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.Collection;

import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public abstract class LimitedParameter<T> extends Parameter<T> {

    public final Collection<String> acceptable;

    public LimitedParameter(final LimitedParameter.Factory<?, T, ?> factory) {
        super(factory);
        this.acceptable = factory.acceptable;
    }

    @Override
    public T parse(final ExecutionRequest request) throws ArgumentParseException {
        final String value = request.getArgument(this.index);
        if (value != null && !this.acceptable.contains(value)) throw new ArgumentUnknownException(request, this);
        return this.doParse(request);
    }

    protected abstract T doParse(final ExecutionRequest request) throws ArgumentParseException;





    public static abstract class Factory<P extends Parameter<Y>, Y, F extends LimitedParameter.Factory<P, Y, F>> extends Parameter.Factory<P, Y, F> {

        protected Collection<String> acceptable;

        protected Factory(final String name, final ConfigurationCourier courier, final Collection<String> acceptable) {
            super(name);
            this.syntax = JoinList.join(acceptable, courier.getBase(), "argument-limited-");
            this.acceptable = acceptable;
        }

        @SuppressWarnings("unchecked")
        public F setAcceptable(final Collection<String> acceptable) {
            this.acceptable = acceptable;
            return (F) this;
        }

    }

}

