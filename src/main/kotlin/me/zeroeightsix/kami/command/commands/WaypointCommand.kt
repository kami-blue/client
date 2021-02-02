package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.manager.managers.WaypointManager.Waypoint
import me.zeroeightsix.kami.module.modules.movement.AutoWalk
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.math.CoordinateConverter.bothConverted
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue
import net.minecraft.util.math.BlockPos

object WaypointCommand : ClientCommand(
    name = "waypoint",
    alias = arrayOf("wp"),
    description = "Manages waypoint."
) {
    private val stashRegex = "\\(\\d+ chests, \\d+ shulkers, \\d+ droppers, \\d+ dispensers, \\d+ hoppers\\)".toRegex()
    private var confirmTime = 0L

    init {
        literal("add", "new", "create", "+") {
            string("name") { nameArg ->
                blockPos("pos") { posArg ->
                    execute("Add a custom waypoint") {
                        add(nameArg.value, posArg.value)
                    }
                }

                int("x") { xArg ->
                    int("y") { yArg ->
                        int("z") { zArg ->
                            execute("Add a custom waypoint") {
                                add(nameArg.value, BlockPos(xArg.value, yArg.value, zArg.value))
                            }
                        }
                    }
                }

                executeSafe("Add a waypoint at your position") {
                    add(nameArg.value, player.position)
                }
            }

            executeSafe("Add an unnamed waypoint at your position") {
                add("Unnamed", player.position)
            }
        }

        literal("del", "remove", "delete", "-") {
            int("id") { idArg ->
                executeAsync("Delete a waypoint by ID") {
                    delete(idArg.value)
                }
            }
        }

        literal("goto", "path") {
            int("id") { idArg ->
                execute("Go to a waypoint with Baritone") {
                    goto(idArg.value)
                }
            }

            blockPos("pos") { posArg ->
                execute("Go to a coordinate with Baritone") {
                    val pos = posArg.value
                    goto(pos.x, pos.y, pos.z)
                }
            }

            int("x") { xArg ->
                int("y") { yArg ->
                    int("z") { zArg ->
                        execute("Go to a coordinate with Baritone") {
                            goto(xArg.value, yArg.value, zArg.value)
                        }
                    }
                }
            }
        }

        literal("list") {
            execute("List saved waypoints") {
                list()
            }
        }

        literal("stash", "stashes") {
            executeAsync("List stash waypoints") {
                stash()
            }
        }

        literal("search") {
            string("name") { nameArg ->
                executeAsync("Search waypoints by name") {
                    search(nameArg.value)
                }
            }
        }

        literal("clear") {
            execute("Clear all waypoints") {
                clear()
            }
        }
    }

    private fun add(name: String, pos: BlockPos) {
        WaypointManager.add(pos, name)
        MessageSendHelper.sendChatMessage("Added waypoint at ${pos.asString()} in the ${InfoCalculator.dimension()} with name '&7$name&f'.")
    }

    private fun delete(id: Int) {
        if (WaypointManager.remove(id)) {
            MessageSendHelper.sendChatMessage("Removed waypoint with ID $id")
        } else {
            MessageSendHelper.sendChatMessage("No waypoint with ID $id")
        }
    }

    private fun goto(id: Int) {
        val waypoint = WaypointManager.get(id)
        if (waypoint != null) {
            if (AutoWalk.isEnabled) AutoWalk.disable()
            val pos = waypoint.currentPos()
            MessageSendHelper.sendBaritoneCommand("goto", pos.x.toString(), pos.y.toString(), pos.z.toString())
        } else {
            MessageSendHelper.sendChatMessage("Couldn't find a waypoint with the ID $id")
        }
    }

    private fun goto(x: Int, y: Int, z: Int) {
        if (AutoWalk.isEnabled) AutoWalk.disable()
        MessageSendHelper.sendBaritoneCommand("goto", x.toString(), y.toString(), z.toString())
    }

    private fun list() {
        if (WaypointManager.waypoints.isEmpty()) {
            MessageSendHelper.sendChatMessage("No waypoints have been saved.")
        } else {
            MessageSendHelper.sendChatMessage("List of waypoints:")
            WaypointManager.waypoints.forEach {
                MessageSendHelper.sendRawChatMessage(format(it))
            }
        }
    }

    private fun stash() {
        val filtered = WaypointManager.waypoints.filter { it.name.matches(stashRegex) }
        if (filtered.isEmpty()) {
            MessageSendHelper.sendChatMessage("No stashes have been logged.")
        } else {
            MessageSendHelper.sendChatMessage("List of logged stashes:")
            filtered.forEach {
                MessageSendHelper.sendRawChatMessage(format(it))
            }
        }
    }

    private fun search(name: String) {
        val filtered = WaypointManager.waypoints.filter { it.name.equals(name, true) }
        if (filtered.isEmpty()) {
            MessageSendHelper.sendChatMessage("No results for &7$name&f")
        } else {
            MessageSendHelper.sendChatMessage("Result of search for &7$name&f:")
            filtered.forEach {
                MessageSendHelper.sendRawChatMessage(format(it))
            }
        }
    }

    private fun clear() {
        if (System.currentTimeMillis() - confirmTime > 15000L) {
            confirmTime = System.currentTimeMillis()
            MessageSendHelper.sendWarningMessage("This will delete ALL your waypoints, " +
                "run ${formatValue("$prefixName clear")} again to confirm"
            )
        } else {
            confirmTime = 0L
            WaypointManager.clear()
            MessageSendHelper.sendChatMessage("Waypoints have been &ccleared")
        }
    }

    private fun format(waypoint: Waypoint): String {
        return "${waypoint.id} [${waypoint.server}] ${waypoint.name} (${bothConverted(waypoint.dimension, waypoint.pos)})"
    }

}