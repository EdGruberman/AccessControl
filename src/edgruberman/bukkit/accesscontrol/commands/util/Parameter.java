package edgruberman.bukkit.accesscontrol.commands.util;

public abstract class Parameter<T> {

    protected final String name;
    protected final String syntax;
    protected final int begin;
    protected final int end;
    protected final boolean required;
    protected final T defaultValue;

    protected Parameter(final Parameter.Factory<?, T, ?> factory) {
        this.name = factory.name;
        this.syntax = factory.syntax;
        this.begin = factory.begin;
        this.end = factory.begin + factory.size;
        this.required = factory.required;
        this.defaultValue = factory.defaultValue;
    }

    public String getName() {
        return this.name;
    }

    public String getSyntax() {
        return this.syntax;
    }

    public int getBegin() {
        return this.begin;
    }

    public int getEnd() {
        return this.end;
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

        public static final int DEFAULT_SIZE = 1;

        protected String name;
        protected String syntax;
        protected int begin;
        protected int size = Parameter.Factory.DEFAULT_SIZE;
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

        public F setBegin(final int begin) {
            this.begin = begin;
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
