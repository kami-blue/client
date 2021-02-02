package me.zeroeightsix.kami.gui.hudgui.elements.world

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.graphics.font.TextComponent
import net.minecraft.util.math.Vec3d

object Coordinate : LabelHud(
    name = "Coordinate",
    category = Category.WORLD,
    description = "Display the current coordinate"
) {

    private val showX by setting("Show X", true)
    private val showY by setting("Show Y", true)
    private val showZ by setting("Show Z", true)
    private val showNetherOverworld by setting("Show Nether/Overworld", true)
    private val decimalPlaces by setting("Decimal Places", 1, 0..4, 1)
    private val thousandsSeparator by setting("Thousands Separator", false)

    override fun SafeClientEvent.updateText() {
        val entity = mc.renderViewEntity ?: player

        displayText.add("Current", secondaryColor)
        displayText.addLine(getFormattedCoords(entity.positionVector))

        if (showNetherOverworld) {
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
            if (showX) append(x)
            if (showY) appendWithComma(y)
            if (showZ) appendWithComma(z)
            TextComponent.TextElement(toString(), primaryColor)
        }
    }

    private fun roundOrInt(input: Double): String {
        val separatorFormat = if (thousandsSeparator) "," else ""

        return "%$separatorFormat.${decimalPlaces}f".format(input)
    }

    private fun StringBuilder.appendWithComma(string: String) = append(if (length > 0) ", $string" else string)

}