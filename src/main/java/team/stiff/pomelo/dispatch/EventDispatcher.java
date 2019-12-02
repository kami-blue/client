package team.stiff.pomelo.dispatch;

/**
 * The dispatcher handles invocation of all registered listeners.
 *
 * @author Daniel
 * @since May 31, 2017
 */
public interface EventDispatcher {

    /**
     * Dispatch all listeners that are listening for the
     * given event.
     *
     * @param event event instance
     * @param <E>   event type
     */
    <E> void dispatch(E event);
}
