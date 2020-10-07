package me.zeroeightsix.kami.event

import me.zero.alpine.type.Cancellable
import me.zeroeightsix.kami.util.Wrapper

/**
 * Created by 086 on 16/11/2017.
 * Updated by Xiaro on 18/08/20
 */
open class KamiEvent : Cancellable() {
    protected val mc = Wrapper.minecraft
    open val partialTicks: Float = mc.renderPartialTicks
    var era: Era = Era.PRE

    enum class Era {
        PRE, PERI, POST
    }
}