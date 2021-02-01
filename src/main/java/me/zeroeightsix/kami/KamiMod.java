package me.zeroeightsix.kami;

import me.zeroeightsix.kami.event.ForgeEventProcessor;
import me.zeroeightsix.kami.gui.mc.KamiGuiUpdateNotification;
import me.zeroeightsix.kami.util.ConfigUtils;
import me.zeroeightsix.kami.util.threads.BackgroundScope;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(
    modid = KamiMod.ID,
    name = KamiMod.NAME,
    version = KamiMod.VERSION
)
public class KamiMod {

    public static final String NAME = "KAMI Blue";
    public static final String ID = "kamiblue";
    public static final String VERSION = "2.01.xx-dev"; // Used for debugging. R.MM.DD-hash format.
    public static final String VERSION_SIMPLE = "2.01.xx-dev"; // Shown to the user. R.MM.DD[-beta] format.
    public static final String VERSION_MAJOR = "2.01.01"; // Used for update checking. RR.MM.01 format.
    public static final int BUILD_NUMBER = -1; // Do not remove, currently unused but will be used in the future.

    public static final String APP_ID = "638403216278683661";

    public static final String DOWNLOADS_API = "https://kamiblue.org/api/v1/downloads.json";
    public static final String CAPES_JSON = "https://raw.githubusercontent.com/kami-blue/cape-api/capes/capes.json";
    public static final String GITHUB_LINK = "https://github.com/kami-blue";
    public static final String WEBSITE_LINK = "https://kamiblue.org";

    public static final String KAMI_KATAKANA = "カミブル";

    public static final String DIRECTORY = "kamiblue/";
    public static final Logger LOG = LogManager.getLogger("KAMI Blue");

    @Mod.Instance
    public static KamiMod INSTANCE;

    private static boolean ready = false;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        final File directory = new File(DIRECTORY);
        if (!directory.exists()) directory.mkdir();

        KamiGuiUpdateNotification.updateCheck();
        LoaderWrapper.preLoadAll();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOG.info("Initializing " + NAME + " " + VERSION);

        LoaderWrapper.loadAll();

        MinecraftForge.EVENT_BUS.register(ForgeEventProcessor.INSTANCE);

        ConfigUtils.INSTANCE.moveAllLegacyConfigs();
        ConfigUtils.INSTANCE.loadAll();

        BackgroundScope.INSTANCE.start();

        LOG.info(NAME + " Mod initialized!");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ready = true;
    }

    public static boolean isReady() {
        return ready;
    }

}
