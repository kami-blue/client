package me.zeroeightsix.kami.command.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.util.text.MessageSendHelper
import java.io.File
import java.util.*

object GenerateWebsiteCommand : ClientCommand(
    name = "generatewebsite",
    description = "Generates the website modules to the file"
) {
    private const val path = "${KamiMod.DIRECTORY}modules.md"

    init {
        execute {
            commandScope.launch {
                val modulesList = ModuleManager.getModules()
                val moduleMap = TreeMap<Module.Category, MutableList<Module>>()
                modulesList.groupByTo(moduleMap) { it.category }

                launch(Dispatchers.IO) {
                    val file = File(path)
                    if (!file.exists()) file.createNewFile()
                    file.bufferedWriter().use {
                        it.appendLine("---")
                        it.appendLine("layout: default")
                        it.appendLine("title: Modules")
                        it.appendLine("description: A list of modules and commands this mod has")
                        it.appendLine("---")
                        it.appendLine("## Modules (${modulesList.size})")
                        it.newLine()

                        for ((category, modules) in moduleMap) {
                            it.appendLine("<details>")
                            it.appendLine("    <summary>$category (${modules.size})</summary>")
                            it.appendLine("    <p><ul>")
                            for (module in modules) {
                                it.appendLine("        <li>${module.name.value}<p><i>${module.description}</i></p></li>")
                            }
                            it.appendLine("    </ul></p>")
                            it.appendLine("</details>")
                        }
                    }
                }

                MessageSendHelper.sendChatMessage("Generated website to .minecraft/$path!")
            }
        }
    }
}