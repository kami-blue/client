package me.zeroeightsix.kami.gui.hudgui.elements.world

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.graphics.font.TextComponent
import net.minecraft.util.math.Vec3d
import org.kamiblue.commons.utils.MathUtils

object Coordinate : LabelHud(
    name = "Coordinate",
    category = Category.WORLD,
    description = "Display the current coordinate"
) {

    private val showX = setting("ShowX", true)
    private val showY = setting("ShowY", true)
    private val showZ = setting("ShowZ", true)
    private val showNetherOverworld = setting("ShowNether/Overworld", true)
    private val decimalPlaces = setting("DecimalPlaces", 1, 0..4, 1)

    override fun SafeClientEvent.updateText() {
        val entity = mc.renderViewEntity ?: mc.player ?: return

        displayText.add("Current", secondaryColor)
        displayText.addLine(getFormattedCoords(entity.positionVector))

        if (showNetherOverworld.value) {
            when (entity.dimension) {
                -1 -> { // Nether
                    displayText.add("Overworld", secondaryColor)
                    displayText.addLine(getFormattedCoords(entity.positionVector.scale(8.0)))
                }
                0 -> { // Overworld
                    displayText.add("Nether", secondaryColor)
                    displayText.addLine(getFormattedCoords(entity.positionVector.scale(0.125)))
                }
            }
        }
    }

    private fun getFormattedCoords(pos: Vec3d): TextComponent.TextElement {
        val x = roundOrInt(pos.x)
        val y = roundOrInt(pos.y)
        val z = roundOrInt(pos.z)
        return StringBuilder().run {
            if (showX.value) append(x)
            if (showY.value) appendWithComma(y.toString())
            if (showZ.value) appendWithComma(z.toString())
            TextComponent.TextElement(toString(), primaryColor)
        }
    }

    private fun roundOrInt(double: Double): Number =
        if (decimalPlaces.value != 0) MathUtils.round(double, decimalPlaces.value)
        else double.toInt()

    private fun StringBuilder.appendWithComma(string: String) = append(if (length > 0) ", $string" else string)

}