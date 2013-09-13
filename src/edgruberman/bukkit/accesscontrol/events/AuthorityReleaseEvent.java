package edgruberman.bukkit.accesscontrol.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import edgruberman.bukkit.accesscontrol.Authority;

/**
 * custom descriptor plugins should clean-up permission applicators in this event
 * and references to Principals should be released as they will no longer be valid
 */
public class AuthorityReleaseEvent extends Event {

    private final Authority authority;

    public AuthorityReleaseEvent(final Authority authority) {
        this.authority = authority;
    }

    public Authority getAuthority() {
        return this.authority;
    }



    // ---- event handlers ----

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return AuthorityReleaseEvent.handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return AuthorityReleaseEvent.handlers;
    }

}