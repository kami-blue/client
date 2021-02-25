package org.kamiblue.client.module.modules.client

import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

internal object Tooltips : Module(
    name = "Tooltips",
    description = "Displays handy module descriptions in the GUI",
    category = Category.CLIENT,
    showOnArray = false,
    enabledByDefault = true
)
