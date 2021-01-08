package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
import me.zeroeightsix.kami.module.Module

object NoHurtCam : Module(
    name = KamiLang.get("module.modules.render.NoHurtCam.Nohurtcam"),
    category = Category.RENDER,
    description = KamiLang.get("module.modules.render.NoHurtCam.DisablesThe'hurt'Camera")
)
