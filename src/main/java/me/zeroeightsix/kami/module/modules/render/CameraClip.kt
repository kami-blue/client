package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
import me.zeroeightsix.kami.mixin.client.render.MixinEntityRenderer
import me.zeroeightsix.kami.module.Module

/**
 * @see MixinEntityRenderer.rayTraceBlocks
 */
object CameraClip : Module(
    name = KamiLang.get("module.modules.render.CameraClip.Cameraclip"),
    category = Category.RENDER,
    description = KamiLang.get("module.modules.render.CameraClip.AllowsYour3rdPerson"),
    showOnArray = false
)
