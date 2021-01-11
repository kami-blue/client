package me.zeroeightsix.kami.util.translation

import me.zeroeightsix.kami.event.ClientEvent
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.util.threads.toSafe

open class TranslationKey(val key: String, private val defaultValueInternal : String) {
    //Use defaultValue for things such as config, we don't want all config being reset on language change.

    constructor(key: String) : this(key, key)

    private var cache = Cache()

    val defaultValue : String
        get() = defaultValueInternal

    open val value : String
        get() = cache.value.value

    fun invalidateCache(){
        cache = Cache()
    }

    fun onInvalidate(block: TranslationKey.() -> Unit) {
        block.run { this@TranslationKey }
    }

    private inner class Cache{
        val value = lazy{
            KamiLang.get(this@TranslationKey)
        }
    }



}