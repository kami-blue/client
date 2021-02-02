package org.kamiblue.client.event.events

import org.kamiblue.client.event.Event
import org.kamiblue.client.event.ProfilerEvent
import org.kamiblue.client.mixin.extension.renderPosX
import org.kamiblue.client.mixin.extension.renderPosY
import org.kamiblue.client.mixin.extension.renderPosZ
import org.kamiblue.client.util.Wrapper
import org.kamiblue.client.util.graphics.KamiTessellator

class RenderWorldEvent : Event, ProfilerEvent {
    override val profilerName: String = "kbRender3D"

    init {
        KamiTessellator.buffer.setTranslation(
            -Wrapper.minecraft.renderManager.renderPosX,
            -Wrapper.minecraft.renderManager.renderPosY,
            -Wrapper.minecraft.renderManager.renderPosZ
        )
    }
}