package edgruberman.bukkit.accesscontrol.events;

import java.util.Locale;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import edgruberman.bukkit.accesscontrol.Authority;
import edgruberman.bukkit.accesscontrol.Descriptor;
import edgruberman.bukkit.accesscontrol.Registrar;

/** custom descriptor plugins can register their custom descriptors in this event */
public class DescriptorRegistrationEvent extends Event {

    private final Authority authority;
    private final Registrar registrar;

    public DescriptorRegistrationEvent(final Authority authority, final Registrar registrar) {
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
