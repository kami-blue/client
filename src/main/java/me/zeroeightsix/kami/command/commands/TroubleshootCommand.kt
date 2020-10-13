package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.module.ModuleManager.getModules
import me.zeroeightsix.kami.util.ConfigUtils
//import me.zeroeightsix.kami.util.SupportUtil
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendRawChatMessage
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.common.ForgeVersion.*
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Loader
import org.lwjgl.opengl.GL11
import java.io.File
import java.lang.System.getProperty
import java.util.stream.Stream

class TroubleshootCommand : Command("troubleshoot", null, "tsc") {
    override fun call(args: Array<String?>) {
        var enabled = ""
        val liteLoader = if (FMLCommonHandler.instance().modName.contains("LiteLoader")) "LiteLoader: YES" else "LiteLoader: NO"
        val modInfoStr = arrayListOf<String>()
        val mods = getModules()
        val doTicketPost = args.size > 1

        mods.forEach {
            if (it.isDisabled) return
            enabled += "${it.name}, "
        }

        if (Loader.instance().activeModList.size > 5) { //the 5 always active mods are listed a couple lines down
            for (mod in Loader.instance().activeModList) {
                if (Stream.of("minecraft", "mcp", "FML", "forge", "kamiblue").anyMatch { s: String -> mod.modId.contains(s) }) {
                    continue
                } // exclude obvious and redundant
                modInfoStr.add(mod.name + " " + mod.version)
            }
        }

        fun sendToChat() {
            sendRawChatMessage(
                    "Troubleshooting Information:\n" +
                            "Enabled modules:\n${TextFormatting.GRAY}$enabled\n" +
                            "${KamiMod.MODNAME} ${KamiMod.KAMI_KANJI} ${KamiMod.VER_FULL_BETA}\n" +
                            "Forge Version: ${getMajorVersion()}.${getMinorVersion()}.${getRevisionVersion()}.${getBuildVersion()}\n" +
                            "$liteLoader\n" +
                            "Other Forge mods:$modInfoStr\n" +
                            "Java: ${getProperty("java.runtime.name")} ${getProperty("java.runtime.version")} ${getProperty("java.vm.name")} ${getProperty("java.vm.vendor")}\n" +
                            "OS: ${getProperty("os.name")} ${getProperty("os.arch")}CPU: ${OpenGlHelper.getCpu()}GPU: ${GlStateManager.glGetString(GL11.GL_VENDOR)} / ${GlStateManager.glGetString(GL11.GL_RENDERER)}\n" +
                            "Please send a screenshot of the full output to the developer or moderator who's helping you!"
            )
        }

        fun sendToSupport() {
            //SupportUtil.uploadJsonFile(File("KAMIBlueConfig.json"))
        }
        if (doTicketPost) ConfigUtils.saveConfiguration()
        sendToChat()
    }
    init {
        setDescription("Prints troubleshooting information")
    }
}