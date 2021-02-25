package org.kamiblue.client.module.modules.misc

import net.minecraft.entity.player.EnumPlayerModelParts
import org.kamiblue.client.event.events.RunGameLoopEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.threads.safeListener

internal object SkinFlicker : Module(
    name = "SkinFlicker",
    description = "Toggle your skin layers rapidly for a cool skin effect",
    category = Category.MISC
) {
    private val mode by setting("Mode", FlickerMode.HORIZONTAL)
    private val delay by setting("Delay", 10, 0..500, 5, description = "Skin layer toggle delay, in milliseconds")

    private enum class FlickerMode {
        HORIZONTAL, VERTICAL, RANDOM
    }

    private val timer = TickTimer()
    private var lastIndex = 0

    init {
        safeListener<RunGameLoopEvent.Tick> {
            if (!timer.tick(delay.toLong())) return@safeListener

            val part = when (mode) {
                FlickerMode.RANDOM -> EnumPlayerModelParts.values().random()
                FlickerMode.VERTICAL -> verticalParts[lastIndex]
                FlickerMode.HORIZONTAL -> horizontalParts[lastIndex]
            }
            mc.gameSettings.switchModelPartEnabled(part)
            lastIndex = (lastIndex + 1) % 7
        }

        onDisable {
            for (model in EnumPlayerModelParts.values()) {
                mc.gameSettings.setModelPartEnabled(model, true)
            }
        }
    }

    private val horizontalParts = arrayOf(
        EnumPlayerModelParts.LEFT_SLEEVE,
        EnumPlayerModelParts.LEFT_PANTS_LEG,
        EnumPlayerModelParts.JACKET,
        EnumPlayerModelParts.HAT,
        EnumPlayerModelParts.CAPE,
        EnumPlayerModelParts.RIGHT_PANTS_LEG,
        EnumPlayerModelParts.RIGHT_SLEEVE
    )

    private val verticalParts = arrayOf(
        EnumPlayerModelParts.HAT,
        EnumPlayerModelParts.JACKET,
        EnumPlayerModelParts.CAPE,
        EnumPlayerModelParts.LEFT_SLEEVE,
        EnumPlayerModelParts.RIGHT_SLEEVE,
        EnumPlayerModelParts.LEFT_PANTS_LEG,
        EnumPlayerModelParts.RIGHT_PANTS_LEG
    )
}