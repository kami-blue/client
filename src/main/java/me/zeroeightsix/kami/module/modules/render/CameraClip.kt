package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.mixin.client.render.MixinEntityRenderer
import me.zeroeightsix.kami.module.Module

/**
 * @see MixinEntityRenderer.rayTraceBlocks
 */
object CameraClip : Module(
    name = "CameraClip",
    category = Category.RENDER,
    description = "Allows your 3rd person camera to pass through blocks",
    showOnArray = false
)
