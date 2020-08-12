// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami;

import me.zero.alpine.EventManager;
import org.apache.logging.log4j.LogManager;
import java.io.File;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import com.google.gson.JsonPrimitive;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import java.util.Optional;
import java.util.Iterator;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.gui.rgui.util.Docking;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import com.google.gson.JsonElement;
import java.util.Map;
import me.zeroeightsix.kami.setting.config.Configuration;
import java.nio.file.LinkOption;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.nio.file.Path;
import java.nio.file.NoSuchFileException;
import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import me.zeroeightsix.kami.module.modules.gui.DiscordSettings;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.util.Donator;
import me.zeroeightsix.kami.setting.SettingsRegister;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.util.LagCompensator;
import me.zeroeightsix.kami.event.ForgeEventProcessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import me.zeroeightsix.kami.util.ColourUtils;
import java.awt.Color;
import java.util.function.Consumer;
import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.Display;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import java.awt.Font;
import me.zeroeightsix.kami.setting.Settings;
import com.google.common.base.Converter;
import me.zeroeightsix.kami.gui.font.CFontRenderer;
import com.google.gson.JsonObject;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.command.CommandManager;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zero.alpine.EventBus;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;

@Mod(modid = "koncclient", name = "KONC-CLIENT", version = "v2.1", updateJSON = "https://raw.githubusercontent.com/S-B99/KAMI/assets/assets/updateChecker.json")
public class KamiMod
{
    static final String MODID = "koncclient";
    static final String MODNAME = "KONC-CLIENT";
    public static final String MODVER = "v2.1";
    public static final String APP_ID = "650731527835090945";
    static final String UPDATE_JSON = "https://raw.githubusercontent.com/S-B99/KAMI/assets/assets/updateChecker.json";
    public static final String DONATORS_JSON = "https://raw.githubusercontent.com/S-B99/KAMI/assets/assets/donators.json";
    public static final String CAPES_JSON = "https://raw.githubusercontent.com/S-B99/KAMI/assets/assets/capes.json";
    public static final String KAMI_HIRAGANA = "\u304b\u307f";
    public static final String KAMI_KATAKANA = "\u30ab\u30df";
    public static final String KAMI_KANJI = "KONC Client!";
    public static final String KAMI_BLUE = "KONC-Client";
    private static final String KAMI_CONFIG_NAME_DEFAULT = "KONCClientConfig.json";
    public static final Logger log;
    public static final EventBus EVENT_BUS;
    public int redForBG;
    public int greenForBG;
    public int blueForBG;
    public boolean rainbowBG;
    @Mod.Instance
    private static KamiMod INSTANCE;
    public KamiGUI guiManager;
    public CommandManager commandManager;
    private Setting<JsonObject> guiStateSetting;
    CFontRenderer cFontRenderer;
    
    public KamiMod() {
        this.guiStateSetting = Settings.custom("gui", new JsonObject(), new Converter<JsonObject, JsonObject>() {
            protected JsonObject doForward(final JsonObject jsonObject) {
                return jsonObject;
            }
            
            protected JsonObject doBackward(final JsonObject jsonObject) {
                return jsonObject;
            }
        }).buildAndRegister("");
        this.cFontRenderer = new CFontRenderer(new Font("Verdana", 0, 18), true, false);
    }
    
    private void checkSettingGuiColour(final Setting setting) {
        final String name3;
        final String s;
        final String name2 = s = (name3 = setting.getName());
        switch (s) {
            case "Red": {
                this.redForBG = setting.getValue();
                break;
            }
            case "Green": {
                this.greenForBG = setting.getValue();
                break;
            }
            case "Blue": {
                this.blueForBG = setting.getValue();
                break;
            }
        }
    }
    
    private void checkRainbowSetting(final Setting setting) {
        final String name3;
        final String s;
        final String name2 = s = (name3 = setting.getName());
        switch (s) {
            case "Rainbow Watermark": {
                this.rainbowBG = setting.getValue();
                break;
            }
        }
    }
    
    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        Display.setTitle("AstraMod");
    }
    
    @SubscribeEvent
    public void onRenderGui(final RenderGameOverlayEvent.Post event) {
        final Minecraft mc = Minecraft.func_71410_x();
        final float[] hue = { System.currentTimeMillis() % 11520L / 11520.0f };
        ModuleManager.getModuleByName("Gui").settingList.forEach(this::checkSettingGuiColour);
        ModuleManager.getModuleByName("Gui").settingList.forEach(this::checkRainbowSetting);
        final int rgb = Color.HSBtoRGB(hue[0], 1.0f, 1.0f);
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            final String playername = mc.field_71439_g.func_70005_c_();
            if (this.rainbowBG) {
                this.cFontRenderer.drawStringWithShadow("Welcome " + playername + "", 1.0, 10.0, rgb);
                this.cFontRenderer.drawStringWithShadow("AstraMod", 1.0, 1.0, rgb);
                final float[] array = hue;
                final int n = 0;
                final float[] array2 = array;
                final int n2 = 0;
                array2[n2] += 0.02f;
            }
            else {
                this.cFontRenderer.drawStringWithShadow("Welcome " + playername + "", 1.0, 10.0, ColourUtils.toRGBA(this.redForBG, this.greenForBG, this.blueForBG, 255));
                this.cFontRenderer.drawStringWithShadow("AstraMod", 1.0, 1.0, ColourUtils.toRGBA(this.redForBG, this.greenForBG, this.blueForBG, 255));
            }
        }
    }
    
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        KamiMod.log.info("  Initializing AstraMod");
        ModuleManager.initialize();
        ModuleManager.getModules().stream().filter(module -> module.alwaysListening).forEach(KamiMod.EVENT_BUS::subscribe);
        MinecraftForge.EVENT_BUS.register((Object)new ForgeEventProcessor());
        LagCompensator.INSTANCE = new LagCompensator();
        Wrapper.init();
        (this.guiManager = new KamiGUI()).initializeGUI();
        this.commandManager = new CommandManager();
        Friends.initFriends();
        SettingsRegister.register("commandPrefix", Command.commandPrefix);
        loadConfiguration();
        KamiMod.log.info("Settings loaded");
        new Donator();
        KamiMod.log.info("Donators init!\n");
        ModuleManager.getModules().stream().filter(Module::isEnabled).forEach(Module::enable);
        try {
            ModuleManager.getModuleByName("InfoOverlay").setEnabled(true);
            if (((DiscordSettings)ModuleManager.getModuleByName("DiscordRPC")).startupGlobal.getValue()) {
                ModuleManager.getModuleByName("DiscordRPC").setEnabled(true);
            }
        }
        catch (NullPointerException e) {
            KamiMod.log.info("NPE in loading always enabled modules\n");
        }
        KamiMod.log.info("AstraMod initialized! ");
    }
    
    public static String getConfigName() {
        final Path config = Paths.get("AstraMod_LastConfig.txt", new String[0]);
        String kamiConfigName = "AstraMod_Config.json";
        try (final BufferedReader reader = Files.newBufferedReader(config)) {
            kamiConfigName = reader.readLine();
            if (!isFilenameValid(kamiConfigName)) {
                kamiConfigName = "AstraMod_Config.json";
            }
        }
        catch (NoSuchFileException e3) {
            try (final BufferedWriter writer = Files.newBufferedWriter(config, new OpenOption[0])) {
                writer.write("AstraMod_Config.json");
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
        return kamiConfigName;
    }
    
    public static void loadConfiguration() {
        try {
            loadConfigurationUnsafe();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void loadConfigurationUnsafe() throws IOException {
        final String kamiConfigName = getConfigName();
        final Path kamiConfig = Paths.get(kamiConfigName, new String[0]);
        if (!Files.exists(kamiConfig, new LinkOption[0])) {
            return;
        }
        Configuration.loadConfiguration(kamiConfig);
        final JsonObject gui = KamiMod.INSTANCE.guiStateSetting.getValue();
        for (final Map.Entry<String, JsonElement> entry : gui.entrySet()) {
            final Optional<Component> optional = KamiMod.INSTANCE.guiManager.getChildren().stream().filter(component -> component instanceof Frame).filter(component -> component.getTitle().equals(entry.getKey())).findFirst();
            if (optional.isPresent()) {
                final JsonObject object = entry.getValue().getAsJsonObject();
                final Frame frame = optional.get();
                frame.setX(object.get("x").getAsInt());
                frame.setY(object.get("y").getAsInt());
                final Docking docking = Docking.values()[object.get("docking").getAsInt()];
                if (docking.isLeft()) {
                    ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.LEFT);
                }
                else if (docking.isRight()) {
                    ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.RIGHT);
                }
                else if (docking.isCenterVertical()) {
                    ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.CENTER);
                }
                frame.setDocking(docking);
                frame.setMinimized(object.get("minimized").getAsBoolean());
                frame.setPinned(object.get("pinned").getAsBoolean());
            }
            else {
                System.err.println("Found GUI config entry for " + entry.getKey() + ", but found no frame with that name");
            }
        }
        getInstance().getGuiManager().getChildren().stream().filter(component -> component instanceof Frame && component.isPinneable() && component.isVisible()).forEach(component -> component.setOpacity(0.0f));
    }
    
    public static void saveConfiguration() {
        try {
            saveConfigurationUnsafe();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveConfigurationUnsafe() throws IOException {
        final JsonObject object = new JsonObject();
        final JsonObject jsonObject = null;
        final JsonObject frameObject;
        final JsonObject jsonObject2;
        KamiMod.INSTANCE.guiManager.getChildren().stream().filter(component -> component instanceof Frame).map(component -> component).forEach(frame -> {
            frameObject = new JsonObject();
            frameObject.add("x", (JsonElement)new JsonPrimitive((Number)frame.getX()));
            frameObject.add("y", (JsonElement)new JsonPrimitive((Number)frame.getY()));
            frameObject.add("docking", (JsonElement)new JsonPrimitive((Number)Arrays.asList(Docking.values()).indexOf(frame.getDocking())));
            frameObject.add("minimized", (JsonElement)new JsonPrimitive(Boolean.valueOf(frame.isMinimized())));
            frameObject.add("pinned", (JsonElement)new JsonPrimitive(Boolean.valueOf(frame.isPinned())));
            jsonObject2.add(frame.getTitle(), (JsonElement)frameObject);
            return;
        });
        KamiMod.INSTANCE.guiStateSetting.setValue(object);
        final Path outputFile = Paths.get(getConfigName(), new String[0]);
        if (!Files.exists(outputFile, new LinkOption[0])) {
            Files.createFile(outputFile, (FileAttribute<?>[])new FileAttribute[0]);
        }
        Configuration.saveConfiguration(outputFile);
        ModuleManager.getModules().forEach(Module::destroy);
    }
    
    public static boolean isFilenameValid(final String file) {
        final File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }
    
    public static KamiMod getInstance() {
        return KamiMod.INSTANCE;
    }
    
    public KamiGUI getGuiManager() {
        return this.guiManager;
    }
    
    public CommandManager getCommandManager() {
        return this.commandManager;
    }
    
    static {
        log = LogManager.getLogger("astraMod");
        EVENT_BUS = new EventManager();
    }
}
