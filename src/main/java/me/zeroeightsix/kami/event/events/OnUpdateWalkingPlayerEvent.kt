package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.util.math.Vec3d

class OnUpdateWalkingPlayerEvent(
        var moving: Boolean,
        var rotating: Boolean,
        var sprinting: Boolean,
        var sneaking: Boolean,
        var onGround: Boolean,
        var pos: Vec3d,
        var rotation: Vec2f
) : KamiEvent()