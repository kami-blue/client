package me.zeroeightsix.kami.util.event

/**
 * The basic Interface for an event bus
 */
interface IEventBus {
    // Subscribing
    /**
     * Subscribes all [objects]' listeners to this event bus
     */
    fun subscribe(vararg objects: Any)

    /**
     * Subscribes all [objects]' listeners to this event bus
     */
    fun subscribe(objects: Iterable<Any>)

    /**
     * Subscribe an [object]'s listeners to this event bus
     */
    fun subscribe(`object`: Any)
    // End of subscribing


    // Unsubscribing
    /**
     * unsubscribes all [objects]' listeners from this event bus
     */
    fun unsubscribe(vararg objects: Any)

    /**
     * unsubscribes all [objects]' listeners from this event bus
     */
    fun unsubscribe(objects: Iterable<Any>)

    /**
     * unsubscribes an [object]'s listeners from this event bus
     */
    fun unsubscribe(`object`: Any)
    // End of unsubscribing


    // Event posting
    /**
     * Posts all events to this event bus, and calls
     * All the listeners of each event
     */
    fun post(vararg events: Any)

    /**
     * Posts all events to this event bus, and calls
     * All the listeners of each event
     */
    fun post(events: Iterable<Any>)

    /**
     * Posts an event to this event bus, and calls
     * All the listeners of this event
     */
    fun post(event: Any)
    // End of event posting
}