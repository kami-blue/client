package org.kamiblue.client.module.modules.player

import net.minecraft.network.play.client.CPacketPlayer
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.manager.managers.TimerManager.modifyTimer
import org.kamiblue.client.manager.managers.TimerManager.resetTimer
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.event.listener.listener
import kotlin.math.min

internal object PacketLimiter : Module(
    name = "PacketLimiter",
    category = Category.PLAYER,
    description = "Adjust timer automatically to ensure not sending too many movement packets",
    modulePriority = 1000
) {
    private val maxPacketsLong by setting("Max Packets Long", 22.0f, 10.0f..40.0f, 0.25f,
        description = "Maximum packets per second in long term")
    private val longTermTicks by setting("Long Term Ticks", 100, 20..250, 5)
    private val maxPacketsShort by setting("Max Packets Short", 25.0f, 10.0f..40.0f, 0.25f,
        description = "Maximum packets per second in short term")
    private val shortTermTicks by setting("Short Term Ticks", 20, 5..50, 1)
    private val minTimer by setting("Min Timer", 0.6f, 0.1f..1.0f, 0.01f)

    private var lastPacketTime = -1L

    private val longPacketTime = ArrayDeque<Short>( 100)
    private val shortPacketTime = ArrayDeque<Short>(20)

    private var longPacketSpeed = 20.0f
    private var shortPacketSpeed = 20.0f
    private var prevTimerSpeed = 1.0f

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

        synchronized(PacketLimiter) {
            longPacketTime.clear()
            shortPacketTime.clear()
        }

        longPacketSpeed = 20.0f
        shortPacketSpeed = 20.0f
    }

    init {
        listener<PacketEvent.PostSend> {
            if (it.cancelled || it.packet !is CPacketPlayer) return@listener

            if (lastPacketTime != -1L) {
                val duration = (System.currentTimeMillis() - lastPacketTime).toShort()

                synchronized(PacketLimiter) {
                    longPacketTime.addAndTrim(duration, longTermTicks)
                    shortPacketTime.addAndTrim(duration, shortTermTicks)

                    longPacketSpeed = (1000.0 / shortPacketTime.average()).toFloat()
                    shortPacketSpeed = (1000.0 / shortPacketTime.average()).toFloat()
                }
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
                prevTimerSpeed = min(it, prevTimerSpeed).coerceAtLeast(minTimer)
                modifyTimer(50.0f / prevTimerSpeed)
            } ?: run {
                prevTimerSpeed = 1.0f
            }
        }
    }

    private fun ArrayDeque<Short>.addAndTrim(value: Short, max: Int) {
        add(value)
        val validMax = max.coerceAtLeast(0)
        while (this.size > validMax){
            this.removeFirst()
        }
    }

    private fun limit(input: Float, max: Float) =
        if (input > max) max / input else null
}