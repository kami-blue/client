// 
// Decompiled by Procyon v0.5.36
// 

package the_fireplace.ias.config;

import net.minecraftforge.common.config.ConfigElement;
import the_fireplace.ias.IAS;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

public class IASConfigGui extends GuiConfig
{
    public IASConfigGui(final GuiScreen parentScreen) {
        super(parentScreen, new ConfigElement(IAS.config.getCategory("general")).getChildElements(), "ias", false, false, GuiConfig.getAbridgedConfigPath(IAS.config.toString()));
    }
}
