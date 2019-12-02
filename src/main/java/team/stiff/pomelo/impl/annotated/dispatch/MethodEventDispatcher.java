package team.stiff.pomelo.impl.annotated.dispatch;

import team.stiff.pomelo.dispatch.EventDispatcher;
import team.stiff.pomelo.handler.EventHandler;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of the {@link EventDispatcher} designed
 * to invoke the listeners via reflection as the handler is a
 * {@link Method} object.
 *
 * @author Daniel
 * @since May 31, 2017
 */
public final class MethodEventDispatcher implements EventDispatcher {
    /**
     * The scanning strategy chosen by the event-bus implementation.
     */
    private final Map<Class<?>, Set<EventHandler>> eventHandlers;

    public MethodEventDispatcher(final Map<Class<?>, Set<EventHandler>> eventHandlers) {
        this.eventHandlers = eventHandlers;
    }

    @Override
    public <E> void dispatch(final E event) {
        // iterate all registered event handlers and pass the event onto them
        for (final EventHandler eventHandler : eventHandlers.getOrDefault(
                event.getClass(), Collections.emptySet()))
            eventHandler.handle(event);
    }
}
