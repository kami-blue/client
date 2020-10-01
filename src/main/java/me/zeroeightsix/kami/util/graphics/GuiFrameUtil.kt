package me.zeroeightsix.kami.util.graphics

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.gui.kami.KamiGUI
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.Display

/**
 * @author l1ving
 * Created by l1ving on 24/03/20
 * Updated by Xiaro on 20/08/20
 */
object GuiFrameUtil {
    // This is bad, but without a rearchitecture, it's probably staying... - 20kdc and l1ving
    @JvmStatic
    fun getFrameByName(name: String?): Frame? {
        val kamiGUI = KamiMod.getInstance().guiManager ?: return null
        val frames = ContainerHelper.getAllChildren(Frame::class.java, kamiGUI)
        for (frame in frames) if (frame.title.equals(name, ignoreCase = true)) return frame
        return null
    }

    /* Additional method to prevent calling kamiGui if you already have an instance */
    @JvmStatic
    fun getFrameByName(kamiGUI: KamiGUI?, name: String?): Frame? {
        if (kamiGUI == null) return null
        val frames = ContainerHelper.getAllChildren(Frame::class.java, kamiGUI)
        for (frame in frames) if (frame.title.equals(name, ignoreCase = true)) return frame
        return null
    }

    @JvmStatic
    fun fixFrames(mc: Minecraft) {
        val kamiGUI = KamiMod.getInstance().guiManager
        if (kamiGUI == null || mc.player == null) return
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