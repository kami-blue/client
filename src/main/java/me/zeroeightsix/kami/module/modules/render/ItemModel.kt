package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

object ItemModel : Module(
    name = KamiLang.get("module.modules.render.ItemModel.Itemmodel"),
    description = KamiLang.get("module.modules.render.ItemModel.ModifyHandItemRendering"),
    category = Category.RENDER
) {
    val posX by setting(KamiLang.get("module.modules.render.ItemModel.Posx"), 0.0f, -5.0f..5.0f, 0.025f)
    val posY by setting(KamiLang.get("module.modules.render.ItemModel.Posy"), 0.0f, -5.0f..5.0f, 0.025f)
    val posZ by setting(KamiLang.get("module.modules.render.ItemModel.Posz"), 0.0f, -5.0f..5.0f, 0.025f)
    val rotateX by setting(KamiLang.get("module.modules.render.ItemModel.Rotatex"), 0.0f, -180.0f..180.0f, 1.0f)
    val rotateY by setting(KamiLang.get("module.modules.render.ItemModel.Rotatey"), 0.0f, -180.0f..180.0f, 1.0f)
    val rotateZ by setting(KamiLang.get("module.modules.render.ItemModel.Rotatez"), 0.0f, -180.0f..180.0f, 1.0f)
    val scale by setting(KamiLang.get("module.modules.render.ItemModel.Scale"), 1.0f, 0.1f..3.0f, 0.025f)
    val modifyHand by setting(KamiLang.get("module.modules.render.ItemModel.Modifyhand"), false)
}