package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.network.play.server.SPacketTimeUpdate
import net.minecraft.util.math.MathHelper
import java.util.*

object LagCompensator : EventListener {
    private val tickRates = FloatArray(20)
    private var nextIndex = 0
    private var timeLastTimeUpdate: Long = 0

    val tickRate: Float
        get() {
            var numTicks = 0.0f
            var sumTickRates = 0.0f
            for (tickRate in tickRates) {
                if (tickRate > 0.0f) {
                    sumTickRates += tickRate
                    numTicks += 1.0f
                }
            }
            val calcTickRate = MathHelper.clamp(sumTickRates / numTicks, 0.0f, 20.0f)
            return if (calcTickRate == 0.0f) 20.0f else calcTickRate
        }

    val adjustTicks: Float get() = tickRate - 20f

    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketTimeUpdate) return@listener
            if (timeLastTimeUpdate != -1L) {
                val timeElapsed = (System.currentTimeMillis() - timeLastTimeUpdate).toFloat() / 1000.0f
                tickRates[nextIndex % tickRates.size] = MathHelper.clamp(20.0f / timeElapsed, 0.0f, 20.0f)
                nextIndex += 1
            }
            timeLastTimeUpdate = System.currentTimeMillis()
        }
    }

    fun reset() {
        nextIndex = 0
        timeLastTimeUpdate = -1L
        Arrays.fill(tickRates, 0.0f)
    }

    init {
        KamiEventBus.subscribe(this)
        reset()
    }
}