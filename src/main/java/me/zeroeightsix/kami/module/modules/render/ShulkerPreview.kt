package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
import me.zeroeightsix.kami.mixin.client.gui.MixinGuiScreen
import me.zeroeightsix.kami.module.Module

/**
 * @see MixinGuiScreen.renderToolTip
 */
object ShulkerPreview : Module(
    name = KamiLang.get("module.modules.render.ShulkerPreview.Shulkerpreview"),
    category = Category.RENDER,
    description = KamiLang.get("module.modules.render.ShulkerPreview.PreviewsShulkersInThe")
)
