package me.zeroeightsix.kami.event.events

import baritone.api.command.ICommand
import me.zeroeightsix.kami.event.KamiEvent

class BaritoneCommandEvent(val command: ICommand) : KamiEvent()