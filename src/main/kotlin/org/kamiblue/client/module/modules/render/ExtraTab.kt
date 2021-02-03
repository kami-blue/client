package org.kamiblue.client.module.modules.render

import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

internal object ExtraTab : Module(
    name = "ExtraTab",
    description = "Expands the player tab menu",
    category = Category.RENDER
) {
    private val tabSize = setting("Max Players", 265, 80..400, 5)

    fun <E> subList(list: List<E>, fromIndex: Int, toIndex: Int): List<E> {
        return list.subList(fromIndex, if (isEnabled) tabSize.value.coerceAtMost(list.size) else toIndex)
    }
}