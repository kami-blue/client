package me.zeroeightsix.kami.mixin.client.accessor

import net.minecraft.util.Timer

var Timer.tickLength: Float
    get() = (this as AccessorTimer).tickLength
    set(value) {
        (this as AccessorTimer).tickLength = value
    }