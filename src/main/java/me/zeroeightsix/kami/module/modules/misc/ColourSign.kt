package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.GuiScreenEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.text.TextComponentString
import java.io.IOException

/**
 * Created by 086 on 8/04/2018.
 * Updated by Xiaro on 09/09/20
 */
@Module.Info(
        name = "ColourSign",
        description = "Allows ingame colouring of text on signs",
        category = Module.Category.MISC
)
class ColourSign : Module() {
    @EventHandler
    private val eventListener = Listener(EventHook { event: GuiScreenEvent.Displayed ->
        if (event.screen is GuiEditSign && isEnabled) {
            event.screen = KamiGuiEditSign((event.screen as GuiEditSign?)!!.tileSign)
        }
    })

    private class KamiGuiEditSign(teSign: TileEntitySign) : GuiEditSign(teSign) {
        @Throws(IOException::class)
        override fun actionPerformed(button: GuiButton) {
            if (button.id == 0) {
                tileSign.signText[editLine] = TextComponentString(tileSign.signText[editLine].formattedText.replace("(" + KamiMod.colour + ")(.)".toRegex(), "$1$1$2$2"))
            }
            super.actionPerformed(button)
        }

        @Throws(IOException::class)
        override fun keyTyped(typedChar: Char, keyCode: Int) {
            super.keyTyped(typedChar, keyCode)
            var s = (tileSign.signText[editLine] as TextComponentString).text
            s = s.replace("&", KamiMod.colour.toString() + "")
            tileSign.signText[editLine] = TextComponentString(s)
        }
    }
}