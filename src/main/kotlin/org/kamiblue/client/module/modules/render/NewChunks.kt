package org.kamiblue.client.module.modules.render

import kotlinx.coroutines.runBlocking
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.network.play.server.SPacketChunkData
import net.minecraft.util.math.ChunkPos
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.RenderWorldEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.EntityUtils.getInterpolatedPos
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.GlStateUtils
import org.kamiblue.client.util.graphics.KamiTessellator
import org.kamiblue.client.util.math.VectorUtils.distanceTo
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.onMainThread
import org.kamiblue.client.util.threads.safeAsyncListener
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.asyncListener
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
    private val yOffset by setting("Y Offset", 0, -256..256, 4, fineStep = 1, description = "Render offset in Y axis")
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
    private val chunks = LinkedHashSet<ChunkPos>()

    init {
        onEnable {
            timer.reset()
        }

        onDisable {
            runBlocking {
                onMainThread {
                    chunks.clear()
                }
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

            for (chunkPos in chunks) {
                if (player.distanceTo(chunkPos) > range) continue

                buffer.begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR)
                buffer.pos(chunkPos.xStart.toDouble(), y, chunkPos.zStart.toDouble()).color(color.r, color.g, color.b, color.a).endVertex()
                buffer.pos(chunkPos.xEnd + 1.0, y, chunkPos.zStart.toDouble()).color(color.r, color.g, color.b, color.a).endVertex()
                buffer.pos(chunkPos.xEnd + 1.0, y, chunkPos.zEnd + 1.0).color(color.r, color.g, color.b, color.a).endVertex()
                buffer.pos(chunkPos.xStart.toDouble(), y, chunkPos.zEnd + 1.0).color(color.r, color.g, color.b, color.a).endVertex()
                KamiTessellator.render()
            }

            glLineWidth(1.0f)
            GlStateUtils.depth(true)
        }

        safeAsyncListener<PacketEvent.PostReceive> { event ->
            if (event.packet !is SPacketChunkData || event.packet.isFullChunk) return@safeAsyncListener
            val chunk = world.getChunk(event.packet.chunkX, event.packet.chunkZ)
            if (chunk.isEmpty) return@safeAsyncListener

            onMainThread {
                if (chunks.add(chunk.pos)) {
                    if (removeMode == RemoveMode.MAX_NUMBER && chunks.size > maxNumber) {
                        chunks.maxByOrNull { player.distanceTo(it) }?.let {
                            chunks.remove(it)
                        }
                    }
                }
            }
        }

        asyncListener<ChunkEvent.Unload> {
            onMainThread {
                if (removeMode == RemoveMode.UNLOAD) {
                    chunks.remove(it.chunk.pos)
                }
            }
        }
    }
}