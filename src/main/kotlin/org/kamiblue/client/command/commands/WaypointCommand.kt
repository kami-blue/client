package org.kamiblue.client.command.commands

import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextFormatting
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.manager.managers.WaypointManager
import org.kamiblue.client.manager.managers.WaypointManager.Waypoint
import org.kamiblue.client.module.modules.movement.AutoWalk
import org.kamiblue.client.util.BaritoneUtils
import org.kamiblue.client.util.InfoCalculator
import org.kamiblue.client.util.math.CoordinateConverter.asString
import org.kamiblue.client.util.math.CoordinateConverter.bothConverted
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.format
import org.kamiblue.client.util.text.formatValue
import java.text.SimpleDateFormat
import java.util.*

object WaypointCommand : ClientCommand(
    name = "waypoint",
    alias = arrayOf("wp"),
    description = "Manages waypoint."
) {
    private val stashRegex = "\\(\\d+ chests, \\d+ shulkers, \\d+ droppers, \\d+ dispensers, \\d+ hoppers\\)".toRegex()
    private var confirmTime = 0L
    private val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy")

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

        literal("sync") {
            execute("Sync Baritone waypoints to KAMI Blue") {
                val waypoints = BaritoneUtils.primary?.worldProvider?.currentWorld?.waypoints?.allWaypoints

                if (waypoints == null || waypoints.size == 0) {
                    MessageSendHelper.sendErrorMessage("There are no available Baritone waypoints to import!")
                    return@execute
                }

                for (waypoint in waypoints) {
                    WaypointManager.get(waypoint.location) ?: run { // Don't duplicate already existing waypoints.
                        val date = sdf.format(Date(waypoint.creationTimestamp))
                        WaypointManager.add(Waypoint(waypoint.location, waypoint.name, date))
                    }
                }

                MessageSendHelper.sendChatMessage("Imported ${formatValue(waypoints.size)} waypoints from Baritone!")
            }
        }
    }

    private fun add(name: String, pos: BlockPos) {
        WaypointManager.add(pos, name)
        MessageSendHelper.sendChatMessage("Added waypoint at ${pos.asString()} in the ${InfoCalculator.dimension()} with name ${formatValue(name)}.")
    }

    private fun delete(id: Int) {
        if (WaypointManager.remove(id)) {
            MessageSendHelper.sendChatMessage("Removed waypoint with ID ${formatValue(id)}")
        } else {
            MessageSendHelper.sendChatMessage("No waypoint with ID ${formatValue(id)}")
        }
    }

    private fun goto(id: Int) {
        val waypoint = WaypointManager.get(id)
        if (waypoint != null) {
            if (AutoWalk.isEnabled) AutoWalk.disable()
            val pos = waypoint.currentPos()
            MessageSendHelper.sendBaritoneCommand("goto", pos.x.toString(), pos.y.toString(), pos.z.toString())
        } else {
            MessageSendHelper.sendChatMessage("Couldn't find a waypoint with the ID ${formatValue(id)}")
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
            MessageSendHelper.sendChatMessage("No results for ${formatValue(name)}")
        } else {
            MessageSendHelper.sendChatMessage("Result of search for ${formatValue(name)}:")
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
            MessageSendHelper.sendChatMessage("Waypoints have been ${TextFormatting.RED format "cleared"}")
        }
    }

    private fun format(waypoint: Waypoint): String {
        return "${waypoint.id} ${formatValue(waypoint.server.toString())} ${waypoint.name} (${bothConverted(waypoint.dimension, waypoint.pos)})"
    }
}
