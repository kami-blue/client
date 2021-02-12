package org.kamiblue.client.event.events

import org.kamiblue.client.event.Event
import org.kamiblue.client.module.AbstractModule

class ModuleToggleEvent internal constructor(val module: AbstractModule) : Event