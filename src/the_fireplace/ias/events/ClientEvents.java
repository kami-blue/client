// 
// Decompiled by Procyon v0.5.36
// 

package the_fireplace.ias.events;

import the_fireplace.ias.IAS;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import the_fireplace.ias.gui.GuiAccountSelector;
import net.minecraft.client.Minecraft;
import com.github.mrebhan.ingameaccountswitcher.tools.Config;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiScreen;
import the_fireplace.ias.gui.GuiButtonWithImage;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent;

public class ClientEvents
{
    @SubscribeEvent
    public void guiEvent(final GuiScreenEvent.InitGuiEvent.Post event) {
        final GuiScreen gui = event.getGui();
        if (gui instanceof GuiMainMenu) {
            event.getButtonList().add(new GuiButtonWithImage(20, gui.field_146294_l / 2 + 104, gui.field_146295_m / 4 + 48 + 72 + 12, 20, 20, ""));
        }
    }
    
    @SubscribeEvent
    public void onClick(final GuiScreenEvent.ActionPerformedEvent event) {
        if (event.getGui() instanceof GuiMainMenu && event.getButton().field_146127_k == 20) {
            if (Config.getInstance() == null) {
                Config.load();
            }
            Minecraft.func_71410_x().func_147108_a((GuiScreen)new GuiAccountSelector());
        }
    }
    
    @SubscribeEvent
    public void onTick(final TickEvent.RenderTickEvent t) {
        final GuiScreen screen = Minecraft.func_71410_x().field_71462_r;
        if (screen instanceof GuiMainMenu) {
            screen.func_73732_a(Minecraft.func_71410_x().field_71466_p, I18n.func_135052_a("ias.loggedinas", new Object[0]) + Minecraft.func_71410_x().func_110432_I().func_111285_a() + ".", screen.field_146294_l / 2, screen.field_146295_m / 4 + 48 + 72 + 12 + 22, -3372920);
        }
        else if (screen instanceof GuiMultiplayer && Minecraft.func_71410_x().func_110432_I().func_148254_d().equals("0")) {
            screen.func_73732_a(Minecraft.func_71410_x().field_71466_p, I18n.func_135052_a("ias.offlinemode", new Object[0]), screen.field_146294_l / 2, 10, 16737380);
        }
    }
    
    @SubscribeEvent
    public void configChanged(final ConfigChangedEvent event) {
        if (event.getModID().equals("ias")) {
            IAS.syncConfig();
        }
    }
}
