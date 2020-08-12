// 
// Decompiled by Procyon v0.5.36
// 

package ninja.genuine.tooltips;

import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiScreen;
import ninja.genuine.tooltips.client.gui.GuiConfigTooltips;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import ninja.genuine.utils.ModUtils;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import ninja.genuine.tooltips.client.config.Config;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.settings.KeyBinding;
import ninja.genuine.tooltips.client.render.TooltipEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(modid = "worldtooltips", name = "World Tooltips", version = "1.3.0", canBeDeactivated = true, clientSideOnly = true, updateJSON = "http://genuine.ninja/worldtooltips/update.json", useMetadata = true, guiFactory = "ninja.genuine.tooltips.client.TooltipsGuiFactory", acceptedMinecraftVersions = "[1.12.2]")
public class WorldTooltips
{
    @Mod.Instance("worldtooltips")
    public static WorldTooltips instance;
    private TooltipEvent events;
    private KeyBinding configKey;
    
    public WorldTooltips() {
        this.events = new TooltipEvent();
        this.configKey = new KeyBinding("World Tooltips Configuration", 74, "World Tooltips");
        WorldTooltips.instance = this;
    }
    
    @Mod.EventHandler
    public void pre(final FMLPreInitializationEvent event) {
        final Configuration cfg = new Configuration(event.getSuggestedConfigurationFile(), "1.3.0");
        Config.setConfiguration(cfg);
        Config.populate();
        Config.save();
        ClientRegistry.registerKeyBinding(this.configKey);
    }
    
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register((Object)this);
        MinecraftForge.EVENT_BUS.register((Object)this.events);
    }
    
    @Mod.EventHandler
    public void post(final FMLPostInitializationEvent event) {
        ModUtils.post();
    }
    
    @Mod.EventHandler
    public void disable(final FMLModDisabledEvent event) {
        MinecraftForge.EVENT_BUS.unregister((Object)this.events);
    }
    
    @SubscribeEvent
    public void keypress(final InputEvent.KeyInputEvent event) {
        if (this.configKey.func_151468_f()) {
            Minecraft.func_71410_x().func_147108_a((GuiScreen)new GuiConfigTooltips(Minecraft.func_71410_x().field_71462_r));
        }
    }
    
    @SubscribeEvent
    public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
        Config.save();
    }
}
