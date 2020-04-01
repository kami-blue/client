package me.zeroeightsix.kami;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.zero.alpine.EventBus;
import me.zero.alpine.EventManager;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.CommandManager;
import me.zeroeightsix.kami.event.ForgeEventProcessor;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.gui.rgui.util.Docking;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.hidden.RunConfig;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.SettingsRegister;
import me.zeroeightsix.kami.setting.config.Configuration;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.LagCompensator;
import me.zeroeightsix.kami.util.RichPresence;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Created by 086 on 7/11/2017.
 * Updated by S-B99 on 25/03/19
 */
@Mod(
        modid = KamiMod.MODID,
        name = KamiMod.MODNAME,
        version = KamiMod.MODVER,
        updateJSON = KamiMod.UPDATE_JSON
)
public class KamiMod {

    public static final String MODNAME = "KAMI Blue";
    public static final String MODID = "kamiblue";
    public static final String MODVER = "v1.1.2-beta";
    public static final String MODVERSMALL = "v1.1.2-beta";
    public static final String APP_ID = "638403216278683661";

    static final String UPDATE_JSON = "https://raw.githubusercontent.com/S-B99/kamiblue/assets/assets/updateChecker.json";
    public static final String DONATORS_JSON = "https://raw.githubusercontent.com/S-B99/kamiblue/assets/assets/donators.json";
    public static final String CAPES_JSON = "https://raw.githubusercontent.com/S-B99/kamiblue/assets/assets/capes.json";
    public static final String GITHUB_LINK = "https://github.com/S-B99/kamiblue/";
    public static final String WEBSITE_LINK = "https://blue.bella.wtf";


    //    public static final String KAMI_HIRAGANA = "\u304B\u307F";
//    public static final String KAMI_KATAKANA = "\u30AB\u30DF";
    public static final String KAMI_KANJI = "\u30ab\u30df\u30d6\u30eb";
    public static final String KAMI_BLUE = "\u1d0b\u1d00\u1d0d\u026a \u0299\u029f\u1d1c\u1d07";
    public static final String KAMI_JAPANESE_ONTOP = "\u4e0a\u306b\u30ab\u30df\u30d6\u30eb\u30fc";
    public static final String KAMI_ONTOP = "\u1d0b\u1d00\u1d0d\u026a \u0299\u029f\u1d1c\u1d07 \u1d0f\u0274 \u1d1b\u1d0f\u1d18";
    public static final String KAMI_WEBSITE = "\u0299\u1d07\u029f\u029f\u1d00\u002e\u1d21\u1d1b\ua730\u002f\u1d0b\u1d00\u1d0d\u026a\u0299\u029f\u1d1c\u1d07";
    public static final char colour = '\u00A7';
    public static final char separator = '\u23d0';
    public static final char quoteLeft = '\u00ab';
    public static final char quoteRight = '\u00bb';

    private static final String KAMI_CONFIG_NAME_DEFAULT = "KAMIBlueConfig.json";

    public static final Logger log = LogManager.getLogger("KAMI Blue");

    public static final EventBus EVENT_BUS = new EventManager();
    public static final ModuleManager MODULE_MANAGER = new ModuleManager();

    @Mod.Instance
    private static KamiMod INSTANCE;

    public KamiGUI guiManager;
    public CommandManager commandManager;
    private Setting<JsonObject> guiStateSetting = Settings.custom("gui", new JsonObject(), new Converter<JsonObject, JsonObject>() {
        @Override
        protected JsonObject doForward(JsonObject jsonObject) {
            return jsonObject;
        }

        @Override
        protected JsonObject doBackward(JsonObject jsonObject) {
            return jsonObject;
        }
    }).buildAndRegister("");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) { }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (RichPresence.INSTANCE.customUsers != null) {
            for (RichPresence.CustomUser user : RichPresence.INSTANCE.customUsers) {
                if (user.uuid.equalsIgnoreCase(Minecraft.getMinecraft().session.getProfile().getId().toString())) {
                    switch (Integer.parseInt(user.type)) {
                        case 0: {
                            DiscordPresence.presence.smallImageKey = "booster";
                            DiscordPresence.presence.smallImageText = "booster uwu";
                            break;
                        }
                        case 1: {
                            DiscordPresence.presence.smallImageKey = "inviter";
                            DiscordPresence.presence.smallImageText = "inviter owo";
                            break;
                        }
                        case 2: {
                            DiscordPresence.presence.smallImageKey = "giveaway";
                            DiscordPresence.presence.smallImageText = "giveaway winner";
                            break;
                        }
                        case 3: {
                            DiscordPresence.presence.smallImageKey = "contest";
                            DiscordPresence.presence.smallImageText = "contest winner";
                            break;
                        }
                        case 4: {
                            DiscordPresence.presence.smallImageKey = "nine";
                            DiscordPresence.presence.smallImageText = "900th member";
                            break;
                        }
                        case 5: {
                            DiscordPresence.presence.smallImageKey = "github1";
                            DiscordPresence.presence.smallImageText = "contributor!! uwu";
                            break;
                        }
                        default: {
                            DiscordPresence.presence.smallImageKey = "donator2";
                            DiscordPresence.presence.smallImageText = "donator <3";
                            break;
                        }
                    }
                }
            }
        }
        Display.setTitle(MODNAME + " " + KAMI_KANJI + " " + MODVERSMALL);
//        Display.setIcon(WindowIcon.ExtractByteBufferFromImagePath("kami.png"));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        log.info("\n\nInitializing " + MODNAME + " " + MODVER);

        MODULE_MANAGER.register();

        MODULE_MANAGER.getModules().stream().filter(module -> module.alwaysListening).forEach(EVENT_BUS::subscribe);
        MinecraftForge.EVENT_BUS.register(new ForgeEventProcessor());
        LagCompensator.INSTANCE = new LagCompensator();

        Wrapper.init();

        guiManager = new KamiGUI();
        guiManager.initializeGUI();

        commandManager = new CommandManager();

        Friends.initFriends();
        SettingsRegister.register("commandPrefix", Command.commandPrefix);
        loadConfiguration();
        log.info("Settings loaded");

        // custom names aren't known at compile-time
        //MODULE_MANAGER.updateLookup(); // generate the lookup table after settings are loaded to make custom module names work

        new RichPresence();
        log.info("Rich Presence Users init!\n");

        // After settings loaded, we want to let the enabled modules know they've been enabled (since the setting is done through reflection)
        MODULE_MANAGER.getModules().stream().filter(Module::isEnabled).forEach(Module::enable);

        // load modules that are on by default // autoenable
        MODULE_MANAGER.getModule(RunConfig.class).enable();

        log.info(MODNAME + " Mod initialized!\n");
    }

    public static String getConfigName() {
        Path config = Paths.get("KAMIBlueLastConfig.txt");
        String kamiConfigName = KAMI_CONFIG_NAME_DEFAULT;
        try (BufferedReader reader = Files.newBufferedReader(config)) {
            kamiConfigName = reader.readLine();
            if (!isFilenameValid(kamiConfigName)) kamiConfigName = KAMI_CONFIG_NAME_DEFAULT;
        } catch (NoSuchFileException e) {
            try (BufferedWriter writer = Files.newBufferedWriter(config)) {
                writer.write(KAMI_CONFIG_NAME_DEFAULT);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return kamiConfigName;
    }

    public static void loadConfiguration() {
        try {
            loadConfigurationUnsafe();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadConfigurationUnsafe() throws IOException {
        String kamiConfigName = getConfigName();
        Path kamiConfig = Paths.get(kamiConfigName);
        if (!Files.exists(kamiConfig)) return;
        Configuration.loadConfiguration(kamiConfig);

        JsonObject gui = KamiMod.INSTANCE.guiStateSetting.getValue();
        for (Map.Entry<String, JsonElement> entry : gui.entrySet()) {
            Optional<Component> optional = KamiMod.INSTANCE.guiManager.getChildren().stream().filter(component -> component instanceof Frame).filter(component -> ((Frame) component).getTitle().equals(entry.getKey())).findFirst();
            if (optional.isPresent()) {
                JsonObject object = entry.getValue().getAsJsonObject();
                Frame frame = (Frame) optional.get();
                frame.setX(object.get("x").getAsInt());
                frame.setY(object.get("y").getAsInt());
                Docking docking = Docking.values()[object.get("docking").getAsInt()];
                if (docking.isLeft()) ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.LEFT);
                else if (docking.isRight()) ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.RIGHT);
                else if (docking.isCenterVertical())
                    ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.CENTER);
                frame.setDocking(docking);
                frame.setMinimized(object.get("minimized").getAsBoolean());
                frame.setPinned(object.get("pinned").getAsBoolean());
            } else {
                System.err.println("Found GUI config entry for " + entry.getKey() + ", but found no frame with that name");
            }
        }
        KamiMod.getInstance().getGuiManager().getChildren().stream().filter(component -> (component instanceof Frame) && (((Frame) component).isPinnable()) && component.isVisible()).forEach(component -> component.setOpacity(0f));
    }

    public static void saveConfiguration() {
        try {
            saveConfigurationUnsafe();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfigurationUnsafe() throws IOException {
        JsonObject object = new JsonObject();
        KamiMod.INSTANCE.guiManager.getChildren().stream().filter(component -> component instanceof Frame).map(component -> (Frame) component).forEach(frame -> {
            JsonObject frameObject = new JsonObject();
            frameObject.add("x", new JsonPrimitive(frame.getX()));
            frameObject.add("y", new JsonPrimitive(frame.getY()));
            frameObject.add("docking", new JsonPrimitive(Arrays.asList(Docking.values()).indexOf(frame.getDocking())));
            frameObject.add("minimized", new JsonPrimitive(frame.isMinimized()));
            frameObject.add("pinned", new JsonPrimitive(frame.isPinned()));
            object.add(frame.getTitle(), frameObject);
        });
        KamiMod.INSTANCE.guiStateSetting.setValue(object);

        Path outputFile = Paths.get(getConfigName());
        if (!Files.exists(outputFile))
            Files.createFile(outputFile);
        Configuration.saveConfiguration(outputFile);
        MODULE_MANAGER.getModules().forEach(Module::destroy);
    }

    public static boolean isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
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
