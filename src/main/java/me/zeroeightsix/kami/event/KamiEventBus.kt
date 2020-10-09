package me.zeroeightsix.kami.event

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.util.event.EventBus

object KamiEventBus : EventBus.SynchronizedEventBus(KamiMod.MAIN_THREAD)