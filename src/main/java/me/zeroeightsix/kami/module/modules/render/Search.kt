package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ColourConverter.rgbToInt
import me.zeroeightsix.kami.util.ESPHelper.drawESPBlock
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.block.Block
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.chunk.Chunk
import org.lwjgl.opengl.GL11
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set
import kotlin.math.sqrt

/**
 * @author wnuke
 * Updated by dominikaaaa on 20/04/20
 * Updated by Afel on 08/06/20
 * Rewrote by Xiaro on 24/07/20
 */
@Module.Info(
        name = "Search",
        description = "Highlights blocks in the world",
        category = Module.Category.RENDER
)
class Search : Module() {
    private val kek = register(Settings.i("kek", 5))
    private val renderUpdate = register(Settings.integerBuilder("RenderUpdate").withValue(1500).withRange(100, 5000).build())
    var overrideWarning = register(Settings.booleanBuilder("OverrideWarning").withValue(false).withVisibility { false }.build())
    private val range = register(Settings.integerBuilder("SearchRange").withValue(128).withRange(1, 256).build())
    private val maximumBlocks = register(Settings.integerBuilder("MaximumBlocks").withValue(128).withRange(16, 1024).build())
    private val filled = register(Settings.b("Filled", true))
    private val outline = register(Settings.b("Outline", true))
    private val tracer = register(Settings.b("Tracer", true))
    private val customColours = register(Settings.b("CustomColours", false))
    private val r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).withVisibility { customColours.value }.build())
    private val g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).withVisibility { customColours.value }.build())
    private val b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).withVisibility { customColours.value }.build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(31).withRange(0, 255).withVisibility { filled.value }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(127).withRange(0, 255).withVisibility { outline.value }.build())
    private val aTracer = register(Settings.integerBuilder("TracerAlpha").withValue(200).withRange(0, 255).withVisibility { tracer.value }.build())
    private val thickness = register(Settings.floatBuilder("LineThickness").withValue(2.0f).withRange(0.0f, 16.0f).build())

    /* Search list */
    private val defaultSearchList = "minecraft:portal,minecraft:end_portal_frame,minecraft:bed"
    private val searchList = register(Settings.stringBuilder("SearchList").withValue(defaultSearchList).withVisibility { false }.build())

    lateinit var searchArrayList: ArrayList<String>

    private fun searchGetArrayList(): ArrayList<String> {
        return ArrayList(searchList.value.split(","))
    }

    fun searchGetString(): String   {
        return searchArrayList.joinToString()
    }

    fun searchAdd(name: String) {
        searchArrayList.add(name)
        searchList.value = searchGetString()
    }

    fun searchRemove(name: String) {
        searchArrayList.remove(name)
        searchList.value = searchGetString()
    }

    fun searchSet(name: String) {
        searchClear()
        searchAdd(name)
    }

    fun searchDefault() {
        searchList.value = defaultSearchList
        searchArrayList = searchGetArrayList()
    }

    fun searchClear() {
        searchList.value = ""
        searchArrayList.clear()
    }
    /* End of eject list */

    private val loadChunks = ConcurrentLinkedQueue<Chunk>()
    private var thread = Thread(Runnable { updateMainList() })
    private val chunkThreads = Executors.newCachedThreadPool()
    private val mainList = ConcurrentHashMap<ChunkPos, Map<BlockPos, Block>>()
    private val renderList = ConcurrentHashMap<BlockPos, Int>()
    private var startTimeChunk = System.currentTimeMillis()
    private var startTimeRender = System.currentTimeMillis()
    private var emptyList = false

    override fun getHudInfo(): String {
        return if (renderList.isNotEmpty()) renderList.size.toString() else "0"
    }

    override fun onEnable() {
        if (!overrideWarning.value && GlStateManager.glGetString(GL11.GL_VENDOR).contains("Intel")) {
            MessageSendHelper.sendErrorMessage("$chatName Warning: Running Search with an Intel Integrated GPU is not recommended, as it has a &llarge&r impact on performance.")
            MessageSendHelper.sendWarningMessage("$chatName If you're sure you want to try, run the &7 ${Command.getCommandPrefix()}search override&f command")
            disable()
            return
        }
        searchArrayList = searchGetArrayList()

        if (thread.state == Thread.State.NEW || thread.state == Thread.State.TERMINATED) {
            thread = Thread(Runnable { updateMainList() })
            thread.priority = 2
            thread.start()
        }
    }

    override fun onUpdate() {
        if (shouldUpdateChunk()) {
            Thread(Runnable {
                updateLoadedChunkList()
            }).start()
        }

        if (shouldUpdateRender()) {
            val cacheList = getRenderList()
            if (cacheList.isNotEmpty()) {
                emptyList = false
                renderList.clear()
                renderList.putAll(cacheList)
            } else if (!emptyList) {
                emptyList = true
            } else {
                emptyList = false
                renderList.clear()
            }
        }
    }

    override fun onWorldRender(event: RenderEvent?) {
        if (renderList.isNotEmpty() && (filled.value || outline.value || tracer.value)) {
            var colour = rgbToInt(r.value, g.value, b.value)
            for ((key, value) in renderList.entries) {
                if (value != -1) colour = value
                drawESPBlock(key, filled.value, outline.value, tracer.value, colour, aFilled.value, aOutline.value, aTracer.value, thickness.value)
            }
        }
    }

    /* Main list updating */
    private fun shouldUpdateChunk(): Boolean {
        return if (System.currentTimeMillis() - startTimeChunk < 3000L) {
            false
        } else {
            startTimeChunk = System.currentTimeMillis()
            true
        }
    }

    private fun updateLoadedChunkList() {
        /* Removes unloaded chunks from the list */
        val toRemove = ArrayList<Chunk>()
        for (chunk in loadChunks) {
            if (chunk.isLoaded) continue
            toRemove.add(chunk)
            mainList.remove(chunk.pos)
        }

        /* Adds new loaded chunks to the list */
        val renderDist = mc.gameSettings.renderDistanceChunks
        val chunkPos1 = ChunkPos(mc.player.chunkCoordX - renderDist, mc.player.chunkCoordZ - renderDist)
        val chunkPos2 = ChunkPos(mc.player.chunkCoordX + renderDist, mc.player.chunkCoordZ + renderDist)
        for (x in chunkPos1.x..chunkPos2.x) for (z in chunkPos1.z..chunkPos2.z) {
            val chunk = mc.world.getChunk(x, z)
            if (!chunk.isLoaded || loadChunks.contains(chunk)) continue
            loadChunks.add(chunk)
        }
        loadChunks.removeAll(toRemove)
    }

    private fun updateMainList() {
        while (isEnabled) {
            val list = loadChunks
            for (chunk in list) {
                val thread = Thread(Runnable {
                    val pos = chunk.pos
                    val found = findBlocksInChunk(chunk, searchArrayList.toTypedArray())
                    mainList[pos] = found
                })
                thread.priority = 1
                chunkThreads.execute(thread)
                Thread.sleep(5L) /* Don't search all chunks in once to avoid lag */
            }
            Thread.sleep(renderUpdate.value.toLong())
        }
    }

    private fun findBlocksInChunk(chunk: Chunk, blocksToFind: Array<String>): Map<BlockPos, Block> {
        val pos1 = BlockPos(chunk.pos.xStart, 0, chunk.pos.zStart)
        val pos2 = BlockPos(chunk.pos.xEnd, 256, chunk.pos.zEnd)
        val blocks = BlockPos.getAllInBox(pos1, pos2)
        val foundBlocks = HashMap<BlockPos, Block>()
        try {
            for (blockPos in blocks) {
                val block = chunk.getBlockState(blockPos).block
                if (block == Blocks.AIR) continue
                if (blocksToFind.contains(block.registryName.toString())) {
                    foundBlocks[blockPos] = block
                }
            }
        } catch (ignored: NullPointerException) {
        } //to fix ghost chunks get loaded and generating NullPointerExceptions
        return foundBlocks
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

    private fun getRenderList(): Map<BlockPos, Int> {
        val cacheDistMap = TreeMap<Double, Map<BlockPos, Block>>(Comparator.naturalOrder())
        /* Calculates distance for all BlockPos, ignores the ones out of the setting range, and puts them into the cacheMap to sort them */
        for (value in mainList.values) {
            for ((key, value) in value) {
                val distance = sqrt(mc.player.getDistanceSq(key))
                if (distance > range.value) continue
                cacheDistMap[distance] = mapOf(Pair(key, value))
            }
        }

        /* Removes the furthest blocks to keep it in the maximum block limit */
        while (cacheDistMap.size > maximumBlocks.value) {
            cacheDistMap.pollLastEntry()
        }

        val cacheRenderMap = HashMap<BlockPos, Int>()
        for (e in cacheDistMap.values) {
            val posColorMap = getPosColor(e)
            cacheRenderMap.putAll(posColorMap)
        }
        return cacheRenderMap
    }

    private fun getPosColor(map: Map<BlockPos, Block>): Map<BlockPos, Int> {
        val posColorMap = HashMap<BlockPos, Int>()
        for ((key, value) in map) {
            val color = if (!customColours.value) {
                val c = if (value == Blocks.PORTAL) {
                    rgbToInt(82, 49, 153)
                } else {
                    value.blockMapColor.colorValue
                }
                rgbToInt((c shr 16), (c shr 8 and 255), (c and 255))
            } else {
                -1
            }
            posColorMap[key] = color
        }
        return posColorMap
    }
    /* End of rendering */
}