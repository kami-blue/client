package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.util.Macro
import me.zeroeightsix.kami.util.MessageSendHelper

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 04/05/20
 */
object MacroManager {

    /**
     * Reads macros from KAMIBlueMacros.json into the macros Map
     */
    fun registerMacros() {
        KamiMod.log.info("Registering macros...")
        Macro.readFileToMemory()
        KamiMod.log.info("Macros registered")
    }

    /**
     * Saves macros from the macros Map into KAMIBlueMacros.json
     */
    fun saveMacros() {
        KamiMod.log.info("Saving macros...")
        Macro.writeMemoryToFile()
        KamiMod.log.info("Macros saved")
    }

    /**
     * Sends the message or command, depending on which one it is
     * @param keyCode int keycode of the key the was pressed
     */
    fun sendMacro(keyCode: Int) {
        val macrosForThisKey = Macro.getMacrosForKey(keyCode) ?: return
        for (currentMacro in macrosForThisKey) {
            if (currentMacro!!.startsWith(Command.getCommandPrefix())) { // this is done instead of just sending a chat packet so it doesn't add to the chat history
                MessageSendHelper.sendKamiCommand(currentMacro, false) // ie, the false here
            } else {
                MessageSendHelper.sendServerMessage(currentMacro)
            }
        }
    }
}