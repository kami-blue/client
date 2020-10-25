package me.zeroeightsix.kami.setting.groups

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import me.zeroeightsix.kami.setting.Setting

open class SettingGroup(
        val name: String
) {

    protected val subSetting = LinkedHashMap<String, Setting<*>>()


    fun getSettings() = subSetting.values.toList()

    open fun addSetting(setting: Setting<*>) {
        subSetting[setting.name.toLowerCase()] = setting
    }


    open fun write(): JsonObject = JsonObject().apply {
        add("Name", JsonPrimitive(name))

        add("Settings", JsonObject().apply {
            for (setting in subSetting.values) {
                add(setting.name, setting.write())
            }
        })
    }

    open fun read(jsonObject: JsonObject?) {
        if (jsonObject == null) return

        jsonObject.getAsJsonObject("Settings").also {
            for (setting in subSetting.values) {
                setting.read(it.get(setting.name))
            }
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SettingGroup

        if (name != other.name) return false
        if (subSetting != other.subSetting) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + subSetting.hashCode()
        return result
    }

}