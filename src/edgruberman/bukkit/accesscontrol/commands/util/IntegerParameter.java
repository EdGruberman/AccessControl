package edgruberman.bukkit.accesscontrol.commands.util;

public class IntegerParameter extends Parameter<Integer> {

    public IntegerParameter(final IntegerParameter.Factory factory) {
        super(factory);
    }

    @Override
    public Integer parse(final ExecutionRequest request) throws ArgumentContingency {
        final String argument = request.getArgument(this.index);
        if (argument == null) return this.defaultValue;

        try {
            return Integer.parseInt(argument);

        } catch (final NumberFormatException e) {
            throw new UnknownArgumentContingency(request, this);
        }
    }





    public static class Factory extends Parameter.Factory<IntegerParameter, Integer, IntegerParameter.Factory> {

        public static IntegerParameter.Factory create(final String name) {
            return new IntegerParameter.Factory().setName(name);
        }

        @Override
        public IntegerParameter.Factory cast() {
            return this;
        }

        @Override
        public IntegerParameter build() {
            return new IntegerParameter(this);
        }

    }

}
