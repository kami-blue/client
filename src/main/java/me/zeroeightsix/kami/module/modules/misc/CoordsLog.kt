package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.math.BlockPos

@Module.Info(
        name = "CoordsLog",
        description = "Automatically logs your coords, based on actions",
        category = Module.Category.MISC
)
object CoordsLog : Module() {
    private val saveOndeath = register(Settings.b("SaveOnDeath", true))
    private val autoLog = register(Settings.b("AutoLog", false))
    private val delay = register(Settings.integerBuilder("Delay").withValue(15).withRange(1, 60).withStep(1))

    private var previousCoord: String? = null
    private var savedDeath = false
    private var timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    init {
        listener<SafeTickEvent> {
            if (autoLog.value) {
                timeout()
            }

            if (saveOndeath.value) {
                savedDeath = if (!savedDeath && (mc.player.isDead || mc.player.health <= 0.0f)) {
                    val deathPoint = logCoordinates("Death - " + InfoCalculator.getServerType())
                    MessageSendHelper.sendChatMessage("You died at ${deathPoint.x}, ${deathPoint.y}, ${deathPoint.z}")
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun timeout() {
        if (timer.tick(delay.value.toLong())) {
            val currentCoord = mc.player.positionVector.toBlockPos().asString()

            if (currentCoord != previousCoord) {
                logCoordinates("autoLogger")
                previousCoord = currentCoord
            }
        }
    }

    private fun logCoordinates(name: String): BlockPos {
        return WaypointManager.add(name).pos
    }
}