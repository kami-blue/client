package org.kamiblue.client.module.modules.render

import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import net.minecraftforge.client.event.EntityViewRenderEvent
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.event.listener.listener

internal object FogColor : Module(
    name = "FogColor",
    description = "Recolors render fog",
    category = Category.RENDER
) {

    private val color by setting("Color", ColorHolder(111, 166, 222), false)

    init {
        listener<EntityViewRenderEvent.FogColors> {
            it.red = color.r.toFloat() / 255f
            it.green = color.g.toFloat() / 255f
            it.blue = color.b.toFloat() / 255f
        }
    }
}
