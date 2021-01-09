package me.zeroeightsix.kami.util.translation

open class TranslationKey(val key: String, val defaultValue : String) {
    //Use defaultValue for things such as config, we don't want all config being reset on language change.

    constructor(key: String) : this(key, key)

    private var cache = Cache()

    open val value : String
        get() = cache.value.value

    fun invalidateCache(){
        cache = Cache()
    }


    private inner class Cache{
        val value = lazy{
            KamiLang.get(this@TranslationKey)
        }
    }



}