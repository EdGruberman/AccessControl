package edgruberman.bukkit.accesscontrol.commands.util;

import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public abstract class ConditionalParameter<T> extends Parameter<T> {

    protected Parameter<T> parameter;

    public ConditionalParameter(final Parameter.Factory<?, T, ?> factory, final Parameter<T> parameter) {
        super(factory);
        this.parameter = parameter;
    }

    public Parameter<T> getParameter() {
        return this.parameter;
    }

    @Override
    public T parse(final ExecutionRequest request) throws ArgumentParseException {
        return this.parameter.parse(request);
    }





    public static class RequiredParameter<T> extends ConditionalParameter<T> {

        public RequiredParameter(final Parameter.Factory<?, T, ?> factory, final Parameter<T> parameter, final ConfigurationCourier courier) {
            super(factory.setSyntax(courier.format("argument-required", parameter.getSyntax())), parameter);
        }

        @Override
        public T parse(final ExecutionRequest request) throws ArgumentParseException {
            if (request.getArgument(this.index) == null) throw new ArgumentMissingException(request, this);
            return super.parse(request);
        }

    }





    public static class OptionalParameter<T> extends ConditionalParameter<T> {

        public OptionalParameter(final Parameter.Factory<?, T, ?> factory, final Parameter<T> parameter, final ConfigurationCourier courier) {
            super(factory.setSyntax(courier.format("argument-optional", parameter.getSyntax())), parameter);
        }

    }

}
