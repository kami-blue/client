package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

@Module.Info(
    name = "ItemModel",
    description = "Modify hand item rendering in first person",
    category = Module.Category.RENDER
)
object ItemModel : Module() {
    val posX by setting("PosX", 0.0f, -5.0f..5.0f, 0.025f)
    val posY by setting("PosY", 0.0f, -5.0f..5.0f, 0.025f)
    val posZ by setting("PosZ", 0.0f, -5.0f..5.0f, 0.025f)
    val rotateX by setting("RotateX", 0.0f, -180.0f..180.0f, 1.0f)
    val rotateY by setting("RotateY", 0.0f, -180.0f..180.0f, 1.0f)
    val rotateZ by setting("RotateZ", 0.0f, -180.0f..180.0f, 1.0f)
    val scale by setting("Scale", 1.0f, 0.1f..3.0f, 0.025f)
    val modifyHand by setting("ModifyHand", false)
}