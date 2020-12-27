package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.util.text.MessageSendHelper
import org.lwjgl.opengl.Display


object FixGuiCommand : ClientCommand(
    name = "fixgui",
    alias = arrayOf("fixmygui"),
    description = "Fixes GUI scale and missing windows"
) {
    init {
        executeSafe {
            ClickGUI.resetScale()
            fixFrames()
            MessageSendHelper.sendChatMessage("Resized GUI and rescaled back to normal!")
        }
    }

    private fun fixFrames() {
        val kamiGUI = KamiMod.INSTANCE.guiManager ?: return
        val frames = ContainerHelper.getAllChildren(Frame::class.java, kamiGUI)

        for (frame in frames) {
            val divider = DisplayGuiScreen.getScale()
            if (frame.x > Display.getWidth() / divider) {
                frame.x = (Display.getWidth() / divider - frame.width).toInt()
            }

            if (frame.y > Display.getHeight() / divider) {
                frame.y = (Display.getHeight() / divider - frame.height).toInt()
            }

            if (frame.x < 0) frame.x = 0
            if (frame.y < 0) frame.y = 0
        }
    }
}