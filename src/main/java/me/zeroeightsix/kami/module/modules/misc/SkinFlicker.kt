package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.entity.player.EnumPlayerModelParts

internal object SkinFlicker : Module(
    name = "SkinFlicker",
    description = "Toggle your skin layers rapidly for a cool skin effect",
    category = Category.MISC
) {
    private val mode by setting("Mode", FlickerMode.HORIZONTAL)
    private val delay by setting("Delay(ms)", 10, 0..500, 10)

    private enum class FlickerMode {
        HORIZONTAL, VERTICAL, RANDOM
    }

    private val timer = TickTimer()
    private var lastIndex = 0

    init {
        safeListener<RenderEvent> {
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