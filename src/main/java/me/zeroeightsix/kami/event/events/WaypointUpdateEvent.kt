package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;

/**
 * @author dominikaaaa
 * @since 31/07/20 15:43
 */
public class WaypointUpdateEvent extends KamiEvent {
    private final UpdateType updateType;

    public WaypointUpdateEvent(UpdateType updateType) {
        super();
        this.updateType = updateType;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public enum UpdateType {
        CREATE, REMOVE
    }
}
