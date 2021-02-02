package org.kamiblue.client.command.commands

import org.kamiblue.client.KamiMod
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.command.CommandManager
import org.kamiblue.client.util.text.MessageSendHelper

// TODO: Add the follow argument when Baritone 1.2.15 is released.
// Currently follow is broken on Forge 1.12.2 in 1.2.14, it is fixed in master branch.
object BaritoneCommand : ClientCommand(
    name = "baritone",
    alias = arrayOf("b")
) {
    init {
        literal("thisway") {
            int("blocks") { blocksArg ->
                executeSafe("Walk in the current direction for X blocks.") {
                    exec("thisway", blocksArg.value.toString())
                    exec("path")
                }
            }
        }

        literal("goal") {
            literal("clear") {
                executeSafe("Clear the current goal.") {
                    exec("goal", "clear")
                }
            }

            int("x/y") { xArg ->
                executeSafe("Set goal to a Y level.") {
                    exec("goal", xArg.value.toString())
                }

                int("y/z") { yArg ->
                    executeSafe("Set goal to X Z.") {
                        exec("goal", xArg.value.toString(), yArg.value.toString())
                    }

                    int("z") { zArg ->
                        executeSafe("Set goal to X Y Z.") {
                            exec("goal", xArg.value.toString(), yArg.value.toString(), zArg.value.toString())
                        }
                    }
                }
            }
        }

        literal("path") {
            executeSafe("Start pathing towards your set goal.") {
                exec("path")
            }
        }

        literal("stop", "cancel") {
            executeSafe("Stop the current Baritone process.") {
                exec("stop")
            }
        }

        literal("mine") {
            block("block") { blockArg ->
                executeSafe("Mine a block.") {
                    exec("mine", blockArg.value.registryName!!.path)
                }
            }

            greedy("blocks") { blocksArg -> // TODO: When array arguments are added switch this over
                executeSafe("Mine any amount of blocks.") {
                    exec("mine", blocksArg.value)
                }
            }
        }

        literal("goto") {
            blockPos("coordinates") { coordinatesArg ->
                executeSafe("Go to a set of coordinates.") {
                    val coord = coordinatesArg.value
                    exec("goto", coord.x.toString(), coord.y.toString(), coord.z.toString())
                }
            }

            baritoneBlock("cached block") { blockArg ->
                executeSafe("Go to a Baritone cached block.") {
                    exec("goto", blockArg.value.registryName!!.path)
                }
            }

            greedy("x y z") { coordinatesArg ->
                executeSafe("Go to a set of coords. Y and Z optional.") {
                    exec("goto", coordinatesArg.value)
                }
            }
        }

        literal("click") {
            executeSafe("Open the click and drag menu.") {
                exec("click")
            }
        }

        literal("build") {
            schematic("schematic") { schematicArg ->
                executeSafe("Build something from inside the schematics folder.") {
                    exec("build", schematicArg.value.name)
                }
            }
        }

        literal("schematica") {
            executeSafe("Build the currently opened schematic in Schematica.") {
                exec("schematica")
            }

            greedy("args") { greedyArg ->
                executeSafe("Build the currently opened schematic in Schematica.") {
                    exec("schematica", greedyArg.value)
                }
            }
        }

        literal("farm") {
            executeSafe("Automatically farm any found crops.") {
                exec("farm")
            }
        }

        literal("explore") {
            executeSafe("Explore away from you.") {
                exec("explore")
            }

            int("x") { xArg ->
                int("z") { zArg ->
                    executeSafe("Explore away from X Z.") {
                        exec("explore", xArg.value.toString(), zArg.value.toString())
                    }
                }
            }
        }

        literal("invert") {
            executeSafe("Go in the opposite direction of your goal.") {
                exec("invert")
            }
        }

        literal("version") {
            executeSafe {
                exec("version")
                MessageSendHelper.sendBaritoneMessage("Running on KAMI Blue ${KamiMod.VERSION}")
            }
        }

        greedy("arguments") { args ->
            executeSafe {
                exec(args.value)
            }
        }
    }

    private fun exec(vararg args: String) {
        val safeArgs = CommandManager.tryParseArgument(args.joinToString(" ")) ?: return
        MessageSendHelper.sendBaritoneCommand(*safeArgs)
    }
}