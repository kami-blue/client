package me.zeroeightsix.kami.setting.settings.impl.collection

import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.setting.settings.ImmutableSetting
import me.zeroeightsix.kami.util.translation.TranslationKey
import me.zeroeightsix.kami.util.translation.TranslationKeyBlank

class MapSetting<K : Any, V : Any, T : MutableMap<K, V>>(
    name: TranslationKey,
    override val value: T,
    visibility: () -> Boolean = { true },
    description: TranslationKey = TranslationKeyBlank()
) : ImmutableSetting<T>(name, value, visibility, { _, input -> input }, description) {

    override val defaultValue: T = valueClass.newInstance()
    private val lockObject = Any()
    private val type = object : TypeToken<Map<K, V>>() {}.type

    init {
        value.toMap(defaultValue)
    }
    override fun resetValue() {
        synchronized(lockObject) {
            value.clear()
            value.putAll(defaultValue)
        }
    }

    override fun write(): JsonElement = gson.toJsonTree(value)

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonArray?.let {
            val cacheMap = gson.fromJson<Map<K, V>>(it, type)

            synchronized(lockObject) {
                value.clear()
                value.putAll(cacheMap)
            }
        }
    }

    override fun toString() = value.entries.joinToString { "${it.key} to ${it.value}" }

}