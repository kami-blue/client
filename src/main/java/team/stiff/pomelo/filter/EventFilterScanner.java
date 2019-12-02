package team.stiff.pomelo.filter;

import java.util.Set;

/**
 * Scans a listener for any filters associated with the listener.
 *
 * @author Daniel
 * @since Jun 13, 2017
 */
public interface EventFilterScanner<T> {

    /**
     * Finds all associated filters with the given listener
     * type.
     *
     * @param listener listener instance
     */
    Set<EventFilter> scan(T listener);
}
