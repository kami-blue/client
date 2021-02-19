package org.kamiblue.client.module.modules.render

import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.threads.safeListener
import kotlin.math.max
import kotlin.math.min

internal object FullBright : Module(
    name = "FullBright",
    description = "Makes everything brighter!",
    category = Category.RENDER,
    alwaysListening = true
) {
    private val gamma by setting("Gamma", 12.0f, 5.0f..15.0f, 0.5f)
    private val transitionLength by setting("Transition Length", 3.0f, 0.0f..10.0f, 0.5f)
    private var oldValue by setting("Old Value", 1.0f, 0.0f..1.0f, 0.1f, { false })

    private var gammaSetting: Float
        get() = mc.gameSettings.gammaSetting
        set(gammaIn) {
            mc.gameSettings.gammaSetting = gammaIn
        }
    private val disableTimer = TickTimer()

    init {
        onEnable {
            oldValue = mc.gameSettings.gammaSetting
        }

        onDisable {
            disableTimer.reset()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@safeListener
            when {
                isEnabled -> {
                    transition(gamma)
                    alwaysListening = true
                }

                isDisabled && gammaSetting != oldValue
                    && !disableTimer.tick((transitionLength * 1000.0f).toLong(), false) -> {
                    transition(oldValue)
                }

                else -> {
                    alwaysListening = false
                    disable()
                }
            }
        }
    }

    private fun transition(target: Float) {
        gammaSetting = when {
            gammaSetting !in 0f..15f -> target

            gammaSetting == target -> return

            gammaSetting < target -> min(gammaSetting + getTransitionAmount(), target)

            else -> max(gammaSetting - getTransitionAmount(), target)
        }
    }

    private fun getTransitionAmount(): Float {
        if (transitionLength == 0f) return 15f
        return (1f / transitionLength / 20f) * (gamma - oldValue)
    }
}