package me.zeroeightsix.kami.util.event

import io.netty.util.internal.ConcurrentSet
import java.util.concurrent.ConcurrentHashMap

/**
 * Includes basic EventBus implementations
 */
object EventBus {

    /**
     * A basic implementation of [AbstractEventBus], for thread safe alternative, use [ConcurrentEventBus] instead
     */
    open class SingleThreadEventBus : AbstractEventBus() {
        final override val subscribedObjects = HashMap<Any, MutableSet<Listener<*>>>()
        final override val subscribedListeners = HashMap<Class<*>, MutableSet<Listener<*>>>()
        final override val newSet get() = HashSet<Listener<*>>()
    }

    /**
     * A thread-safe alternative of [SingleThreadEventBus], note that this would reduce
     * performance in single thread tasks.
     */
    open class ConcurrentEventBus : AbstractEventBus() {
        final override val subscribedObjects = ConcurrentHashMap<Any, MutableSet<Listener<*>>>()
        final override val subscribedListeners = ConcurrentHashMap<Class<*>, MutableSet<Listener<*>>>()
        final override val newSet get() = ConcurrentSet<Listener<*>>()
    }

    /**
     * A thread-safe implementation of [AbstractEventBus] and [IMultiEventBus]
     */
    open class MasterEventBus : ConcurrentEventBus(), IMultiEventBus {
        private val subscribedEventBus = ConcurrentSet<IEventBus>()

        final override fun subscribe(eventBus: IEventBus) {
            subscribedEventBus.add(eventBus)
        }

        final override fun unsubscribe(eventBus: IEventBus) {
            subscribedEventBus.remove(eventBus)
        }

        final override fun post(event: Any) {
            super.post(event)
            for (eventBus in subscribedEventBus) eventBus.post(event)
        }
    }
}