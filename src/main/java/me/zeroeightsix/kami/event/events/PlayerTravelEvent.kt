package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Cancellable
import me.zeroeightsix.kami.event.Event
import me.zeroeightsix.kami.event.ICancellable

class PlayerTravelEvent : Event, ICancellable by Cancellable()