package edgruberman.bukkit.accesscontrol.commands.util;

import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * store object references for lazy MessageFormat formatting
 * @author EdGruberman (ed@rjump.com)
 * @version 2.0.0
 */
public class JoinList<E> extends ArrayList<E> {

    private static final long serialVersionUID = 1L;



    // ---- defaults ----

    public static final String DEFAULT_FORMAT = "{0}";
    public static final String DEFAULT_ITEM = "{0}";
    public static final String DEFAULT_DELIMITER = " ";
    public static final String DEFAULT_LAST = JoinList.DEFAULT_DELIMITER;



    // ---- convenience functions ----

    public static <T> DefaultFactory<T> factory() {
        return JoinList.DefaultFactory.create();
    }

    public static String join(final Collection<?> elements) {
        return JoinList.factory().elements(elements).join();
    }

    public static <T> String join(final T... elements) {
        return JoinList.join(Arrays.asList(elements));
    }



    // ---- instance ----

    private final String format;
    private final String item;
    private final String delimiter;
    private final String last;

    public JoinList() {
        this(JoinList.DEFAULT_FORMAT, JoinList.DEFAULT_ITEM, JoinList.DEFAULT_DELIMITER, JoinList.DEFAULT_LAST);
    }

    public JoinList(final String format, final String item, final String delimiter, final String last) {
        this.format = format;
        this.item = item;
        this.delimiter = delimiter;
        this.last = last;
    }

    /** add arguments as single element array */
    public boolean add(final Object... arguments) {
        return this.add((Object) arguments);
    }

    public String join() {
        final StringBuilder items = new StringBuilder();

        final int last = this.size() - 1;
        for (int i = 0; i < this.size(); i++) {
            final E element = this.get(i);

            // append delimiter
            if (items.length() > 0) {
                if (i < last) {
                    items.append(this.delimiter);
                } else {
                    items.append(this.last);
                }
            }

            // prevent recursion
            if (element == this) {
                items.append("{this}");
                continue;
            }

            // format item, which could either be an array of objects or a single object
            final MessageFormat message = new MessageFormat(this.item);
            final Object[] arguments = ( element instanceof Object[] ? (Object[]) element : new Object[] { element } );
            final StringBuffer sb = message.format(arguments, new StringBuffer(), new FieldPosition(0));
            items.append(sb);
        }

        return MessageFormat.format(this.format, items);
    }

    @Override
    public String toString() {
        return this.join();
    }





    public abstract static class Factory<T, F extends JoinList.Factory<T, F>> {

        protected String format = JoinList.DEFAULT_FORMAT;
        protected String item = JoinList.DEFAULT_ITEM;
        protected String delimiter = JoinList.DEFAULT_DELIMITER;
        protected String last = JoinList.DEFAULT_LAST;

        protected Collection<? extends T> elements = Collections.emptyList();

        public F format(final String format) {
            this.format = format;
            return this.cast();
        }

        public F item(final String item) {
            this.item = item;
            return this.cast();
        }

        public F delimiter(final String delimiter) {
            this.delimiter = delimiter;
            return this.cast();
        }

        public F last(final String last) {
            this.last = last;
            return this.cast();
        }


        public F elements(final Collection<? extends T> elements) {
            this.elements = elements;
            return this.cast();
        }

        protected abstract F cast();

        public JoinList<T> build() {
            final JoinList<T> result = new JoinList<T>(this.format, this.item, this.delimiter, this.last);
            result.addAll(this.elements);
            return result;
        }

        public String join() {
            return this.build().join();
        }

    }





    public static class DefaultFactory<T> extends JoinList.Factory<T, DefaultFactory<T>> {

        public static <Y> DefaultFactory<Y> create() {
            return new DefaultFactory<Y>();
        }

        @Override
        protected DefaultFactory<T> cast() {
            return this;
        }

    }

}
