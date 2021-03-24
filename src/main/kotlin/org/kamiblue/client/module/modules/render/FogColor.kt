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

    private val color = setting("Color", ColorHolder(111, 166, 222), false)

    init {
        listener<EntityViewRenderEvent.FogColors> {
            it.red = color.value.r.toFloat() / 255f
            it.green = color.value.g.toFloat() / 255f
            it.blue = color.value.b.toFloat() / 255f
        }
    }
}
