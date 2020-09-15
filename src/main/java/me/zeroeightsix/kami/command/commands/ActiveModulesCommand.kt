package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.module.Module.Category
import me.zeroeightsix.kami.module.ModuleManager.getModuleT
import me.zeroeightsix.kami.module.modules.client.ActiveModules
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 05/04/20
 */
class ActiveModulesCommand : Command("activemodules", ChunkBuilder().append("category").append("r").append("g").append("b").build(), "activemods", "modules") {

    val categories = Category.values().filter { !it.isHidden }.joinToString(separator = ",\n")

    override fun call(args: Array<String?>) {
        for (i in 0..3) {
            if (args.getOrNull(i) == null) {
                sendErrorMessage(chatLabel + "Missing arguments! Please fill out the command syntax properly")
                return
            }
        }
        val category = convertToCategory(args[0]) ?: return
        val color = IntArray(3) { convertToColor(args[it + 1]) ?: return }
        getModuleT(ActiveModules::class.java)!!.setColor(category, color)
    }

    private fun convertToCategory(string: String?): Category? {
        val category = string?.toUpperCase()?.let { Category.valueOf(it) }
        return if (category == null || category == Category.HIDDEN) {
            sendErrorMessage("Category $string not found! Valid categories:\n $categories")
            null
        } else {
            category
        }
    }

    private fun convertToColor(string: String?): Int? {
        return string?.toIntOrNull() ?: let {
            sendErrorMessage("Invalid color input $string")
            null
        }
    }

    init {
        setDescription("Allows you to customize ActiveModule's category colours")
    }
}