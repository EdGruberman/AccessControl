package edgruberman.bukkit.accesscontrol.commands.util;

import edgruberman.bukkit.accesscontrol.messaging.Courier.ConfigurationCourier;

public abstract class Parameter<T> {

    protected final String name;
    protected final String syntax;
    protected final int index;
    protected final T defaultValue;

    protected Parameter(final Parameter.Factory<?, T, ?> factory) {
        this.name = factory.name;
        this.syntax = factory.syntax;
        this.index = factory.index;
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

    public T getDefaultValue() {
        return this.defaultValue;
    }

    /** @throws ArgumentParseException when the value can not be parsed */
    public abstract T parse(final ExecutionRequest request) throws ArgumentParseException;





    public static abstract class Factory<P extends Parameter<Y>, Y, F extends Factory<P, Y, F>> {

        protected String name;
        protected String syntax;
        protected int index;

        protected Y defaultValue = null;

        protected Factory(final String name) {
            this.name = name;
        }

        protected Factory(final String name, final ConfigurationCourier courier) {
            this(name);
            this.syntax = courier.format("argument-name", name);
        }

        @SuppressWarnings("unchecked")
        public F setName(final String name) {
            this.name = name;
            return (F) this;
        }

        @SuppressWarnings("unchecked")
        public F setSyntax(final String syntax) {
            this.syntax = syntax;
            return (F) this;
        }

        @SuppressWarnings("unchecked")
        public F setIndex(final int index) {
            this.index = index;
            return (F) this;
        }

        @SuppressWarnings("unchecked")
        public F setDefaultValue(final Y defaultValue) {
            this.defaultValue = defaultValue;
            return (F) this;
        }

        public abstract P build();

    }

}
