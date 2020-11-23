package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.GuiScreenEvent
import me.zeroeightsix.kami.mixin.client.accessor.gui.edtiLine
import me.zeroeightsix.kami.mixin.client.accessor.gui.tileSign
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.text.TextComponentString
import java.io.IOException

@Module.Info(
        name = "ColorSign",
        description = "Allows ingame coloring of text on signs",
        category = Module.Category.MISC
)
object ColorSign : Module() {
    init {
        listener<GuiScreenEvent.Displayed> { event ->
            if (event.screen !is GuiEditSign) return@listener
            (event.screen as? GuiEditSign?)?.tileSign?.let { event.screen = KamiGuiEditSign(it) }
        }
    }

    private class KamiGuiEditSign(teSign: TileEntitySign) : GuiEditSign(teSign) {
        @Throws(IOException::class)
        override fun actionPerformed(button: GuiButton) {
            if (button.id == 0) {
                tileSign.signText[edtiLine] = TextComponentString(tileSign.signText[edtiLine].formattedText.replace("(§)(.)".toRegex(), "$1$1$2$2"))
            }
            super.actionPerformed(button)
        }

        @Throws(IOException::class)
        override fun keyTyped(typedChar: Char, keyCode: Int) {
            super.keyTyped(typedChar, keyCode)
            var s = (tileSign.signText[edtiLine] as TextComponentString).text
            s = s.replace('&', '§')
            tileSign.signText[edtiLine] = TextComponentString(s)
        }
    }
}