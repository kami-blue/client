package team.stiff.pomelo;

import team.stiff.pomelo.handler.EventHandler;

/**
 * Pomelo is a simplistic event-bus that supports event filtering.
 * <p>
 * todo; thread-safety
 * todo; junit testing
 *
 * @author Daniel
 * @since May 31, 2017
 */
public interface EventManager {

    /**
     * Notify all registered {@link EventHandler}s that are listening
     * for the passed event that the event has been dispatched.
     *
     * @param event event instance
     * @param <E>   event type
     * @return passed event instance
     */
    <E> E dispatchEvent(E event);

    /**
     * Checks if the given listener object is registered.
     *
     * @param listener listener instance
     * @return true if registered; false otherwise
     */
    boolean isRegisteredListener(Object listener);

    /**
     * Register an object as an event listener that listens for the provided
     * eventClass type to be dispatched.
     *
     * @param listener event listener instance
     * @return true if successfully added; false otherwise
     */
    boolean addEventListener(Object listener);

    /**
     * Remove an event listener from the bus so it does not listen
     * for event dispatches anymore.
     *
     * @param listener event listener instance
     * @return true if successfully removed; false otherwise
     */
    boolean removeEventListener(Object listener);
}
