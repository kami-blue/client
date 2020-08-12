// 
// Decompiled by Procyon v0.5.36
// 

package me.zero.alpine;

import java.lang.annotation.Annotation;
import me.zero.alpine.listener.EventHandler;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Objects;
import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import me.zero.alpine.listener.Listener;
import java.util.List;
import java.util.Map;

public class EventManager implements EventBus
{
    private final Map<Object, List<Listener>> SUBSCRIPTION_CACHE;
    private final Map<Class<?>, List<Listener>> SUBSCRIPTION_MAP;
    private final List<EventBus> ATTACHED_BUSES;
    
    public EventManager() {
        this.SUBSCRIPTION_CACHE = new HashMap<Object, List<Listener>>();
        this.SUBSCRIPTION_MAP = new HashMap<Class<?>, List<Listener>>();
        this.ATTACHED_BUSES = new ArrayList<EventBus>();
    }
    
    @Override
    public void subscribe(final Object object) {
        final List<Listener> listeners = this.SUBSCRIPTION_CACHE.computeIfAbsent(object, o -> Arrays.stream(o.getClass().getDeclaredFields()).filter(EventManager::isValidField).map(field -> asListener(o, field)).filter(Objects::nonNull).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList()));
        listeners.forEach(this::subscribe);
        if (!this.ATTACHED_BUSES.isEmpty()) {
            this.ATTACHED_BUSES.forEach(bus -> bus.subscribe(object));
        }
    }
    
    @Override
    public void subscribe(final Object... objects) {
        Arrays.stream(objects).forEach(this::subscribe);
    }
    
    @Override
    public void subscribe(final Iterable<Object> objects) {
        objects.forEach(this::subscribe);
    }
    
    @Override
    public void unsubscribe(final Object object) {
        final List<Listener> objectListeners = this.SUBSCRIPTION_CACHE.get(object);
        if (objectListeners == null) {
            return;
        }
        final List obj;
        this.SUBSCRIPTION_MAP.values().forEach(listeners -> {
            Objects.requireNonNull(obj);
            listeners.removeIf(obj::contains);
            return;
        });
        if (!this.ATTACHED_BUSES.isEmpty()) {
            this.ATTACHED_BUSES.forEach(bus -> bus.unsubscribe(object));
        }
    }
    
    @Override
    public void unsubscribe(final Object... objects) {
        Arrays.stream(objects).forEach(this::unsubscribe);
    }
    
    @Override
    public void unsubscribe(final Iterable<Object> objects) {
        objects.forEach(this::unsubscribe);
    }
    
    @Override
    public void post(final Object event) {
        final List<Listener> listeners = this.SUBSCRIPTION_MAP.get(event.getClass());
        if (listeners != null) {
            listeners.forEach(listener -> listener.invoke(event));
        }
        if (!this.ATTACHED_BUSES.isEmpty()) {
            this.ATTACHED_BUSES.forEach(bus -> bus.post(event));
        }
    }
    
    @Override
    public void attach(final EventBus bus) {
        if (!this.ATTACHED_BUSES.contains(bus)) {
            this.ATTACHED_BUSES.add(bus);
        }
    }
    
    @Override
    public void detach(final EventBus bus) {
        if (this.ATTACHED_BUSES.contains(bus)) {
            this.ATTACHED_BUSES.remove(bus);
        }
    }
    
    private static boolean isValidField(final Field field) {
        return field.isAnnotationPresent(EventHandler.class) && Listener.class.isAssignableFrom(field.getType());
    }
    
    private static Listener asListener(final Object object, final Field field) {
        try {
            final boolean accessible = field.isAccessible();
            field.setAccessible(true);
            final Listener listener = (Listener)field.get(object);
            field.setAccessible(accessible);
            if (listener == null) {
                return null;
            }
            if (listener.getPriority() > 5 || listener.getPriority() < 1) {
                throw new RuntimeException("Event Priority out of bounds! %s");
            }
            return listener;
        }
        catch (IllegalAccessException e) {
            return null;
        }
    }
    
    private void subscribe(final Listener listener) {
        List<Listener> listeners;
        int index;
        for (listeners = this.SUBSCRIPTION_MAP.computeIfAbsent(listener.getTarget(), target -> new ArrayList()), index = 0; index < listeners.size() && listener.getPriority() >= listeners.get(index).getPriority(); ++index) {}
        listeners.add(index, listener);
    }
}
