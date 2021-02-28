package org.kamiblue.client.event.events

import net.minecraft.entity.Entity
import org.kamiblue.client.event.*

sealed class RenderEntityEvent(
    val entity: Entity,
    override val phase: Phase
) : Event, ICancellable by Cancellable(), IMultiPhase<RenderEntityEvent>, ProfilerEvent {

    override val profilerName: String get() = "kbRenderEntity${phase.displayName}"

    override fun nextPhase(): RenderEntityEvent {
        throw UnsupportedOperationException()
    }

    class All(entity: Entity, phase: Phase) : RenderEntityEvent(entity, phase)

    class Model(entity: Entity, phase: Phase) : RenderEntityEvent(entity, phase)

    companion object {
        @JvmStatic
        var renderingEntities = false
    }
}