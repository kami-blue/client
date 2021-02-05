package org.kamiblue.client.event.events

import org.kamiblue.client.event.*
import net.minecraft.entity.Entity

class RenderEntityEvent(
    val entity: Entity,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val partialTicks: Float,
    override val phase: Phase
) : Event, ICancellable by Cancellable(), IMultiPhase<RenderEntityEvent>, ProfilerEvent {

    override val profilerName: String get() = "kbRenderEntity${phase.displayName}"

    override fun nextPhase(): RenderEntityEvent {
        throw UnsupportedOperationException()
    }
}