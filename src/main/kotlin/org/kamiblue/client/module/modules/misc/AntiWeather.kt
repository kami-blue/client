package org.kamiblue.client.module.modules.misc

import org.kamiblue.client.mixin.client.world.MixinWorld
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

/**
 * @see MixinWorld.getThunderStrengthHead
 * @see MixinWorld.getRainStrengthHead
 */
internal object AntiWeather : Module(
    name = "AntiWeather",
    description = "Removes rain and thunder from your world",
    category = Category.MISC
)
