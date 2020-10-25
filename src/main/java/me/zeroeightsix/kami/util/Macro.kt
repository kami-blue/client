package me.zeroeightsix.kami.util

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.manager.managers.FileInstanceManager
import me.zeroeightsix.kami.util.text.MessageSendHelper
import java.io.*
import java.util.*

/**
 * @author l1ving
 * Created by l1ving on 04/05/20
 */
object Macro {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private const val configName = "KAMIBlueMacros.json"
    val file = File(configName)

    fun writeMemoryToFile(): Boolean {
        return try {
            val fw = FileWriter(file, false)
            gson.toJson(FileInstanceManager.macros, fw)
            fw.flush()
            fw.close()
            KamiMod.log.info("Macro saved")
            true
        } catch (e: IOException) {
            KamiMod.log.info("Failed saving macro")
            e.printStackTrace()
            false
        }
    }

    fun readFileToMemory(): Boolean {
        var success = false
        try {
            try {
                FileInstanceManager.macros = gson.fromJson(FileReader(file), object : TypeToken<LinkedHashMap<Int?, List<String?>?>?>() {}.type)!!
                KamiMod.log.info("Macro loaded")
                success = true
            } catch (e: FileNotFoundException) {
                KamiMod.log.warn("Could not find file $configName, clearing the macros list")
                FileInstanceManager.macros.clear()
            }
        } catch (e: IllegalStateException) {
            KamiMod.log.warn("$configName is empty!")
            FileInstanceManager.macros.clear()
        }
        return success
    }

    fun getMacrosForKey(keycode: Int): List<String?>? {
        val entry = FileInstanceManager.macros.entries.find { it.key == keycode } ?: return null
        return entry.value
    }

    fun addMacroToKey(keycode: Int?, macro: String?) {
        if (keycode == null || macro.isNullOrBlank()) return  // prevent trying to add a null macro
        FileInstanceManager.macros.getOrPut(keycode, ::arrayListOf).add(macro)
    }

    fun removeMacro(keycode: Int) {
        FileInstanceManager.macros.keys.removeIf { it == keycode }
    }

    fun sendMacrosToChat(messages: Array<String?>) {
        for (s in messages) MessageSendHelper.sendRawChatMessage("[$s]")
    }
}