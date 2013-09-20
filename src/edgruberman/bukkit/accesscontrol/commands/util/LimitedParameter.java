package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.Collection;

public abstract class LimitedParameter<T> extends Parameter<T> {

    public final Collection<String> acceptable;

    public LimitedParameter(final LimitedParameter.Factory<?, T, ?> factory) {
        super(factory);
        this.acceptable = factory.acceptable;
    }

    @Override
    protected T parse(final ExecutionRequest request) throws ArgumentContingency {
        final String argument = request.getArgument(this.index);
        if (argument == null) return null;

        if (!this.acceptable.contains(argument)) throw new UnknownArgumentContingency(request, this);
        return this.parseLimited(request);
    }

    protected abstract T parseLimited(final ExecutionRequest request) throws ArgumentContingency;





    public static abstract class Factory<P extends Parameter<Y>, Y, F extends LimitedParameter.Factory<P, Y, F>> extends Parameter.Factory<P, Y, F> {

        protected Collection<String> acceptable;

        public F setAcceptable(final Collection<String> acceptable) {
            this.acceptable = acceptable;
            return this.cast();
        }

    }

}

