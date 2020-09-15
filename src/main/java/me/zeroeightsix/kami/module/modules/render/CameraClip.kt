package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module

/**
 * [me.zeroeightsix.kami.mixin.client.MixinEntityRenderer.rayTraceBlocks]
 */
@Module.Info(
        name = "CameraClip",
        category = Module.Category.RENDER,
        description = "Allows your 3rd person camera to pass through blocks",
        showOnArray = Module.ShowOnArray.OFF
)
object CameraClip : Module()