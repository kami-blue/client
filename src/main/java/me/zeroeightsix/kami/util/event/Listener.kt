package me.zeroeightsix.kami.util.event

/**
 * Listener for event listening
 *
 * @param function action to perform when this listener gets called by the event bus
 */
class Listener<T : Any>(val event: Class<T>, private val function: (T) -> Unit) {

    fun invoke(event: T) = function(event)

    override fun equals(other: Any?) = this === other
            || (other is Listener<*>
            && other.event == this.event
            && other.function == this.function)

    override fun hashCode(): Int {
        return 31 * event.hashCode() + function.hashCode()
    }

    companion object {
        /**
         * Create and register a new listener for this object
         *
         * @param T target event type
         * @param event target event
         * @param function action to perform when this listener gets called by the event bus
         */
        @JvmStatic
        @Deprecated("For Java use only", ReplaceWith("Any.register(this, Listener(event, function))", "me.zeroeightsix.kami.util.event.listener"))
        fun <T : Any> listener(event: Class<T>, function: (event: T) -> Unit) {
            ListenerManager.register(this, Listener(event, function))
        }
    }
}

/**
 * Create and register a new listener for this object
 *
 * @param T target event
 * @param function action to perform when this listener gets called by the event bus
 */
inline fun <reified T : Any> Any.listener(noinline function: (event: T) -> Unit) {
    ListenerManager.register(this, Listener(T::class.java, function))
}