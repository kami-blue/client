package org.kamiblue.client.module.modules.render

import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import net.minecraftforge.client.event.EntityViewRenderEvent
import org.kamiblue.event.listener.listener

internal object FogColor : Module(
    name = "FogColor",
    description = "Recolors render fog",
    category = Category.RENDER
) {
    private val r by setting("Red", 111, 0..255, 1)
    private val g by setting("Green", 166, 0..255, 1)
    private val b by setting("Blue", 222, 0..255, 1)

    init {
        listener<EntityViewRenderEvent.FogColors> {
            it.red = r.toFloat() / 255f
            it.green = g.toFloat() / 255f
            it.blue = b.toFloat() / 255f
        }
    }
}
