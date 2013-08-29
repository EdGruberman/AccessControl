package edgruberman.bukkit.accesscontrol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

/** descriptor manager */
public final class Registrar {

    private final Plugin plugin;
    private final LinkedHashMap<String, Registration> registrations = new LinkedHashMap<String, Registration>();
    private final Comparator<Descriptor> sorter;

    Registrar(final Plugin plugin, final List<String> references) {
        this.plugin = plugin;

        for (final String reference : references) this.registrations.put(reference, null);
        plugin.getServer().getPluginManager().callEvent(new DescriptorRegistrationEvent(Main.getAuthority(), this));

        // remove unregistered references
        final Iterator<Map.Entry<String, Registration>> it = this.registrations.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<String, Registration> entry = it.next();
            if (entry.getValue() == null) it.remove();
        }

        this.sorter = this.loadSorter(references);
    }

    private Comparator<Descriptor> loadSorter(final List<String> references) {
        final List<Class<? extends Descriptor>> order = new ArrayList<Class<? extends Descriptor>>();

        for (final String reference : references) {
            final Registration registration = this.registrations.get(reference.toLowerCase());
            if (registration == null) {
                this.plugin.getLogger().log(Level.WARNING, "Invalid Descriptor reference in descriptors.order: {0}", reference);
                continue;
            }

            order.add(registration.getImplementation());
        }

        return new Sorter(order);
    }

    /** @return registrations by reference sorted by reference order specified in configuration */
    public LinkedHashMap<String, Registration> getRegistrations() {
        return this.registrations;
    }

    /** @return registrations sorted by reference order specified in configuration */
    public List<Registration> registrations() {
        final List<Registration> result = new ArrayList<Registration>();
        for (final Map.Entry<String, Registration> entry : this.registrations.entrySet()) {
            result.add(entry.getValue()); // iterate by entries to ensure order respected
        }
        return result;
    }

    Comparator<Descriptor> getSorter() {
        return this.sorter;
    }

    // put registration in order specified in configuration, or at end of map
    void register(final String reference, final Class<? extends Descriptor> implementation, final Descriptor.Factory factory) {
        if (this.registrations.get(reference) != null) throw new IllegalArgumentException("reference must be unique; conflicting reference: " + reference);
        this.registrations.put(reference, new Registration(reference, implementation, factory));
    }



    /** descriptor registration */
    public static final class Registration {

        private final String reference;
        private final Class<? extends Descriptor> implementation;
        private final Descriptor.Factory factory;

        Registration(final String reference, final Class<? extends Descriptor> implementation, final Descriptor.Factory factory) {
            this.reference = reference;
            this.implementation = implementation;
            this.factory = factory;
        }

        public String getReference() {
            return this.reference;
        }

        public Class<? extends Descriptor> getImplementation() {
            return this.implementation;
        }

        public Descriptor.Factory getFactory() {
            return this.factory;
        }

    }



    /** custom descriptor plugins can register their custom descriptors in this event */
    public static class DescriptorRegistrationEvent extends Event {

        private final Authority authority;
        private final Registrar registrar;

        private DescriptorRegistrationEvent(final Authority authority, final Registrar registrar) {
            this.authority = authority;
            this.registrar = registrar;
        }

        public Authority getAuthority() {
            return this.authority;
        }

        /** @param reference name used in configuration file for ordering and in command arguments */
        public void register(final String reference, final Class<? extends Descriptor> implementation, final Descriptor.Factory factory) {
            if (reference == null) throw new IllegalArgumentException("reference can not be null");
            if (implementation == null) throw new IllegalArgumentException("implementation can not be null");
            if (factory == null) throw new IllegalArgumentException("factory can not be null");

            this.registrar.register(reference.toLowerCase(Locale.ENGLISH), implementation, factory);
        }

        // ---- event handlers ----

        private static final HandlerList handlers = new HandlerList();

        public static HandlerList getHandlerList() {
            return DescriptorRegistrationEvent.handlers;
        }

        @Override
        public HandlerList getHandlers() {
            return DescriptorRegistrationEvent.handlers;
        }

    }



    /** order descriptors for a given principal */
    final static class Sorter implements Comparator<Descriptor> {

        private final List<Class<? extends Descriptor>> order;

        Sorter(final List<Class<? extends Descriptor>> order) {
            this.order = order;
        }

        @Override
        public int compare(final Descriptor o1, final Descriptor o2) {
            final Integer i1 = this.order.indexOf(o1.getClass());
            final Integer i2 = this.order.indexOf(o2.getClass());
            return i1.compareTo(i2);
        }

    }

}

