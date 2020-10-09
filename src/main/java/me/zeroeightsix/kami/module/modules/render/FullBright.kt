package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import kotlin.math.max
import kotlin.math.min

@Module.Info(
        name = "FullBright",
        description = "Makes everything brighter!",
        category = Module.Category.RENDER,
        alwaysListening = true
)
object FullBright : Module() {
    private val gamma = register(Settings.floatBuilder("Gamma").withValue(12f).withRange(1f, 15f).build())
    private val transitionLength = register(Settings.floatBuilder("TransitionLength").withValue(3f).withRange(0f, 10f).build())
    private val oldValue = register(Settings.floatBuilder("OldValue").withValue(1f).withRange(0f, 1f).withVisibility { false }.build())

    private var gammaSetting: Float
        get() = mc.gameSettings.gammaSetting
        set(gammaIn) {
            mc.gameSettings.gammaSetting = gammaIn
        }

    override fun onEnable() {
        oldValue.value = mc.gameSettings.gammaSetting
    }

    override fun onUpdate(event: SafeTickEvent) {
        when {
            isEnabled -> {
                transition(gamma.value)
                alwaysListening = true
            }

            isDisabled && gammaSetting != oldValue.value -> {
                transition(oldValue.value)
            }

            else -> {
                alwaysListening = false
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
        if (transitionLength.value == 0f) return 15f
        return (1f / transitionLength.value / 20f) * (gamma.value - oldValue.value)
    }
}