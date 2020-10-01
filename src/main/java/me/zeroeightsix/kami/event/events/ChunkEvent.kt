package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.network.play.server.SPacketChunkData
import net.minecraft.world.chunk.Chunk

/**
 * @author 086
 * Updated by Xiaro on 18/08/20
 */
class ChunkEvent(val chunk: Chunk, val packet: SPacketChunkData) : KamiEvent()