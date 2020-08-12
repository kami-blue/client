// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EventManager2<T>
{
    private final Map<Class<?>, List<EventData<T>>> listeners;
    
    public EventManager2() {
        this.listeners = new HashMap<Class<?>, List<EventData<T>>>();
    }
    
    public void addListener(final T listener) {
        for (final Method method : listener.getClass().getMethods()) {
            if (method.isAnnotationPresent(EventMethod.class) && method.getParameterTypes().length == 1) {
                final Class<?> event = method.getParameterTypes()[0];
                if (!this.containsListener(event, listener)) {
                    if (this.listeners.containsKey(event)) {
                        this.listeners.get(event).add(new EventData<T>(listener, method));
                    }
                    else {
                        final List<EventData<T>> listeners = new ArrayList<EventData<T>>();
                        listeners.add(new EventData<T>(listener, method));
                        this.listeners.put(event, listeners);
                    }
                }
            }
        }
    }
    
    public void removeListener(final T listener) {
        for (final Class<?> eventType : this.listeners.keySet()) {
            final List<EventData<T>> listeners = this.listeners.get(eventType);
            for (int i = 0; i < listeners.size(); ++i) {
                final EventData<T> eventData = listeners.get(i);
                if (eventData.listener == listener) {
                    listeners.remove(eventData);
                }
            }
        }
    }
    
    public boolean containsListener(final Class<?> event, final T listener) {
        if (this.listeners.containsKey(event)) {
            for (final EventData<T> eventData : this.listeners.get(event)) {
                if (eventData.listener == listener && event.isAssignableFrom(eventData.method.getParameterTypes()[0])) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean containsListener(final T listener) {
        for (final Class<?> eventType : this.listeners.keySet()) {
            if (this.containsListener(eventType, listener)) {
                return true;
            }
        }
        return false;
    }
    
    public <E> E invoke(final E event) {
        if (this.listeners.containsKey(event.getClass())) {
            final List<EventData<T>> listeners = this.listeners.get(event.getClass());
            for (int i = 0; i < listeners.size(); ++i) {
                final EventData<T> eventData = listeners.get(i);
                try {
                    eventData.method.invoke(eventData.listener, event);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return event;
    }
    
    private class EventData<T>
    {
        T listener;
        Method method;
        
        EventData(final T listener, final Method method) {
            this.listener = listener;
            this.method = method;
        }
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    public @interface EventMethod {
    }
}
