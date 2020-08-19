package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.math.Vec3d

/**
 * Created by 086 on 10/12/2017.
 * https://github.com/fr1kin/ForgeHax/blob/4697e629f7fa4f85faa66f9ac080573407a6d078/src/main/java/com/matt/forgehax/events/RenderEvent.java
 *
 * Updated by Xiaro on 18/08/20
 */
class RenderEvent(val tessellator: Tessellator, val renderPos: Vec3d) : KamiEvent() {
    val buffer: BufferBuilder get() = tessellator.buffer

    fun setTranslation(translation: Vec3d) {
        buffer.setTranslation(-translation.x, -translation.y, -translation.z)
    }

    fun resetTranslation() {
        setTranslation(renderPos)
    }
}