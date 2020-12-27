package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.ActiveModules

// TODO: Remove when new GUI is merged (use proper color settings)
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
                            ActiveModules.setColor(category.value, r.value.coerceIn(0, 255), g.value.coerceIn(0, 255), b.value.coerceIn(0, 255))
                        }
                    }
                }
            }
        }
    }
}