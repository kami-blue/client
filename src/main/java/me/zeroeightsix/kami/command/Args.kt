package me.zeroeightsix.kami.command

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import org.kamiblue.command.AbstractArg


class ModuleArg(
    override val name: String
) : AbstractArg<Module>() {

    override suspend fun convertToType(string: String?): Module? {
        return ModuleManager.getModuleOrNull(string)
    }

}