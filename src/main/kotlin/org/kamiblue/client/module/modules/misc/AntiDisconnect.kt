package org.kamiblue.client.module.modules.misc

import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

internal object AntiDisconnect : Module(
    name = "AntiDisconnect",
    description = "Are you sure you want to disconnect?",
    category = Category.MISC
) {
    val presses = setting("Button Presses", 3, 1..20, 1)
}