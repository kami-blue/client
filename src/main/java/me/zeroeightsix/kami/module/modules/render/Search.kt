package me.zeroeightsix.kami.module.modules.render

import io.netty.util.internal.ConcurrentSet
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.setting.settings.impl.collection.CollectionSetting
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.ShaderHelper
import me.zeroeightsix.kami.util.math.VectorUtils.distanceTo
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.set
import kotlin.math.max

@Module.Info(
    name = "Search",
    description = "Highlights blocks in the world",
    category = Module.Category.RENDER
)
object Search : Module() {
    private val defaultSearchList = linkedSetOf("minecraft:portal", "minecraft:end_portal_frame", "minecraft:bed")

    private val renderUpdate = setting("RenderUpdate", 1500, 500..3000, 100)
    val overrideWarning = setting("OverrideWarning", false, { false })
    private val range = setting("SearchRange", 128, 0..256, 8)
    private val maximumBlocks = setting("MaximumBlocks", 256, 16..4096, 128)
    private val filled = setting("Filled", true)
    private val outline = setting("Outline", true)
    private val tracer = setting("Tracer", true)
    private val customColours = setting("CustomColours", false)
    private val r = setting("Red", 155, 0..255, 1, { customColours.value })
    private val g = setting("Green", 144, 0..255, 1, { customColours.value })
    private val b = setting("Blue", 255, 0..255, 1, { customColours.value })
    private val aFilled = setting("FilledAlpha", 31, 0..255, 1, { filled.value })
    private val aOutline = setting("OutlineAlpha", 127, 0..255, 1, { outline.value })
    private val aTracer = setting("TracerAlpha", 200, 0..255, 1, { tracer.value })
    private val thickness = setting("LineThickness", 2.0f, 0.25f..5.0f, 0.25f)
    val searchList = setting(CollectionSetting("SearchList", defaultSearchList, { false }))

    private val chunkThreads = ConcurrentHashMap<ChunkPos, Thread>()
    private val chunkThreadPool = Executors.newCachedThreadPool()
    private val loadedChunks = ConcurrentSet<ChunkPos>()
    private val mainList = ConcurrentHashMap<ChunkPos, List<BlockPos>>()
    private val renderList = ConcurrentHashMap<BlockPos, ColorHolder>()
    private val renderer = ESPRenderer()
    private var dirty = 0
    private var startTimeChunk = 0L
    private var startTimeRender = 0L

    override fun getHudInfo(): String {
        return if (renderList.isNotEmpty()) renderList.size.toString() else "0"
    }

    init {
        onEnable {
            if (!overrideWarning.value && ShaderHelper.isIntegratedGraphics) {
                MessageSendHelper.sendErrorMessage("$chatName Warning: Running Search with an Intel Integrated GPU is not recommended, as it has a &llarge&r impact on performance.")
                MessageSendHelper.sendWarningMessage("$chatName If you're sure you want to try, run the ${formatValue("${CommandManager.prefix}search override")} command")
                disable()
                return@onEnable
            }
            startTimeChunk = 0L
            startTimeRender = 0L
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (shouldUpdateChunk()) {
                updateLoadedChunkList()
                updateMainList()
            }

            if (shouldUpdateRender()) {
                updateRenderList()
            }
        }

        listener<RenderWorldEvent> {
            if (dirty > 1) {
                dirty = 0
                renderer.clear()
                for ((pos, colour) in renderList) renderer.add(pos, colour)
            }
            renderer.render(false)
        }
    }

    /* Main list updating */
    private fun shouldUpdateChunk(): Boolean {
        return if (System.currentTimeMillis() - startTimeChunk < max(renderUpdate.value * 2, 500)) {
            false
        } else {
            startTimeChunk = System.currentTimeMillis()
            true
        }
    }

    private fun SafeClientEvent.updateLoadedChunkList() {
        /* Removes unloaded chunks from the list */
        Thread {
            for (chunkPos in loadedChunks) {
                if (isChunkLoaded(chunkPos)) continue
                chunkThreads.remove(chunkPos)
                loadedChunks.remove(chunkPos)
                mainList.remove(chunkPos)
            }

            /* Adds new loaded chunks to the list */
            val renderDist = mc.gameSettings.renderDistanceChunks
            val playerChunkPos = ChunkPos(player.position)
            val chunkPos1 = ChunkPos(playerChunkPos.x - renderDist, playerChunkPos.z - renderDist)
            val chunkPos2 = ChunkPos(playerChunkPos.x + renderDist, playerChunkPos.z + renderDist)
            for (x in chunkPos1.x..chunkPos2.x) for (z in chunkPos1.z..chunkPos2.z) {
                val chunk = world.getChunk(x, z)
                if (!chunk.isLoaded) continue
                loadedChunks.add(chunk.pos)
            }
        }.start()
    }

    private fun updateMainList() {
        Thread {
            for (chunkPos in loadedChunks) {
                val thread = Thread {
                    findBlocksInChunk(chunkPos, searchList.toHashSet())
                }
                thread.priority = 1
                chunkThreads.putIfAbsent(chunkPos, thread)
            }
            for (thread in chunkThreads.values) {
                chunkThreadPool.execute(thread)
                Thread.sleep(5L)
            }
        }.start()
    }

    private fun findBlocksInChunk(chunkPos: ChunkPos, blocksToFind: HashSet<String>) {
        val yRange = IntRange(0, 256)
        val xRange = IntRange(chunkPos.xStart, chunkPos.xEnd)
        val zRange = IntRange(chunkPos.zStart, chunkPos.zEnd)
        val foundBlocks = ArrayList<BlockPos>()
        for (y in yRange) for (x in xRange) for (z in zRange) {
            val blockPos = BlockPos(x, y, z)
            val block = mc.world.getBlockState(blockPos).block
            if (block == Blocks.AIR) continue
            if (!blocksToFind.contains(block.registryName.toString())) continue
            foundBlocks.add(BlockPos(blockPos))
        }
        mainList[chunkPos] = foundBlocks
    }
    /* End of main list updating */

    /* Rendering */
    private fun shouldUpdateRender(): Boolean {
        return if (System.currentTimeMillis() - startTimeRender < renderUpdate.value) {
            false
        } else {
            startTimeRender = System.currentTimeMillis()
            true
        }
    }

    private fun SafeClientEvent.updateRenderList() {
        Thread {
            val cacheDistMap = TreeMap<Double, BlockPos>(Comparator.naturalOrder())
            /* Calculates distance for all BlockPos, ignores the ones out of the setting range, and puts them into the cacheMap to sort them */
            for (posList in mainList.values) {
                for (i in posList.indices) {
                    val pos = posList[i]
                    val distance = player.distanceTo(pos)
                    if (distance > range.value) continue
                    cacheDistMap[distance] = pos
                }
            }

            /* Removes the furthest blocks to keep it in the maximum block limit */
            while (cacheDistMap.size > maximumBlocks.value) {
                cacheDistMap.pollLastEntry()
            }

            renderList.keys.removeIf { pos ->
                !cacheDistMap.containsValue(pos)
            }

            for (pos in cacheDistMap.values) {
                renderList[pos] = getPosColor(pos)
            }

            /* Updates renderer */
            renderer.aFilled = if (filled.value) aFilled.value else 0
            renderer.aOutline = if (outline.value) aOutline.value else 0
            renderer.aTracer = if (tracer.value) aTracer.value else 0
            renderer.thickness = thickness.value

            if (renderList.size != renderer.getSize()) {
                dirty = 2
            } else {
                dirty++
            }
        }.start()
    }

    private fun SafeClientEvent.getPosColor(pos: BlockPos): ColorHolder {
        val blockState = world.getBlockState(pos)
        val block = blockState.block
        return if (!customColours.value) {
            if (block == Blocks.PORTAL) {
                ColorHolder(82, 49, 153)
            } else {
                val colorInt = blockState.getMapColor(world, pos).colorValue
                ColorHolder((colorInt shr 16), (colorInt shr 8 and 255), (colorInt and 255))
            }
        } else {
            ColorHolder(r.value, g.value, b.value)
        }
    }
    /* End of rendering */

    private fun SafeClientEvent.isChunkLoaded(chunkPos: ChunkPos): Boolean {
        return world.getChunk(chunkPos.x, chunkPos.z).isLoaded
    }
}