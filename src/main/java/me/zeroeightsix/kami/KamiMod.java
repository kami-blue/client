package me.zeroeightsix.kami;

import com.google.common.base.Converter;
import com.google.gson.JsonObject;
import me.zeroeightsix.kami.command.CommandManager;
import me.zeroeightsix.kami.event.ForgeEventProcessor;
import me.zeroeightsix.kami.event.KamiEventBus;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.gui.mc.KamiGuiUpdateNotification;
import me.zeroeightsix.kami.manager.ManagerLoader;
import me.zeroeightsix.kami.manager.managers.FileInstanceManager;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ConfigUtils;
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;

@Mod(
    modid = KamiMod.MODID,
    name = KamiMod.MODNAME,
    version = KamiMod.VER_FULL_BETA
)
public class KamiMod {

    public static final String MODNAME = "KAMI Blue";
    public static final String MODID = "kamiblue";
    public static final String VER_FULL_BETA = "v1.1.7-beta"; // this is changed to v1.x.x-commit for debugging by automatic builds
    public static final String VER_SMALL = "v1.1.7-beta"; // shown to the user, unchanged
    public static final String VER_STABLE = "1.11.01"; // used for update checking

    public static final String APP_ID = "638403216278683661";

    public static final String DOWNLOADS_API = "https://kamiblue.org/api/v1/downloads.json";
    public static final String CAPES_JSON = "https://raw.githubusercontent.com/kami-blue/cape-api/capes/capes.json";
    public static final String GITHUB_LINK = "https://github.com/kami-blue/";
    public static final String WEBSITE_LINK = "https://kamiblue.org";

    public static final String KAMI_KATAKANA = "\u30ab\u30df\u30d6\u30eb";

    public static final String DIRECTORY = "kamiblue/";
    public static final Logger LOG = LogManager.getLogger("KAMI Blue");

    @Mod.Instance
    public static KamiMod INSTANCE;
    public static Thread MAIN_THREAD;

    private KamiGUI guiManager;
    private CommandManager commandManager;
    private Setting<JsonObject> guiStateSetting;

    @SuppressWarnings("ResultOfMethodCallIgnored") // Java meme
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        final File directory = new File(DIRECTORY);
        if (!directory.exists()) directory.mkdir();

        MAIN_THREAD = Thread.currentThread();
        KamiGuiUpdateNotification.updateCheck();
        ModuleManager.preLoad();
        ManagerLoader.preLoad();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOG.info("Initializing " + MODNAME + " " + VER_FULL_BETA);

        ModuleManager.load();
        ManagerLoader.load();

        MinecraftForge.EVENT_BUS.register(ForgeEventProcessor.INSTANCE);

        guiStateSetting = Settings.custom("gui", new JsonObject(), new Converter<JsonObject, JsonObject>() {
            @Override
            protected JsonObject doForward(@Nullable JsonObject jsonObject) {
                return jsonObject;
            }

            @Override
            protected JsonObject doBackward(@Nullable JsonObject jsonObject) {
                return jsonObject;
            }
        }).buildAndRegister("");
        guiManager = new KamiGUI();
        guiManager.initializeGUI();
        commandManager = new CommandManager();

        FileInstanceManager.fixEmptyFiles();
        ConfigUtils.INSTANCE.loadAll();

        // After settings loaded, we want to let the enabled modules know they've been enabled (since the setting is done through reflection)
        for (Module module : ModuleManager.getModules()) {
            if (module.getAlwaysListening()) KamiEventBus.INSTANCE.subscribe(module);
            if (module.isEnabled()) module.enable();
        }

        // Need to reload the font after the settings were loaded
        KamiFontRenderer.INSTANCE.reloadFonts();

        LOG.info(MODNAME + " Mod initialized!");
    }

    public KamiGUI getGuiManager() {
        return guiManager;
    }

    public void setGuiManager(KamiGUI guiManager) {
        this.guiManager = guiManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public Setting<JsonObject> getGuiStateSetting() {
        return guiStateSetting;
    }

}
