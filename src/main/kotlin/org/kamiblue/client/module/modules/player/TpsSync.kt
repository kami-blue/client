package org.kamiblue.client.module.modules.player

import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

internal object TpsSync : Module(
    name = "TpsSync",
    description = "Synchronizes block states with the server TPS",
    category = Category.PLAYER
)
