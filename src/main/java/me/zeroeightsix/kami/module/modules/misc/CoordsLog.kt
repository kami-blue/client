package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.Waypoint
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.math.BlockPos

@Module.Info(
        name = "CoordsLog",
        description = "Automatically logs your coords, based on actions",
        category = Module.Category.MISC, showOnArray = Module.ShowOnArray.ON
)
class CoordsLog : Module() {
    private val forceLogOnDeath = register(Settings.b("SaveDeathCoords", true))
    private val deathInChat = register(Settings.b("LogInChat", true))
    private val autoLog = register(Settings.b("Delay", false))
    private val delay = register(Settings.doubleBuilder("DelayT").withMinimum(1.0).withValue(15.0).withMaximum(60.0).build())
    private val checkDuplicates = register(Settings.b("AvoidDuplicates", true))

    private var previousCoord: String? = null
    private var playerIsDead = false
    private var startTime: Long = 0

    override fun onUpdate() {
        if (mc.player == null) return
        if (autoLog.value) {
            timeout()
        }

        if (0 < mc.player.health && playerIsDead) {
            playerIsDead = false
        }

        if (!playerIsDead && 0 >= mc.player.health && forceLogOnDeath.value) {
            val deathPoint = logCoordinates("Death - " + InfoCalculator.getServerType())
            if (deathInChat.value) {
                MessageSendHelper.sendChatMessage("You died at ${deathPoint.x}, ${deathPoint.y}, ${deathPoint.z}")
            }
            playerIsDead = true
        }
    }

    private fun timeout() {
        if (startTime == 0L) startTime = System.currentTimeMillis()

        if (startTime + delay.value * 1000 <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            startTime = System.currentTimeMillis()
            val pos = Waypoint.getCurrentCoord()
            val currentCoord = pos.toString()

            if (checkDuplicates.value) {
                if (currentCoord != previousCoord) {
                    logCoordinates("autoLogger")
                    previousCoord = currentCoord
                }
            } else {
                logCoordinates("autoLogger")
                previousCoord = currentCoord
            }
        }
    }

    private fun logCoordinates(name: String): BlockPos {
        return Waypoint.writePlayerCoords(name)
    }

    public override fun onDisable() {
        startTime = 0
    }
}