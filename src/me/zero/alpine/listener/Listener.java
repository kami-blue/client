// 
// Decompiled by Procyon v0.5.36
// 

package me.zero.alpine.listener;

import net.jodah.typetools.TypeResolver;
import java.util.function.Predicate;

public final class Listener<T> implements EventHook<T>
{
    private final Class<T> target;
    private final EventHook<T> hook;
    private final Predicate<T>[] filters;
    private final byte priority;
    
    @SafeVarargs
    public Listener(final EventHook<T> hook, final Predicate<T>... filters) {
        this(hook, (byte)3, (Predicate[])filters);
    }
    
    @SafeVarargs
    public Listener(final EventHook<T> hook, final byte priority, final Predicate<T>... filters) {
        this.hook = hook;
        this.priority = priority;
        this.target = (Class<T>)TypeResolver.resolveRawArgument(EventHook.class, hook.getClass());
        this.filters = filters;
    }
    
    public final Class<T> getTarget() {
        return this.target;
    }
    
    public final byte getPriority() {
        return this.priority;
    }
    
    @Override
    public final void invoke(final T event) {
        if (this.filters.length > 0) {
            for (final Predicate<T> filter : this.filters) {
                if (!filter.test(event)) {
                    return;
                }
            }
        }
        this.hook.invoke(event);
    }
}
