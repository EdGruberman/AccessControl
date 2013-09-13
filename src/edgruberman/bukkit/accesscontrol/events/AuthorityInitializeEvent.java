package edgruberman.bukkit.accesscontrol.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import edgruberman.bukkit.accesscontrol.Authority;

/** custom descriptor plugins can register permission applicators in this event */
public class AuthorityInitializeEvent extends Event {

    private final Authority authority;

    public AuthorityInitializeEvent(final Authority authority) {
        this.authority = authority;
    }

    public Authority getAuthority() {
        return this.authority;
    }



    // ---- event handlers ----

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return AuthorityInitializeEvent.handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return AuthorityInitializeEvent.handlers;
    }

}
