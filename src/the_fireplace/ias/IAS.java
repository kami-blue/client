// 
// Decompiled by Procyon v0.5.36
// 

package the_fireplace.ias;

import the_fireplace.ias.tools.SkinTools;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import the_fireplace.ias.events.ClientEvents;
import net.minecraftforge.common.MinecraftForge;
import com.github.mrebhan.ingameaccountswitcher.MR;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import the_fireplace.iasencrypt.Standards;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import the_fireplace.ias.config.ConfigValues;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;

@Mod(modid = "ias", name = "In-Game Account Switcher", clientSideOnly = true, guiFactory = "the_fireplace.ias.config.IASGuiFactory", updateJSON = "http://thefireplace.bitnamiapp.com/jsons/ias.json", acceptedMinecraftVersions = "[1.11,)")
public class IAS
{
    public static Configuration config;
    private static Property CASESENSITIVE_PROPERTY;
    private static Property ENABLERELOG_PROPERTY;
    
    public static void syncConfig() {
        ConfigValues.CASESENSITIVE = IAS.CASESENSITIVE_PROPERTY.getBoolean();
        ConfigValues.ENABLERELOG = IAS.ENABLERELOG_PROPERTY.getBoolean();
        if (IAS.config.hasChanged()) {
            IAS.config.save();
        }
    }
    
    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        (IAS.config = new Configuration(event.getSuggestedConfigurationFile())).load();
        IAS.CASESENSITIVE_PROPERTY = IAS.config.get("general", "ias.cfg.casesensitive", false, I18n.func_135052_a("ias.cfg.casesensitive.tooltip", new Object[0]));
        IAS.ENABLERELOG_PROPERTY = IAS.config.get("general", "ias.cfg.enablerelog", false, I18n.func_135052_a("ias.cfg.enablerelog.tooltip", new Object[0]));
        syncConfig();
        if (!event.getModMetadata().version.equals("7.0.3")) {
            Standards.updateFolder();
        }
        else {
            System.out.println("Dev environment detected!");
        }
    }
    
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        MR.init();
        MinecraftForge.EVENT_BUS.register((Object)new ClientEvents());
        Standards.importAccounts();
    }
    
    @Mod.EventHandler
    public void postInit(final FMLPostInitializationEvent event) {
        SkinTools.cacheSkins();
    }
}
