package me.zeroeightsix.kami.util.text

import me.zeroeightsix.kami.module.Module

object SortHelper {
    val lengthThenNatural = compareBy<Module> { it.name.value }

}