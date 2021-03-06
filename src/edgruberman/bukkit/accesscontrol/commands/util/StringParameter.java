package edgruberman.bukkit.accesscontrol.commands.util;

public class StringParameter extends Parameter<String> {

    public StringParameter(final StringParameter.Factory factory) {
        super(factory);
    }

    @Override
    protected String parse(final ExecutionRequest request) throws ArgumentContingency {
        return request.getArgument(this.index);
    }





    public static class Factory extends Parameter.Factory<StringParameter, String, StringParameter.Factory> {

        public static StringParameter.Factory create(final String name) {
            return new StringParameter.Factory().setName(name);
        }

        @Override
        public StringParameter.Factory cast() {
            return this;
        }

        @Override
        public StringParameter build() {
            return new StringParameter(this);
        }

    }

}
