package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Cancellable
import me.zeroeightsix.kami.event.Event
import me.zeroeightsix.kami.event.IMultiPhase
import me.zeroeightsix.kami.event.Phase
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.kamiblue.commons.extension.next

class OnUpdateWalkingPlayerEvent private constructor(
    var moving: Boolean,
    var rotating: Boolean,
    var sprinting: Boolean,
    var sneaking: Boolean,
    var onGround: Boolean,
    var pos: Vec3d,
    var rotation: Vec2f,
    override val phase: Phase
) : Event, IMultiPhase<OnUpdateWalkingPlayerEvent>, Cancellable() {

    constructor(moving: Boolean, rotating: Boolean, sprinting: Boolean, sneaking: Boolean, onGround: Boolean, pos: Vec3d, rotation: Vec2f)
        : this(moving, rotating, sprinting, sneaking, onGround, pos, rotation, Phase.PRE)

    override fun nextPhase(): OnUpdateWalkingPlayerEvent {
        return OnUpdateWalkingPlayerEvent(moving, rotating, sprinting, sneaking, onGround, pos, rotation, phase.next())
    }

}