package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.translation.TranslationKey
import me.zeroeightsix.kami.util.translation.TranslationKeyBlank

class Button(
    name: TranslationKey,
    private val action: (Button) -> Unit,
    descriptionIn: TranslationKey = TranslationKeyBlank()
) : BooleanSlider(name, 0.0, descriptionIn) {
    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        value = 1.0
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (prevState != MouseState.DRAG) {
            value = 0.0
            action(this)
        }
    }
}