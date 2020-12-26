package me.zeroeightsix.kami.event

import io.netty.util.internal.ConcurrentSet
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import me.zeroeightsix.kami.util.threads.defaultScope
import org.kamiblue.event.eventbus.AbstractAsyncEventBus
import org.kamiblue.event.listener.AsyncListener
import org.kamiblue.event.listener.Listener
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

object KamiEventBus : AbstractAsyncEventBus() {
    override val subscribedObjects = ConcurrentHashMap<Any, MutableSet<Listener<*>>>()
    override val subscribedListeners = ConcurrentHashMap<Class<*>, MutableSet<Listener<*>>>()
    override val newSet get() = ConcurrentSkipListSet<Listener<*>>(Comparator.reverseOrder())

    override val subscribedObjectsAsync = ConcurrentHashMap<Any, MutableSet<AsyncListener<*>>>()
    override val subscribedListenersAsync = ConcurrentHashMap<Class<*>, MutableSet<AsyncListener<*>>>()
    override val newSetAsync get() = ConcurrentSet<AsyncListener<*>>()

    override fun post(event: Any) {
        val asyncList = subscribedListenersAsync[event.javaClass]?.map {
            defaultScope.async {
                @Suppress("UNCHECKED_CAST") // IDE meme
                (it as AsyncListener<Any>).function.invoke(event)
            }
        }

        subscribedListeners[event.javaClass]?.forEach {
            @Suppress("UNCHECKED_CAST") // IDE meme
            (it as Listener<Any>).function.invoke(event)
        }

        runBlocking {
            asyncList?.awaitAll()
        }
    }
}