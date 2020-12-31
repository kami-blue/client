package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import me.zeroeightsix.kami.mixin.extension.renderPosX
import me.zeroeightsix.kami.mixin.extension.renderPosY
import me.zeroeightsix.kami.mixin.extension.renderPosZ
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.graphics.KamiTessellator

class RenderWorldEvent : KamiEvent() {
    init {
        KamiTessellator.buffer.setTranslation(
            -Wrapper.minecraft.renderManager.renderPosX,
            -Wrapper.minecraft.renderManager.renderPosY,
            -Wrapper.minecraft.renderManager.renderPosZ
        )
    }
}