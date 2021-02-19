package org.kamiblue.client.event

import io.netty.util.internal.ConcurrentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.kamiblue.client.util.Wrapper
import org.kamiblue.event.eventbus.AbstractAsyncEventBus
import org.kamiblue.event.listener.AsyncListener
import org.kamiblue.event.listener.Listener
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

object KamiEventBus : AbstractAsyncEventBus() {
    override val subscribedObjects = ConcurrentHashMap<Any, List<Listener<*>>>()
    override val subscribedListeners = ConcurrentHashMap<Class<*>, MutableSet<Listener<*>>>()
    override val newSet get() = ConcurrentSkipListSet<Listener<*>>(Comparator.reverseOrder())

    override val subscribedObjectsAsync = ConcurrentHashMap<Any, List<AsyncListener<*>>>()
    override val subscribedListenersAsync = ConcurrentHashMap<Class<*>, MutableSet<AsyncListener<*>>>()
    override val newSetAsync get() = ConcurrentSet<AsyncListener<*>>()

    override fun post(event: Any) {
        invokeSerial(event, false)
        invokeParallel(event)
    }

    fun post(event: ProfilerEvent) {
        Wrapper.minecraft.profiler.startSection(event.profilerName)

        postProfiler(event)

        Wrapper.minecraft.profiler.endSection()
    }

    fun postProfiler(event: Any) {
        Wrapper.minecraft.profiler.startSection("serial")
        invokeSerial(event, true)

        Wrapper.minecraft.profiler.endStartSection("parallel")
        invokeParallel(event)
        Wrapper.minecraft.profiler.endSection()
    }

    private fun invokeSerial(event: Any, isProfilerEvent: Boolean) {
        subscribedListeners[event.javaClass]?.forEach {
            if (isProfilerEvent) Wrapper.minecraft.profiler.startSection(it.ownerName)

            @Suppress("UNCHECKED_CAST") // IDE meme
            (it as Listener<Any>).function.invoke(event)

            if (isProfilerEvent) Wrapper.minecraft.profiler.endSection()
        }
    }

    private fun invokeParallel(event: Any) {
        val listeners = subscribedListenersAsync[event.javaClass] ?: return

        if (listeners.isNotEmpty()) {
            runBlocking {
                listeners.forEach {
                    launch(Dispatchers.Default) {
                        @Suppress("UNCHECKED_CAST") // IDE meme
                        (it as AsyncListener<Any>).function.invoke(event)
                    }
                }
            }
        }
    }
}