package me.zeroeightsix.kami.util.event

import io.netty.util.internal.ConcurrentSet
import me.zeroeightsix.kami.KamiMod
import java.util.concurrent.ConcurrentHashMap

/**
 * Includes basic EventBus implementations
 */
object EventBus {

    /**
     * A basic implementation of [AbstractEventBus], for thread safe alternative, use [SynchronizedEventBus], [ConcurrentEventBus] instead
     */
    open class SingleThreadEventBus : AbstractEventBus() {
        final override val subscribedObjects = HashMap<Any, MutableSet<Listener<*>>>()
        final override val subscribedListeners = HashMap<Class<*>, MutableSet<Listener<*>>>()
        final override val newSet get() = HashSet<Listener<*>>()
    }

    /**
     * A thread-safe alternative of [SingleThreadEventBus], note that this would reduce
     * performance in non [mainThread].
     */
    open class SynchronizedEventBus(val mainThread: Thread) : SingleThreadEventBus() {
        override fun subscribe(`object`: Any) {
            val thread = Thread.currentThread()
            if (thread == KamiMod.MAIN_THREAD) {
                super.subscribe(`object`)
            } else {
                synchronized(thread) {
                    super.subscribe(`object`)
                }
            }
        }

        override fun unsubscribe(`object`: Any) {
            val thread = Thread.currentThread()
            if (thread == KamiMod.MAIN_THREAD) {
                super.unsubscribe(`object`)
            } else {
                synchronized(thread) {
                    super.unsubscribe(`object`)
                }
            }
        }

        override fun post(event: Any) {
            val thread = Thread.currentThread()
            if (thread == KamiMod.MAIN_THREAD) {
                super.post(event)
            } else {
                synchronized(thread) {
                    super.post(event)
                }
            }
        }
    }

    /**
     * A concurrent alternative of [SingleThreadEventBus], note that this would reduce
     * performance in single thread tasks.
     */
    open class ConcurrentEventBus : AbstractEventBus() {
        final override val subscribedObjects = ConcurrentHashMap<Any, MutableSet<Listener<*>>>()
        final override val subscribedListeners = ConcurrentHashMap<Class<*>, MutableSet<Listener<*>>>()
        final override val newSet get() = ConcurrentSet<Listener<*>>()
    }

    /**
     * A concurrent implementation of [AbstractEventBus] and [IMultiEventBus]
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