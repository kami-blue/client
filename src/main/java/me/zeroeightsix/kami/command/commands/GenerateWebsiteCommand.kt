package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage

/**
 * @author l1ving
 * Updated by l1ving on 18/03/20
 * Updated by Xiaro on 18/08/20
 *
 * Horribly designed command for uh, generating the modules page on the website. This was the easiest way I could do it, but maybe not the most efficient.
 */
class GenerateWebsiteCommand : Command("genwebsite", null) {
    override fun call(args: Array<String>) {
        val mods = ModuleManager.getModules()
        val modCategories = arrayOf("Chat", "Combat", "Client", "Misc", "Movement", "Player", "Render")
        KamiMod.log.info("\n"
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
                str += "        <li>" + module.name.value + "<p><i>" + module.description + "</i></p></li>"
            }
            KamiMod.log.info("<details>")
            KamiMod.log.info("    <summary>$modCategory ($totalMods)</summary>")
            KamiMod.log.info("    <p><ul>")
            KamiMod.log.info(str)
            KamiMod.log.info("    </ul></p>")
            KamiMod.log.info("</details>")
        }
        sendChatMessage(getLabel().substring(0, 1).toUpperCase() + getLabel().substring(1) + ": Generated website to log file!")
    }

    private fun nameAndDescription(module: Module): String {
        return "<li>" + module.name + "<p><i>" + module.description + "</i></p></li>"
    }

    init {
        setDescription("Generates the module page for the website")
    }
}