package org.kamiblue.client

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.kamiblue.client.event.ForgeEventProcessor
import org.kamiblue.client.gui.mc.KamiGuiUpdateNotification
import org.kamiblue.client.util.ConfigUtils
import org.kamiblue.client.util.threads.BackgroundScope
import java.io.File

@Mod(
    modid = KamiMod.ID,
    name = KamiMod.NAME,
    version = KamiMod.VERSION
)
class KamiMod {

    companion object {
        const val NAME = "KAMI Blue"
        const val ID = "kamiblue"
        const val VERSION = "2.02.xx-dev" // Used for debugging. R.MM.DD-hash format.
        const val VERSION_SIMPLE = "2.02.xx-dev" // Shown to the user. R.MM.DD[-beta] format.
        const val VERSION_MAJOR = "2.02.01" // Used for update checking. RR.MM.01 format.
        const val BUILD_NUMBER = -1 // Do not remove, currently unused but will be used in the future.

        const val APP_ID = "638403216278683661"

        const val DOWNLOADS_API = "https://kamiblue.org/api/v1/downloads.json"
        const val CAPES_JSON = "https://raw.githubusercontent.com/kami-blue/cape-api/capes/capes.json"
        const val GITHUB_LINK = "https://github.com/kami-blue"
        const val WEBSITE_LINK = "https://kamiblue.org"

        const val KAMI_KATAKANA = "カミブル"

        const val DIRECTORY = "kamiblue/"
        val LOG: Logger = LogManager.getLogger(NAME)

        var ready: Boolean = false

        fun isReady(): Boolean {
            return ready
        }
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        val directory = File(DIRECTORY)
        if (!directory.exists()) directory.mkdir()

        KamiGuiUpdateNotification.updateCheck()
        LoaderWrapper.preLoadAll()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        LOG.info("Initializing $NAME $VERSION")

        LoaderWrapper.loadAll()

        MinecraftForge.EVENT_BUS.register(ForgeEventProcessor)

        ConfigUtils.moveAllLegacyConfigs()
        ConfigUtils.loadAll()

        BackgroundScope.start()

        LOG.info("$NAME Mod initialized!")
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        ready = true
    }
}