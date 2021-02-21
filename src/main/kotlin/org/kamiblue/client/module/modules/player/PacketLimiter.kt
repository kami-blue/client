package org.kamiblue.client.module.modules.player

import net.minecraft.network.play.client.CPacketPlayer
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.manager.managers.TimerManager.modifyTimer
import org.kamiblue.client.manager.managers.TimerManager.resetTimer
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.CircularArray
import org.kamiblue.event.listener.listener

internal object PacketLimiter : Module(
    name = "PacketLimiter",
    category = Category.PLAYER,
    description = "Adjust timer automatically to ensure not sending too many movement packets",
    modulePriority = 1000
) {
    private val maxPacketsLong by setting("Max Packets Long", 21.5f, 10.0f..40.0f, 0.25f,
        description = "Maximum packets per second in long term")
    private val maxPacketsShort by setting("Max Packets Short", 24.0f, 10.0f..40.0f, 0.25f,
        description = "Maximum packets per second in short term")

    private var lastPacketTime = -1L

    private val longPacketTime = CircularArray.create<Short>(100, 50)
    private var longPacketSpeed = 20.0f

    private val shortPacketTime = CircularArray.create<Short>(10, 50)
    private var shortPacketSpeed = 20.0f

    init {
        onDisable {
            resetTimer()
            reset()
        }

        listener<ConnectionEvent.Disconnect> {
            reset()
        }
    }

    private fun reset() {
        lastPacketTime = -1L

        longPacketTime.reset()
        shortPacketTime.reset()

        longPacketSpeed = 20.0f
        shortPacketSpeed = 20.0f
    }

    init {
        listener<PacketEvent.PostSend> {
            if (it.cancelled || it.packet !is CPacketPlayer) return@listener

            if (lastPacketTime != -1L) {
                val duration = (System.currentTimeMillis() - lastPacketTime).toShort()

                longPacketTime.add(duration)
                shortPacketTime.add(duration)

                longPacketSpeed = 1000.0f / shortPacketTime.average()
                shortPacketSpeed = 1000.0f / shortPacketTime.average()
            }

            lastPacketTime = System.currentTimeMillis()
        }

        listener<TickEvent.ClientTickEvent>(Int.MIN_VALUE) { event ->
            if (event.phase != TickEvent.Phase.END) return@listener

            if (maxPacketsLong <= maxPacketsShort) {
                limit(longPacketSpeed, maxPacketsLong) ?: limit(shortPacketSpeed, maxPacketsShort)
            } else {
                limit(longPacketSpeed, maxPacketsLong) ?: limit(shortPacketSpeed, maxPacketsShort)
            }?.let {
                modifyTimer(50.0f * it)
            }
        }
    }

    private fun limit(input: Float, max: Float) =
        if (input > max) input / max else null
}