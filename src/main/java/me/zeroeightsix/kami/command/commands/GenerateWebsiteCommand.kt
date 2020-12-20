package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.util.text.MessageSendHelper

object GenerateWebsiteCommand : ClientCommand(
    name = "generatewebsite",
    description = "Generates the website modules to the log"
) {
    init {
        execute {
            val mods = ModuleManager.getModules()
            val modCategories = arrayOf("Chat", "Combat", "Client", "Misc", "Movement", "Player", "Render")
            KamiMod.LOG.info("\n"
                + "---\n"
                + "layout: default\n"
                + "title: Modules\n"
                + "description: A list of modules and commands this mod has\n"
                + "---"
                + "\n## Modules (${mods.size})\n")

            for (modCategory in modCategories) {
                var totalMods = 0
                var str = ""
                for (module in mods) {
                    if (!module.isProduction) continue
                    if (!module.category.toString().equals(modCategory, ignoreCase = true)) continue
                    totalMods++
                    str += "        <li>" + module.name.value + "<p><i>" + module.description + "</i></p></li>\n"
                }
                KamiMod.LOG.info("<details>")
                KamiMod.LOG.info("    <summary>$modCategory ($totalMods)</summary>")
                KamiMod.LOG.info("    <p><ul>")
                KamiMod.LOG.info(str)
                KamiMod.LOG.info("    </ul></p>")
                KamiMod.LOG.info("</details>")
            }
            MessageSendHelper.sendChatMessage("Generated website to log file!")

        }
    }
}