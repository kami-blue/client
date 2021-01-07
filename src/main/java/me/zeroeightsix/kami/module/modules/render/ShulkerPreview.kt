package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.mixin.client.gui.MixinGuiScreen
import me.zeroeightsix.kami.module.Module

/**
 * @see MixinGuiScreen.renderToolTip
 */
object ShulkerPreview : Module(
    name = "ShulkerPreview",
    category = Category.RENDER,
    description = "Previews shulkers in the game GUI"
)
