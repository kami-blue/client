package org.kamiblue.client.gui.hudgui.elements.world

import net.minecraft.util.math.Vec3d
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.util.graphics.font.TextComponent
import org.kamiblue.client.util.math.VectorUtils.times

internal object Coordinate : LabelHud(
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

    private val netherToOverworld = Vec3d(8.0, 1.0, 8.0)
    private val overworldToNether = Vec3d(0.125, 1.0, 0.125)

    override fun SafeClientEvent.updateText() {
        val entity = mc.renderViewEntity ?: player

        displayText.add("XYZ", secondaryColor)
        displayText.addLine(getFormattedCoords(entity.positionVector))

        if (showNetherOverworld) {
            when (entity.dimension) {
                -1 -> { // Nether
                    displayText.add("Overworld", secondaryColor)
                    displayText.addLine(getFormattedCoords(entity.positionVector * netherToOverworld))
                }
                0 -> { // Overworld
                    displayText.add("Nether", secondaryColor)
                    displayText.addLine(getFormattedCoords(entity.positionVector * overworldToNether))
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