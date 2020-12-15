package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.ActiveModules
import java.awt.Color

object ActiveModulesCommand : ClientCommand(
    name = "activemodules",
    description = "Change activemodules category colors"
) {
    init {
        enum<Module.Category>("category") { category ->
            int("r") { r ->
                int("g") { g ->
                    int("b") { b ->
                        execute {
                            ActiveModules.setColor(
                                category.value,
                                Color(r.value, b.value, b.value)
                            )
                        }
                    }
                }
            }
        }
    }
}