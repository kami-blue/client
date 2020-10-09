package me.zeroeightsix.kami.util.event

import me.zeroeightsix.kami.KamiMod
import java.util.concurrent.ConcurrentHashMap

/**
 * Used for storing the map of objects and their listeners
 */
object ListenerManager {
    @JvmStatic
    private val listenerMap = ConcurrentHashMap<Any, ArrayList<Listener<*>>>()

    /**
     * Register the [listener] to the [ListenerManager]
     *
     * @param object object of the [listener] belongs to
     * @param listener listener to register
     */
    @JvmStatic
    fun register(`object`: Any, listener: Listener<*>) {
        listenerMap.getOrPut(`object`, ::ArrayList).let {
            val thread = Thread.currentThread()
            if (thread == KamiMod.MAIN_THREAD) {
                it.add(listener)
            } else {
                synchronized(thread) {
                    it.add(listener)
                }
            }
        }
    }

    /**
     * Get all registered listeners of this [object]
     *
     * @param object object to get listeners
     *
     * @return registered listeners of [object]
     */
    @JvmStatic
    fun getListeners(`object`: Any) = listenerMap[`object`]
}