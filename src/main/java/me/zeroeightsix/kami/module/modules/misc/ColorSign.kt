package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.GuiEvent
import me.zeroeightsix.kami.mixin.extension.editLine
import me.zeroeightsix.kami.mixin.extension.tileSign
import me.zeroeightsix.kami.module.Module
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.text.TextComponentString
import org.kamiblue.event.listener.listener
import java.io.IOException

object ColorSign : Module(
    name = "ColorSign",
    description = "Allows ingame coloring of text on signs",
    category = Category.MISC
) {
    init {
        listener<GuiEvent.Displayed> { event ->
            if (event.screen !is GuiEditSign) return@listener
            (event.screen as? GuiEditSign?)?.tileSign?.let { event.screen = KamiGuiEditSign(it) }
        }
    }

    private class KamiGuiEditSign(teSign: TileEntitySign) : GuiEditSign(teSign) {
        @Throws(IOException::class)
        override fun actionPerformed(button: GuiButton) {
            if (button.id == 0) {
                tileSign.signText[editLine] = TextComponentString(tileSign.signText[editLine].formattedText.replace("(§)(.)".toRegex(), "$1$1$2$2"))
            }
            super.actionPerformed(button)
        }

        @Throws(IOException::class)
        override fun keyTyped(typedChar: Char, keyCode: Int) {
            super.keyTyped(typedChar, keyCode)
            var s = (tileSign.signText[editLine] as TextComponentString).text
            s = s.replace('&', '§')
            tileSign.signText[editLine] = TextComponentString(s)
        }
    }
}