package me.zeroeightsix.kami.util

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Macros
import java.io.*
import java.util.*

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 04/05/20
 */
object Macro {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private const val configName = "KAMIBlueMacros.json"
    private val file = File(configName)

    fun writeMemoryToFile() {
        try {
            val fw = FileWriter(file, false)
            gson.toJson(Macros.macros, fw)
            fw.flush()
            fw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun readFileToMemory() {
        try {
            Macros.macros = gson.fromJson(FileReader(file), object : TypeToken<HashMap<Int?, List<String?>?>?>() {}.type)
        } catch (e: FileNotFoundException) {
            KamiMod.log.warn("Could not find file $configName, clearing the macros list")
            Macros.macros.clear()
        }
    }

    fun getMacrosForKey(keycode: Int): List<String?>? {
        for ((key, value) in Macros.macros) {
            if (keycode == key.toInt()) {
                return value
            }
        }
        return null
    }

    fun addMacroToKey(keycode: Int?, macro: String?) {
        if (macro == null) return  // prevent trying to add a null macro
        Macros.macros.getOrPut(keycode, ::mutableListOf).add(macro)
    }

    fun removeMacro(keycode: Int) {
        for (entry in Macros.macros.entries) {
            if (entry.key == keycode) {
                entry.setValue(null)
            }
        }
    }

    fun sendMacrosToChat(messages: Array<String?>) {
        for (s in messages) MessageSendHelper.sendRawChatMessage("[$s]")
    }
}