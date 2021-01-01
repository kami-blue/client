package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Event
import me.zeroeightsix.kami.module.Module

class ModuleToggleEvent(val module: Module, val prevState: Boolean) : Event