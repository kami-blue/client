package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Event
import net.minecraft.network.play.server.SPacketChunkData
import net.minecraft.world.chunk.Chunk

class ChunkEvent(val chunk: Chunk, val packet: SPacketChunkData) : Event