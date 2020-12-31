package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TickTimer
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import kotlin.math.max
import kotlin.math.min

@Module.Info(
        name = "FullBright",
        description = "Makes everything brighter!",
        category = Module.Category.RENDER,
        alwaysListening = true
)
object FullBright : Module() {
    private val gamma = register(Settings.floatBuilder("Gamma").withValue(12.0f).withRange(1.0f, 15f))
    private val transitionLength = register(Settings.floatBuilder("TransitionLength").withValue(3.0f).withRange(0.0f, 10.0f))
    private val oldValue = register(Settings.floatBuilder("OldValue").withValue(1.0f).withRange(0.0f, 1.0f).withVisibility { false })

    private var gammaSetting: Float
        get() = mc.gameSettings.gammaSetting
        set(gammaIn) {
            mc.gameSettings.gammaSetting = gammaIn
        }
    private var disableTimer = TickTimer()

    override fun onEnable() {
        oldValue.value = mc.gameSettings.gammaSetting
    }

    override fun onDisable() {
        disableTimer.reset()
    }

    init {
        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@listener
            when {
                isEnabled -> {
                    transition(gamma.value)
                    alwaysListening = true
                }

                isDisabled && gammaSetting != oldValue.value
                        && !disableTimer.tick((transitionLength.value * 1000.0f).toLong(), false) -> {
                    transition(oldValue.value)
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
        if (transitionLength.value == 0f) return 15f
        return (1f / transitionLength.value / 20f) * (gamma.value - oldValue.value)
    }
}