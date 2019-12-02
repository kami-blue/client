package team.stiff.pomelo.handler.scan;

import team.stiff.pomelo.handler.EventHandler;

import java.util.Map;
import java.util.Set;

/**
 * Attempts to locate all event listeners in a given object and
 * stores them in a unmodifiable list. ({@see #getImmutableListeners})
 *
 * @author Daniel
 * @since May 31, 2017
 */
public interface EventHandlerScanner {

    /**
     * Check the given object for any possible listeners that are
     * contained inside.
     *
     * @return true if listeners located, false otherwise
     */
    Map<Class<?>, Set<EventHandler>> locate(Object listenerContainer);
}
