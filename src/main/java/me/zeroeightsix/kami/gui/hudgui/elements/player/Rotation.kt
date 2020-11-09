package me.zeroeightsix.kami.gui.hudgui.elements.player

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.util.math.MathUtils
import me.zeroeightsix.kami.util.math.RotationUtils

@HudElement.Info(
        category = HudElement.Category.PLAYER,
        description = "Player rotation"
)
object Rotation : LabelHud("Rotation") {

    override fun updateText() {
        val yaw = MathUtils.round(RotationUtils.normalizeAngle(mc.player?.rotationYaw?: 0.0f), 1)
        val pitch = MathUtils.round(mc.player?.rotationPitch?: 0.0f, 1)
        displayText.add("Yaw", secondaryColor.value)
        displayText.add(yaw.toString(), primaryColor.value)
        displayText.add("Pitch", secondaryColor.value)
        displayText.add(pitch.toString(), primaryColor.value)
    }

}