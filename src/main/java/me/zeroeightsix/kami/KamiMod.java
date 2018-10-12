package me.zeroeightsix.kami;

import me.zero.alpine.EventBus;
import me.zero.alpine.EventManager;
import me.zeroeightsix.kami.command.CommandManager;
import me.zeroeightsix.kami.event.ForgeEventProcessor;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.SettingsPool;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.LagCompensator;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by 086 on 7/11/2017.
 */
@Mod(modid = KamiMod.MODID, name = KamiMod.MODNAME, version = KamiMod.MODVER)
public class KamiMod {

    public static final String MODID = "kami";
    public static final String MODNAME = "KAMI";
    public static final String MODVER = "b9";

    public static final String KAMI_HIRAGANA = "\u304B\u307F";
    public static final String KAMI_KATAKANA = "\u30AB\u30DF";
    public static final String KAMI_KANJI = "\u795E";

    public static final Logger log = LogManager.getLogger("KAMI");

    public static final EventBus EVENT_BUS = new EventManager();

    @Mod.Instance
    private static KamiMod INSTANCE;

    public KamiGUI guiManager;
    public CommandManager commandManager;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        KamiMod.log.info("\n\nInitializing KAMI " + MODVER);

        ModuleManager.initialize();

        ModuleManager.getModules().stream().filter(module -> module.alwaysListening).forEach(EVENT_BUS::subscribe);
        MinecraftForge.EVENT_BUS.register(new ForgeEventProcessor());
        LagCompensator.INSTANCE = new LagCompensator();

        Wrapper.init();

        guiManager = new KamiGUI();
        guiManager.initializeGUI();

        commandManager = new CommandManager();

        File file = new File("kami.settings");
        Friends.INSTANCE.initSettings();
        if (file.exists()) {
            try {
                SettingsPool.load(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        KamiMod.log.info("Settings loaded");

        // After settings loaded, we want to let the enabled modules know they've been enabled (since the setting is done through reflection)
        ModuleManager.getModules().stream().filter(Module::isEnabled).forEach(Module::enable);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                File f = new File("kami.settings");
                if (!f.exists())
                    f.createNewFile();
                SettingsPool.save(f);
                ModuleManager.getModules().forEach(Module::destroy);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }));

        KamiMod.log.info("KAMI Mod initialized!\n");
    }

    public static KamiMod getInstance() {
        return INSTANCE;
    }

    public KamiGUI getGuiManager() {
        return guiManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
