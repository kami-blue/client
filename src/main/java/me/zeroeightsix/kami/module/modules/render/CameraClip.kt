package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.mixin.client.render.MixinEntityRenderer
import me.zeroeightsix.kami.module.Module

/**
 * @see MixinEntityRenderer.rayTraceBlocks
 */
object CameraClip : Module(
    category = Category.RENDER,
    showOnArray = false
)
