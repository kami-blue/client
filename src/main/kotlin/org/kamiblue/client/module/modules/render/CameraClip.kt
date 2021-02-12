package org.kamiblue.client.module.modules.render

import org.kamiblue.client.mixin.client.render.MixinEntityRenderer
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

/**
 * @see MixinEntityRenderer.rayTraceBlocks
 */
internal object CameraClip : Module(
    name = "CameraClip",
    category = Category.RENDER,
    description = "Allows your 3rd person camera to pass through blocks",
    showOnArray = false
)
