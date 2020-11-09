package me.zeroeightsix.kami.gui.hudgui.elements.player

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import net.minecraft.util.EnumFacing

@HudElement.Info(
        category = HudElement.Category.PLAYER,
        description = "Direction of player facing to"
)
object Direction : LabelHud("Direction") {

    override fun updateText() {
        var facing = "Unknown"
        var axis = "Unknown"
        mc.player?.horizontalFacing?.let {
            facing = it.name2.capitalize()
            axis = it.axisDirection.sign + it.axis.name
        }
        displayText.add(facing, secondaryColor.value)
        displayText.add("($axis)", primaryColor.value)
    }

    private val EnumFacing.AxisDirection.sign get() = if (this == EnumFacing.AxisDirection.POSITIVE) '+' else '-'

}