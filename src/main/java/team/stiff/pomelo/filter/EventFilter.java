package team.stiff.pomelo.filter;

import team.stiff.pomelo.handler.EventHandler;

/**
 * An event filter tests whether or not certain conditions
 * are met whatever situation the filter will be used in
 * before allowing the event handler to be invoked.
 *
 * @author Daniel
 * @since May 31, 2017
 */
public interface EventFilter<E> {

    /**
     * Tests the given predicate when the handling
     * event is dispatched.
     *
     * @param eventHandler event handler instance
     * @param event        instance of event being dispatched
     * @return true if listener passes all filters, false otherwise
     */
    boolean test(EventHandler eventHandler, E event);
}
