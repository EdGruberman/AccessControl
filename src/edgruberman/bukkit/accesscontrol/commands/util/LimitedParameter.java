package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.Collection;
import java.util.Collections;

/** @version 1.1.0 */
public abstract class LimitedParameter<T> extends Parameter<T> {

    protected final Collection<String> known;

    public LimitedParameter(final LimitedParameter.Factory<?, T, ?> factory) {
        super(factory);
        this.known = Collections.unmodifiableCollection(factory.known);
    }

    public Collection<String> getKnown() {
        return this.known;
    }

    @Override
    protected T parse(final ExecutionRequest request) throws ArgumentContingency {
        final String argument = request.getArgument(this.index);
        if (argument == null) return null;

        if (!this.known.contains(argument)) throw new UnknownArgumentContingency(request, this);
        return this.parseLimited(request);
    }

    protected abstract T parseLimited(final ExecutionRequest request) throws ArgumentContingency;





    public static abstract class Factory<P extends Parameter<Y>, Y, F extends LimitedParameter.Factory<P, Y, F>> extends Parameter.Factory<P, Y, F> {

        protected Collection<String> known;

        public F setKnown(final Collection<String> known) {
            this.known = known;
            return this.cast();
        }

    }

}

