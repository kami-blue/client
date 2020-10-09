package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.client.renderer.Tessellator

/**
 * Created by 086 on 10/12/2017.
 * https://github.com/fr1kin/ForgeHax/blob/4697e629f7fa4f85faa66f9ac080573407a6d078/src/main/java/com/matt/forgehax/events/RenderEvent.java
 *
 * Updated by Xiaro on 18/08/20
 */
class RenderWorldEvent(val tessellator: Tessellator, override val partialTicks: Float) : KamiEvent() {
    val buffer = tessellator.buffer

    fun setupTranslation() {
        buffer.setTranslation(-mc.renderManager.renderPosX, -mc.renderManager.renderPosY, -mc.renderManager.renderPosZ)
    }
}