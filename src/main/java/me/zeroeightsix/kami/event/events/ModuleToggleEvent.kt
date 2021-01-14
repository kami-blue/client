package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Event
import me.zeroeightsix.kami.module.AbstractModule
import me.zeroeightsix.kami.module.Module

class ModuleToggleEvent internal constructor(val module: AbstractModule) : Event