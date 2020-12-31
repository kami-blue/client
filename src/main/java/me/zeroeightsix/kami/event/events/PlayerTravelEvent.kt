package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Cancellable
import me.zeroeightsix.kami.event.ICancellable
import me.zeroeightsix.kami.event.KamiEvent

class PlayerTravelEvent : KamiEvent(), ICancellable by Cancellable()