package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.EntityUtils.getInterpolatedPos
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.math.VectorUtils.distanceTo
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.onMainThread
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.network.play.server.SPacketChunkData
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.*
import java.io.*
import java.util.*
import kotlin.collections.LinkedHashSet

internal object NewChunks : Module(
    name = "NewChunks",
    description = "Highlights newly generated chunks",
    category = Category.RENDER
) {
    private val relative by setting("Relative", false, description = "Renders the chunks at relative Y level to player")
    private val yOffset by setting("YOffset", 0, -256..256, 4, fineStep = 1, description = "Render offset in Y axis")
    private val color by setting("Color", ColorHolder(255, 64, 64, 200), description = "Highlighting color")
    private val thickness by setting("Thickness", 1.5f, 0.1f..4.0f, 0.1f, description = "Thickness of the highlighting square")
    private val range by setting("Render Range", 512, 64..2048, 32, description = "Maximum range for chunks to be highlighted")
    private val autoClear by setting("Auto Clear", false, description = "Clears the new chunks every 10 minutes")
    private val removeMode by setting("Remove Mode", RemoveMode.MAX_NUMBER, description = "Mode to use for removing chunks")
    private val maxNumber by setting("Max Number", 5000, 1000..10000, 500, { removeMode == RemoveMode.MAX_NUMBER }, description = "Maximum number of chunks to keep")

    @Suppress("unused")
    private enum class RemoveMode {
        NEVER, UNLOAD, MAX_NUMBER
    }

    private val timer = TickTimer(TimeUnit.MINUTES)
    private val chunks = LinkedHashSet<Chunk>()

    init {
        onEnable {
            timer.reset()
        }

        onDisable {
            onMainThread {
                chunks.clear()
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase == TickEvent.Phase.END && autoClear && timer.tick(10L)) {
                chunks.clear()
                MessageSendHelper.sendChatMessage("$chatName Cleared chunks!")
            }
        }

        safeListener<RenderWorldEvent> {
            val y = yOffset.toDouble() + if (relative) getInterpolatedPos(player, KamiTessellator.pTicks()).y else 0.0

            glLineWidth(thickness)
            GlStateUtils.depth(false)

            val buffer = KamiTessellator.buffer

            for (chunk in chunks) {
                if (player.distanceTo(chunk.pos) > range) continue

                buffer.begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR)
                buffer.pos(chunk.pos.xStart.toDouble(), y, chunk.pos.zStart.toDouble()).color(color.r, color.g, color.b, color.a).endVertex()
                buffer.pos(chunk.pos.xEnd + 1.0, y, chunk.pos.zStart.toDouble()).color(color.r, color.g, color.b, color.a).endVertex()
                buffer.pos(chunk.pos.xEnd + 1.0, y, chunk.pos.zEnd + 1.0).color(color.r, color.g, color.b, color.a).endVertex()
                buffer.pos(chunk.pos.xStart.toDouble(), y, chunk.pos.zEnd + 1.0).color(color.r, color.g, color.b, color.a).endVertex()
                KamiTessellator.render()
            }

            glLineWidth(1.0f)
            GlStateUtils.depth(true)
        }

        safeListener<PacketEvent.PostReceive> { event ->
            if (event.packet !is SPacketChunkData || event.packet.isFullChunk) return@safeListener
            val chunk = world.getChunk(event.packet.chunkX, event.packet.chunkZ)
            if (chunk.isEmpty) return@safeListener

            onMainThread {
                if (chunks.add(chunk)) {
                    if (removeMode == RemoveMode.MAX_NUMBER && chunks.size > maxNumber) {
                        chunks.maxByOrNull { player.distanceTo(it.pos) }?.let {
                            chunks.remove(it)
                        }
                    }
                }
            }
        }

        listener<ChunkEvent.Unload> {
            onMainThread {
                if (removeMode == RemoveMode.UNLOAD) {
                    chunks.remove(it.chunk)
                }
            }
        }
    }
}