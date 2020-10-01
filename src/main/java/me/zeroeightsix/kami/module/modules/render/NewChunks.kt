package me.zeroeightsix.kami.module.modules.render

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ChunkEvent
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting.SettingListeners
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils.getInterpolatedPos
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.graphics.KamiTessellator.begin
import me.zeroeightsix.kami.util.graphics.KamiTessellator.pTicks
import me.zeroeightsix.kami.util.graphics.KamiTessellator.render
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendWarningMessage
import net.minecraft.client.Minecraft
import net.minecraft.world.chunk.Chunk
import org.apache.commons.lang3.SystemUtils
import org.lwjgl.opengl.GL11.*
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

@Module.Info(
        name = "NewChunks",
        description = "Highlights newly generated chunks",
        category = Module.Category.RENDER
)
object NewChunks : Module() {
    private val yOffset = register(Settings.i("YOffset", 0))
    private val relative = register(Settings.b("Relative", true))
    private val autoClear = register(Settings.b("AutoClear", true))
    private val saveNewChunks = register(Settings.b("SaveNewChunks", false))
    private val saveOption = register(Settings.enumBuilder(SaveOption::class.java).withValue(SaveOption.EXTRA_FOLDER).withName("SaveOption").withVisibility { saveNewChunks.value }.build())
    private val saveInRegionFolder = register(Settings.booleanBuilder("InRegion").withValue(false).withVisibility { saveNewChunks.value }.build())
    private val alsoSaveNormalCoords = register(Settings.booleanBuilder("SaveNormalCoords").withValue(false).withVisibility { saveNewChunks.value }.build())
    private val closeFile = register(Settings.booleanBuilder("CloseFile").withValue(false).withVisibility { saveNewChunks.value }.build())
    private val range = register(Settings.integerBuilder("RenderRange").withValue(256).withRange(64, 1024).build())
    private val customColor = register(Settings.b("CustomColor", false))
    private val red = register(Settings.integerBuilder("Red").withRange(0, 255).withValue(255).withVisibility { customColor.value }.build())
    private val green = register(Settings.integerBuilder("Green").withRange(0, 255).withValue(255).withVisibility { customColor.value }.build())
    private val blue = register(Settings.integerBuilder("Blue").withRange(0, 255).withValue(255).withVisibility { customColor.value }.build())

    private var lastSetting = LastSetting()
    private var logWriter: PrintWriter? = null
    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.MINUTES)
    private val chunks = ArrayList<Chunk>()

    override fun onDisable() {
        logWriterClose()
        chunks.clear()
        sendChatMessage("$chatName Saved and cleared chunks!")
    }

    override fun onEnable() {
        timer.reset()
    }

    override fun onUpdate() {
        if (autoClear.value && timer.tick(10L)) {
            chunks.clear()
            sendChatMessage("$chatName Cleared chunks!")
        }
    }

    override fun onWorldRender(event: RenderEvent) {
        val y = yOffset.value.toDouble() + if (relative.value) getInterpolatedPos(mc.player, pTicks()).y else 0.0
        glLineWidth(2.0f)
        glDisable(GL_DEPTH_TEST)
        val color = if (customColor.value) ColorHolder(red.value, green.value, blue.value) else ColorHolder(155, 144, 255)
        val buffer = KamiTessellator.buffer
        for (chunk in chunks) {
            if (sqrt(chunk.pos.getDistanceSq(mc.player)) > range.value) continue
            begin(GL_LINE_LOOP)
            buffer.pos(chunk.pos.xStart.toDouble(), y, chunk.pos.zStart.toDouble()).color(color.r, color.g, color.b, 255).endVertex()
            buffer.pos(chunk.pos.xEnd + 1.toDouble(), y, chunk.pos.zStart.toDouble()).color(color.r, color.g, color.b, 255).endVertex()
            buffer.pos(chunk.pos.xEnd + 1.toDouble(), y, chunk.pos.zEnd + 1.toDouble()).color(color.r, color.g, color.b, 255).endVertex()
            buffer.pos(chunk.pos.xStart.toDouble(), y, chunk.pos.zEnd + 1.toDouble()).color(color.r, color.g, color.b, 255).endVertex()
            render()
        }
        glEnable(GL_DEPTH_TEST)
    }

    @EventHandler
    private val listener = Listener(EventHook { event: ChunkEvent ->
        if (event.packet.isFullChunk) return@EventHook
        chunks.add(event.chunk)
        if (saveNewChunks.value) saveNewChunk(event.chunk)
    })

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
            KamiMod.log.error(chatName + " some exception happened when trying to start the logging -> " + e.message)
            sendErrorMessage(chatName + " onLogStart: " + e.message)
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
                    file = Objects.requireNonNull(mc.getIntegratedServer())!!.getWorld(dimension).chunkSaveLocation
                } catch (e: Exception) {
                    e.printStackTrace()
                    KamiMod.log.error("some exception happened when getting canonicalFile -> " + e.message)
                    sendErrorMessage(chatName + " onGetPath: " + e.message)
                }

                // Gets the "depth" of this directory relative the the game's run directory, 2 is the location of the world
                if (Objects.requireNonNull(file)!!.toPath().relativize(mc.gameDir.toPath()).nameCount != 2) {
                    // subdirectory of the main save directory for this world
                    file = file!!.parentFile
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
                KamiMod.log.error("some exception happened when trying to make the file -> " + e.message)
                sendErrorMessage(chatName + " onCreateFile: " + e.message)
            }
            return rV
        }

    private fun makeMultiplayerDirectory(): Path {
        var rV = Minecraft.getMinecraft().gameDir
        var folderName: String
        when (saveOption.value) {
            SaveOption.LITE_LOADER_WDL -> {
                folderName = Objects.requireNonNull(mc.getCurrentServerData())!!.serverName
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
                    sendWarningMessage("$chatName nhack wdl directory doesnt exist: $folderName")
                    sendWarningMessage("$chatName creating the directory now. It is recommended to update the ip")
                }
            }
            else -> {
                folderName = Objects.requireNonNull(mc.getCurrentServerData())!!.serverName + "-" + mc.getCurrentServerData()!!.serverIP
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
            var folderName = Objects.requireNonNull(mc.getCurrentServerData())!!.serverIP
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
        return !isInteger(ending)
    }

    private fun isInteger(s: String): Boolean {
        try {
            s.toInt()
        } catch (e: NumberFormatException) {
            return false
        } catch (e: NullPointerException) {
            return false
        }
        return true
    }

    private fun saveNewChunk(log: PrintWriter?, data: String) {
        log!!.println(data)
    }

    @EventHandler
    private val unloadListener = Listener(EventHook { event: net.minecraftforge.event.world.ChunkEvent.Unload -> chunks.remove(event.chunk) }
    )

    private enum class SaveOption {
        EXTRA_FOLDER, LITE_LOADER_WDL, NHACK_WDL
    }

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
                    || mc.getCurrentServerData()?.serverIP != ip
        }

        private fun update() {
            lastSaveOption = saveOption.value as SaveOption
            lastInRegion = saveInRegionFolder.value
            lastSaveNormal = alsoSaveNormalCoords.value
            dimension = mc.player.dimension
            ip = Objects.requireNonNull(mc.getCurrentServerData())!!.serverIP
        }
    }

    init {
        closeFile.settingListener = SettingListeners {
            if (closeFile.value) {
                logWriterClose()
                sendChatMessage("$chatName Saved file!")
                closeFile.value = false
            }
        }
    }
}