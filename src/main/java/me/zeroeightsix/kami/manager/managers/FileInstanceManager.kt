package me.zeroeightsix.kami.manager.managers

import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.util.Macro
import java.io.FileWriter
import java.io.IOException
import java.util.*

object FileInstanceManager : Manager() {
    /**
     * Map of all the macros.
     * KeyCode, Actions
     */
    var macros = LinkedHashMap<Int, ArrayList<String>>()

    /**
     * Super lazy fix for Windows users sometimes saving empty files
     */
    @JvmStatic
    fun fixEmptyFiles() {
        if (!Macro.file.exists()) {
            try {
                val w = FileWriter(Macro.file)
                w.write("{}")
                w.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    init {
        fixEmptyFiles()
    }
}