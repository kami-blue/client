package org.kamiblue.client.util.threads

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.KamiEventBus
import org.kamiblue.client.event.events.RenderEvent
import org.kamiblue.client.event.events.RenderOverlayEvent
import org.kamiblue.client.event.events.RenderWorldEvent
import org.kamiblue.client.util.Wrapper
import org.kamiblue.event.listener.listener

object MainThreadExecutor {
    private val jobs = ArrayList<MainThreadJob<*>>()
    private val mutex = Mutex()

    init {
        listener<TickEvent.ClientTickEvent>(Int.MAX_VALUE) {
            if (it.phase == TickEvent.Phase.START) runJobs()
        }

        listener<TickEvent.ClientTickEvent>(Int.MIN_VALUE) {
            if (it.phase == TickEvent.Phase.END) runJobs()
        }

        listener<RenderWorldEvent> {
            runJobs()
        }

        listener<RenderOverlayEvent>(Int.MIN_VALUE) {
            runJobs()
        }

        listener<RenderEvent>(Int.MIN_VALUE) {
            runJobs()
        }

        KamiEventBus.subscribe(this)
    }

    private fun runJobs() {
        if (jobs.isEmpty()) return

        runBlocking {
            mutex.withLock {
                coroutineScope {
                    jobs.forEach {
                        launch { it.run() }
                    }
                }
                jobs.clear()
            }
        }
    }

    suspend fun <T> add(block: suspend () -> T) =
        MainThreadJob(block).apply {
            if (Wrapper.minecraft.isCallingFromMinecraftThread) {
                run()
            } else {
                mutex.withLock {
                    jobs.add(this)
                }
            }
        }.deferred

    private class MainThreadJob<T>(private val block: suspend () -> T) {
        val deferred = CompletableDeferred<T>()

        suspend fun run() {
            deferred.completeWith(
                runCatching { block.invoke() }
            )
        }
    }
}