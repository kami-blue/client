package me.zeroeightsix.kami.module.modules.render

import io.netty.util.internal.ConcurrentSet
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.ShaderHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.set
import kotlin.math.max
import kotlin.math.sqrt

@Module.Info(
        name = "Search",
        description = "Highlights blocks in the world",
        category = Module.Category.RENDER
)
object Search : Module() {
    private val renderUpdate = register(Settings.integerBuilder("RenderUpdate").withValue(1500).withRange(500, 3000).withStep(100).build())
    val overrideWarning: Setting<Boolean> = register(Settings.booleanBuilder("OverrideWarning").withValue(false).withVisibility { false }.build())
    private val range = register(Settings.integerBuilder("SearchRange").withValue(128).withRange(0, 256).withStep(8).build())
    private val maximumBlocks = register(Settings.integerBuilder("MaximumBlocks").withValue(256).withRange(16, 4096).withStep(128).build())
    private val filled = register(Settings.b("Filled", true))
    private val outline = register(Settings.b("Outline", true))
    private val tracer = register(Settings.b("Tracer", true))
    private val customColours = register(Settings.b("CustomColours", false))
    private val r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).withStep(1).withVisibility { customColours.value }.build())
    private val g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).withStep(1).withVisibility { customColours.value }.build())
    private val b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).withStep(1).withVisibility { customColours.value }.build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(31).withRange(0, 255).withStep(1).withVisibility { filled.value }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(127).withRange(0, 255).withStep(1).withVisibility { outline.value }.build())
    private val aTracer = register(Settings.integerBuilder("TracerAlpha").withValue(200).withRange(0, 255).withStep(1).withVisibility { tracer.value }.build())
    private val thickness = register(Settings.floatBuilder("LineThickness").withValue(2.0f).withRange(0.25f, 5.0f).withStep(0.25f).build())

    /* Search list */
    private const val defaultSearchList = "minecraft:portal,minecraft:end_portal_frame,minecraft:bed"
    private val searchList = register(Settings.stringBuilder("SearchList").withValue(defaultSearchList).withVisibility { false }.build())

    var searchArrayList = searchGetArrayList()

    private fun searchGetArrayList(): ArrayList<String> {
        return ArrayList(searchList.value.split(","))
    }

    fun searchGetString(): String {
        return searchArrayList.joinToString(separator = ",")
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

    override fun onEnable() {
        if (!overrideWarning.value && ShaderHelper.isIntegratedGraphics()) {
            MessageSendHelper.sendErrorMessage("$chatName Warning: Running Search with an Intel Integrated GPU is not recommended, as it has a &llarge&r impact on performance.")
            MessageSendHelper.sendWarningMessage("$chatName If you're sure you want to try, run the &7 ${Command.getCommandPrefix()}search override&f command")
            disable()
            return
        }
        searchArrayList = searchGetArrayList()
        startTimeChunk = 0L
        startTimeRender = 0L
    }

    init {
        listener<SafeTickEvent> {
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

    private fun updateLoadedChunkList() {
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
            val playerChunkPos = ChunkPos(mc.player.position)
            val chunkPos1 = ChunkPos(playerChunkPos.x - renderDist, playerChunkPos.z - renderDist)
            val chunkPos2 = ChunkPos(playerChunkPos.x + renderDist, playerChunkPos.z + renderDist)
            for (x in chunkPos1.x..chunkPos2.x) for (z in chunkPos1.z..chunkPos2.z) {
                val chunk = mc.world.getChunk(x, z)
                if (!chunk.isLoaded) continue
                loadedChunks.add(chunk.pos)
            }
        }.start()
    }

    private fun updateMainList() {
        Thread {
            for (chunkPos in loadedChunks) {
                val thread = Thread {
                    findBlocksInChunk(chunkPos, searchArrayList.toHashSet())
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

    private fun updateRenderList() {
        Thread {
            val cacheDistMap = TreeMap<Double, BlockPos>(Comparator.naturalOrder())
            /* Calculates distance for all BlockPos, ignores the ones out of the setting range, and puts them into the cacheMap to sort them */
            for (posList in mainList.values) {
                for (i in posList.indices) {
                    val pos = posList[i]
                    val distance = sqrt(mc.player.getDistanceSq(pos))
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

    private fun getPosColor(pos: BlockPos): ColorHolder {
        val blockState = mc.world.getBlockState(pos)
        val block = blockState.block
        return if (!customColours.value) {
            if (block == Blocks.PORTAL) {
                ColorHolder(82, 49, 153)
            } else {
                val colorInt = blockState.getMapColor(mc.world, pos).colorValue
                ColorHolder((colorInt shr 16), (colorInt shr 8 and 255), (colorInt and 255))
            }
        } else {
            ColorHolder(r.value, g.value, b.value)
        }
    }
    /* End of rendering */

    private fun isChunkLoaded(chunkPos: ChunkPos): Boolean {
        return mc.world.getChunk(chunkPos.x, chunkPos.z).isLoaded
    }
}