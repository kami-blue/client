package me.zeroeightsix.kami.manager.managers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter

object MacroManager : Manager {
    private var macroMap = LinkedHashMap<Int, ArrayList<String>>()
    val isEmpty get() = macroMap.isEmpty()
    val macros: Map<Int, List<String>> get() = macroMap

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val type = object : TypeToken<LinkedHashMap<Int, List<String>>>() {}.type
    private const val configName = "KAMIBlueMacros.json"
    private val file get() = File(configName)

    init {
        listener<InputEvent.KeyInputEvent> {
            sendMacro(Keyboard.getEventKey())
        }
    }

    /**
     * Reads macros from KAMIBlueMacros.json into the macros Map
     */
    fun loadMacros(): Boolean {
        return try {
            FileReader(file).buffered().use {
                macroMap = gson.fromJson(it, type)
                KamiMod.LOG.info("Macro loaded")
            }
            true
        } catch (e: FileNotFoundException) {
            KamiMod.LOG.warn("Could not find file $configName")
            false
        } catch (e: Exception) {
            KamiMod.LOG.warn("Failed loading macro", e)
            false
        }
    }

    /**
     * Saves macros from the macros Map into KAMIBlueMacros.json
     */
    fun saveMacros(): Boolean {
        return try {
            FileWriter(file, false).buffered().use {
                gson.toJson(macroMap, it)
                KamiMod.LOG.info("Macro saved")
            }
            true
        } catch (e: Exception) {
            KamiMod.LOG.warn("Failed saving macro", e)
            false
        }
    }

    /**
     * Sends the message or command, depending on which one it is
     * @param keyCode int keycode of the key the was pressed
     */
    private fun sendMacro(keyCode: Int) {
        val macros = getMacros(keyCode) ?: return
        for (macro in macros) {
            if (macro.startsWith(CommandManager.prefix)) { // this is done instead of just sending a chat packet so it doesn't add to the chat history
                MessageSendHelper.sendKamiCommand(macro) // ie, the false here
            } else {
                MessageManager.sendMessageDirect(macro)
            }
        }
    }

    fun getMacros(keycode: Int) = macroMap[keycode]

    fun addMacroToKey(keycode: Int, macro: String) {
        macroMap.getOrPut(keycode, ::ArrayList).add(macro)
    }

    fun removeMacro(keycode: Int) {
        macroMap.remove(keycode)
    }

}