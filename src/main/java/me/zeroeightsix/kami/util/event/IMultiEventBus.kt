package me.zeroeightsix.kami.util.event

/**
 * Event bus that allow subscribing another [IEventBus] to it
 */
interface IMultiEventBus {
    // Subscribing
    /**
     * Subscribe an [eventBus] to this event bus
     */
    fun subscribe(eventBus: IEventBus)
    // End of subscribing


    // Unsubscribing
    /**
     * unsubscribes an [eventBus] from this event bus
     */
    fun unsubscribe(eventBus: IEventBus)
    // End of unsubscribing
}