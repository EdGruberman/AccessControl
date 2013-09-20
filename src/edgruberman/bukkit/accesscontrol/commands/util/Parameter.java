package edgruberman.bukkit.accesscontrol.commands.util;

public abstract class Parameter<T> {

    protected final String name;
    protected final String syntax;
    protected final int index;
    protected final boolean required;
    protected final T defaultValue;

    protected Parameter(final Parameter.Factory<?, T, ?> factory) {
        this.name = factory.name;
        this.syntax = factory.syntax;
        this.index = factory.index;
        this.required = factory.required;
        this.defaultValue = factory.defaultValue;
    }

    public String getName() {
        return this.name;
    }

    public String getSyntax() {
        return this.syntax;
    }

    public int getIndex() {
        return this.index;
    }

    public boolean isRequired() {
        return this.required;
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * @throws ArgumentContingency when the value can not be parsed
     * @return null when default value should be used
     */
    protected abstract T parse(ExecutionRequest request) throws ArgumentContingency;





    /** F must be the implementation itself and not a super */
    public static abstract class Factory<P extends Parameter<Y>, Y, F extends Parameter.Factory<P, Y, F>> {

        protected String name;
        protected String syntax;
        protected int index;
        protected boolean required;
        protected Y defaultValue;

        public F setName(final String name) {
            this.name = name;
            return this.cast();
        }

        public F setSyntax(final String syntax) {
            this.syntax = syntax;
            return this.cast();
        }

        public F setIndex(final int index) {
            this.index = index;
            return this.cast();
        }

        public F setRequired(final boolean required) {
            this.required = required;
            return this.cast();
        }

        public F setDefaultValue(final Y defaultValue) {
            this.defaultValue = defaultValue;
            return this.cast();
        }

        public abstract F cast();

        public abstract P build();

    }

}
