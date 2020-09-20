package me.zeroeightsix.kami.event

import me.zero.alpine.type.Cancellable
import me.zeroeightsix.kami.util.Wrapper.minecraft

/**
 * Created by 086 on 16/11/2017.
 * Updated by Xiaro on 18/08/20
 */
open class KamiEvent : Cancellable() {
    var era: Era = Era.PRE
    open val partialTicks: Float = minecraft.renderPartialTicks

    enum class Era {
        PRE, PERI, POST
    }
}