package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

object ExtraTab : Module(
    category = Category.RENDER
) {
    val tabSize = setting("MaxPlayers", 265, 80..400, 5)

    fun <E> subList(list: List<E>, fromIndex: Int, toIndex: Int): List<E> {
        return list.subList(fromIndex, if (isEnabled) tabSize.value.coerceAtMost(list.size) else toIndex)
    }
}