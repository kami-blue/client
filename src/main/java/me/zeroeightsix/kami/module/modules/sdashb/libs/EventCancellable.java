package me.zeroeightsix.kami.module.modules.sdashb.libs;

/**
 * Author Seth
 * 4/6/2019 @ 1:27 AM.
 * https://github.com/seppukudevelopment/seppuku
 */

public class EventCancellable extends EventStageable {

    private boolean canceled;

    public EventCancellable() {
    }

    public EventCancellable(EventStage stage) {
        super(stage);
    }

    public EventCancellable(EventStage stage, boolean canceled) {
        super(stage);
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
