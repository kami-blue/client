package org.kamiblue.client.module.modules.misc

import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.manager.managers.WaypointManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.InfoCalculator
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.math.CoordinateConverter.asString
import org.kamiblue.client.util.math.VectorUtils.toBlockPos
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.safeListener

internal object CoordsLog : Module(
    name = "CoordsLog",
    description = "Automatically logs your coords, based on actions",
    category = Category.MISC
) {
    private val saveOnDeath = setting("Save On Death", true)
    private val autoLog = setting("Automatically Log", false)
    private val delay = setting("Delay", 15, 1..60, 1)

    private var previousCoord: String? = null
    private var savedDeath = false
    private var timer = TickTimer(TimeUnit.SECONDS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (autoLog.value && timer.tick(delay.value.toLong())) {
                val currentCoord = player.positionVector.toBlockPos().asString()

                if (currentCoord != previousCoord) {
                    WaypointManager.add("autoLogger")
                    previousCoord = currentCoord
                }
            }

            if (saveOnDeath.value) {
                savedDeath = if (player.isDead || player.health <= 0.0f) {
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
