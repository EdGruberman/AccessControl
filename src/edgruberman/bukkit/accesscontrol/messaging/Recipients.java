package edgruberman.bukkit.accesscontrol.messaging;

import edgruberman.bukkit.accesscontrol.messaging.messages.Confirmation;

public interface Recipients {

    public abstract Confirmation send(Message message);

}
