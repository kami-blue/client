package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ChunkEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.EntityUtils.getInterpolatedPos
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.math.VectorUtils.distanceTo
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.Minecraft
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.apache.commons.lang3.SystemUtils
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.GL_LINE_LOOP
import org.lwjgl.opengl.GL11.glLineWidth
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*

object NewChunks : Module(
    name = "NewChunks",
    description = "Highlights newly generated chunks",
    category = Category.RENDER
) {
    private val relative = setting("Relative", true)
    private val autoClear = setting("AutoClear", true)
    private val saveNewChunks = setting("SaveNewChunks", false)
    private val saveOption = setting("SaveOption", SaveOption.EXTRA_FOLDER, { saveNewChunks.value })
    private val saveInRegionFolder = setting("InRegion", false, { saveNewChunks.value })
    private val alsoSaveNormalCoords = setting("SaveNormalCoords", false, { saveNewChunks.value })
    private val closeFile = setting("CloseFile", false, { saveNewChunks.value })
    private val renderMode = setting("RenderMode", RenderMode.BOTH)
    private val yOffset = setting("YOffset", 0, -256..256, 4, { isWorldMode })
    private val customColor = setting("CustomColor", false, { isWorldMode })
    private val red = setting("Red", 255, 0..255, 1, { customColor.value && isWorldMode })
    private val green = setting("Green", 255, 0..255, 1, { customColor.value && isWorldMode })
    private val blue = setting("Blue", 255, 0..255, 1, { customColor.value && isWorldMode })
    private val range = setting("RenderRange", 256, 64..1024, 64)
    val radarScale = setting("RadarScale", 2.0,1.0..10.0, 0.1, { isRadarMode })
    private val removeMode = setting("RemoveMode", RemoveMode.MAX_NUM)
    private val maxNum = setting("MaxNum", 10000, 1000..100000, 1000, { removeMode.value == RemoveMode.MAX_NUM })

    private var lastSetting = LastSetting()
    private var logWriter: PrintWriter? = null
    private val timer = TickTimer(TimeUnit.MINUTES)
    val chunks = HashSet<Chunk>()

    override fun onDisable() {
        logWriterClose()
        chunks.clear()
        MessageSendHelper.sendChatMessage("$chatName Saved and cleared chunks!")
    }

    override fun onEnable() {
        timer.reset()
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase == TickEvent.Phase.END && autoClear.value && timer.tick(10L)) {
                chunks.clear()
                MessageSendHelper.sendChatMessage("$chatName Cleared chunks!")
            }
        }

        listener<RenderWorldEvent> {
            if (renderMode.value == RenderMode.RADAR) return@listener
            val y = yOffset.value.toDouble() + if (relative.value) getInterpolatedPos(mc.player, KamiTessellator.pTicks()).y else 0.0
            glLineWidth(2.0f)
            GlStateUtils.depth(false)
            val color = if (customColor.value) ColorHolder(red.value, green.value, blue.value) else ColorHolder(155, 144, 255)
            val buffer = KamiTessellator.buffer
            for (chunk in chunks) {
                if (mc.player.distanceTo(chunk.pos) > range.value) continue
                KamiTessellator.begin(GL_LINE_LOOP)
                buffer.pos(chunk.pos.xStart.toDouble(), y, chunk.pos.zStart.toDouble()).color(color.r, color.g, color.b, 255).endVertex()
                buffer.pos(chunk.pos.xEnd + 1.toDouble(), y, chunk.pos.zStart.toDouble()).color(color.r, color.g, color.b, 255).endVertex()
                buffer.pos(chunk.pos.xEnd + 1.toDouble(), y, chunk.pos.zEnd + 1.toDouble()).color(color.r, color.g, color.b, 255).endVertex()
                buffer.pos(chunk.pos.xStart.toDouble(), y, chunk.pos.zEnd + 1.toDouble()).color(color.r, color.g, color.b, 255).endVertex()
                KamiTessellator.render()
            }
            GlStateUtils.depth(true)
        }

        listener<ChunkEvent> { event ->
            if (event.packet.isFullChunk) return@listener
            chunks.add(event.chunk)
            if (saveNewChunks.value) saveNewChunk(event.chunk)
            if (removeMode.value == RemoveMode.MAX_NUM && chunks.size > maxNum.value) {
                var removeChunk = chunks.first()
                var maxDist = Double.MIN_VALUE
                chunks.forEach {
                    val dist = mc.player.distanceTo(it.pos)
                    if (dist > maxDist) {
                        maxDist = dist
                        removeChunk = it
                    }
                }
                chunks.remove(removeChunk)
            }
        }

        listener<net.minecraftforge.event.world.ChunkEvent.Unload> {
            if (removeMode.value == RemoveMode.UNLOAD)
                chunks.remove(it.chunk)
        }
    }

    // needs to be synchronized so no data gets lost
    private fun saveNewChunk(chunk: Chunk) {
        saveNewChunk(testAndGetLogWriter(), getNewChunkInfo(chunk))
    }

    private fun getNewChunkInfo(chunk: Chunk): String {
        var rV = String.format("%d,%d,%d", System.currentTimeMillis(), chunk.x, chunk.z)
        if (alsoSaveNormalCoords.value) {
            rV += String.format(",%d,%d", chunk.x * 16 + 8, chunk.z * 16 + 8)
        }
        return rV
    }

    private fun testAndGetLogWriter(): PrintWriter? {
        if (lastSetting.testChangeAndUpdate()) {
            logWriterClose()
            logWriterOpen()
        }
        return logWriter
    }

    private fun logWriterClose() {
        if (logWriter != null) {
            logWriter!!.close()
            logWriter = null
            lastSetting = LastSetting() // what if the settings stay the same?
        }
    }

    private fun logWriterOpen() {
        val filepath = path.toString()
        try {
            logWriter = PrintWriter(BufferedWriter(FileWriter(filepath, true)), true)
            var head = "timestamp,ChunkX,ChunkZ"
            if (alsoSaveNormalCoords.value) {
                head += ",x coordinate,z coordinate"
            }
            logWriter!!.println(head)
        } catch (e: Exception) {
            e.printStackTrace()
            KamiMod.LOG.error(chatName + " some exception happened when trying to start the logging -> " + e.message)
            MessageSendHelper.sendErrorMessage(chatName + " onLogStart: " + e.message)
        }
    }

    private val path: Path
        get() {
            // code from baritone (https://github.com/cabaletta/baritone/blob/master/src/main/java/baritone/cache/WorldProvider.java)

            var file: File? = null
            val dimension = mc.player.dimension

            // If there is an integrated server running (Aka Singleplayer) then do magic to find the world save file
            if (mc.isSingleplayer) {
                try {
                    file = mc.integratedServer?.getWorld(dimension)?.chunkSaveLocation
                } catch (e: Exception) {
                    e.printStackTrace()
                    KamiMod.LOG.error("some exception happened when getting canonicalFile -> " + e.message)
                    MessageSendHelper.sendErrorMessage(chatName + " onGetPath: " + e.message)
                }

                // Gets the "depth" of this directory relative the the game's run directory, 2 is the location of the world
                if (file?.toPath()?.relativize(mc.gameDir.toPath())?.nameCount != 2) {
                    // subdirectory of the main save directory for this world
                    file = file?.parentFile
                }
            } else { // Otherwise, the server must be remote...
                file = makeMultiplayerDirectory().toFile()
            }

            // We will actually store the world data in a subfolder: "DIM<id>"
            if (dimension != 0) { // except if it's the overworld
                file = File(file, "DIM$dimension")
            }

            // maybe we want to save it in region folder
            if (saveInRegionFolder.value) {
                file = File(file, "region")
            }
            file = File(file, "newChunkLogs")
            val date = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
            file = File(file, mc.getSession().username + "_" + date + ".csv") // maybe dont safe the name actually. But I also dont want to make another option...
            val rV = file.toPath()
            try {
                if (!Files.exists(rV)) { // ovsly always...
                    Files.createDirectories(rV.parent)
                    Files.createFile(rV)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                KamiMod.LOG.error("some exception happened when trying to make the file -> " + e.message)
                MessageSendHelper.sendErrorMessage(chatName + " onCreateFile: " + e.message)
            }
            return rV
        }

    private fun makeMultiplayerDirectory(): Path {
        var rV = Minecraft.getMinecraft().gameDir
        var folderName: String
        when (saveOption.value) {
            SaveOption.LITE_LOADER_WDL -> {
                folderName = mc.currentServerData?.serverName ?: "Offline"
                rV = File(rV, "saves")
                rV = File(rV, folderName)
            }
            SaveOption.NHACK_WDL -> {
                folderName = nHackInetName
                rV = File(rV, "config")
                rV = File(rV, "wdl-saves")
                rV = File(rV, folderName)

                // extra because name might be different
                if (!rV.exists()) {
                    MessageSendHelper.sendWarningMessage("$chatName nhack wdl directory doesnt exist: $folderName")
                    MessageSendHelper.sendWarningMessage("$chatName creating the directory now. It is recommended to update the ip")
                }
            }
            else -> {
                folderName = mc.currentServerData?.serverName + "-" + mc.currentServerData?.serverIP
                if (SystemUtils.IS_OS_WINDOWS) {
                    folderName = folderName.replace(":", "_")
                }
                rV = File(rV, "KAMI_NewChunks")
                rV = File(rV, folderName)
            }
        }
        return rV.toPath()
    }

    // if there is no port then we have to manually include the standard port..
    private val nHackInetName: String
        get() {
            var folderName = mc.currentServerData?.serverIP ?: "Offline"
            if (SystemUtils.IS_OS_WINDOWS) {
                folderName = folderName.replace(":", "_")
            }
            if (hasNoPort(folderName)) {
                folderName += "_25565" // if there is no port then we have to manually include the standard port..
            }
            return folderName
        }

    private fun hasNoPort(ip: String): Boolean {
        if (!ip.contains("_")) {
            return true
        }
        val sp = ip.split("_").toTypedArray()
        val ending = sp[sp.size - 1]
        // if it is numeric it means it might be a port...
        return ending.toIntOrNull() != null
    }

    private fun saveNewChunk(log: PrintWriter?, data: String) {
        log!!.println(data)
    }

    private enum class SaveOption {
        EXTRA_FOLDER, LITE_LOADER_WDL, NHACK_WDL
    }

    @Suppress("unused")
    private enum class RemoveMode {
        UNLOAD, MAX_NUM, NEVER
    }

    enum class RenderMode {
        WORLD, RADAR, BOTH
    }

    val isRadarMode get() = renderMode.value == RenderMode.BOTH || renderMode.value == RenderMode.RADAR
    private val isWorldMode get() = renderMode.value == RenderMode.BOTH || renderMode.value == RenderMode.WORLD

    private class LastSetting {
        var lastSaveOption: SaveOption? = null
        var lastInRegion = false
        var lastSaveNormal = false
        var dimension = 0
        var ip: String? = null
        fun testChangeAndUpdate(): Boolean {
            if (testChange()) {
                // so we dont have to do this process again next time
                update()
                return true
            }
            return false
        }

        fun testChange(): Boolean {
            // these somehow include the test whether its null
            return saveOption.value != lastSaveOption
                    || saveInRegionFolder.value != lastInRegion
                    || alsoSaveNormalCoords.value != lastSaveNormal
                    || dimension != mc.player.dimension
                    || mc.currentServerData?.serverIP != ip
        }

        private fun update() {
            lastSaveOption = saveOption.value
            lastInRegion = saveInRegionFolder.value
            lastSaveNormal = alsoSaveNormalCoords.value
            dimension = mc.player.dimension
            ip = mc.currentServerData?.serverIP
        }
    }

    init {
        closeFile.valueListeners.add { _, it ->
            if (it) {
                logWriterClose()
                MessageSendHelper.sendChatMessage("$chatName Saved file!")
                closeFile.value = false
            }
        }
    }
}