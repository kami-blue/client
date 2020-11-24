package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.text.MessageSendHelper

@Module.Info(
        name = "CoordsLog",
        description = "Automatically logs your coords, based on actions",
        category = Module.Category.MISC
)
object CoordsLog : Module() {
    private val saveOnDeath = setting("SaveOnDeath", true)
    private val autoLog = setting("AutoLog", false)
    private val delay = setting("Delay", 15, 1..60, 1)

    private var previousCoord: String? = null
    private var savedDeath = false
    private var timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    init {
        listener<SafeTickEvent> {
            if (autoLog.value) {
                if (timer.tick(delay.value.toLong())) {
                    val currentCoord = mc.player.positionVector.toBlockPos().asString()

                    if (currentCoord != previousCoord) {
                        WaypointManager.add("autoLogger")
                        previousCoord = currentCoord
                    }
                }
            }

            if (saveOnDeath.value) {
                savedDeath = if (mc.player.isDead || mc.player.health <= 0.0f) {
                    if (!savedDeath) {
                        val deathPoint = WaypointManager.add("Death - " + InfoCalculator.getServerType()).pos
                        MessageSendHelper.sendChatMessage("You died at ${deathPoint.x}, ${deathPoint.y}, ${deathPoint.z}")
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

}