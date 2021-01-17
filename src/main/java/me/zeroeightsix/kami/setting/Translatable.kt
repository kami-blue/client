package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.util.translation.TranslationKey

open class Translatable {
    fun getTranslationKey(name: String): TranslationKey {
        return TranslationKey(this.javaClass.name.removePrefix("me.zeroeightsix.kami.") + ".$name")
    }

    protected fun getTranslationKey(name: String, default: String): TranslationKey {
        return TranslationKey(this.javaClass.name.removePrefix("me.zeroeightsix.kami.") + ".$name", default)
    }
}