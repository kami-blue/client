package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

/**
 * Created by 086 on 8/04/2018.
 */
@Module.Info(
        name = "ColourSign",
        description = "Allows ingame colouring of text on signs",
        category = Module.Category.MISC
)
public class ColourSign extends Module {

    @EventHandler
    public Listener<GuiScreenEvent.Displayed> eventListener = new Listener<>(event -> {
        if (event.getScreen() instanceof GuiEditSign && isEnabled()) {
            event.setScreen(new KamiGuiEditSign(((GuiEditSign) event.getScreen()).tileSign));
        }
    });

//    @EventHandler
//    public Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
//        if (event.getPacket() instanceof CPacketUpdateSign) {
//            String[] lines = ((CPacketUpdateSign) event.getPacket()).lines;
//            for (int i = 0; i < 4; i++) {
//                lines[i] = lines[i].replace(KamiMod.colour + "", KamiMod.colour + KamiMod.colour + "rr");
//            }
//        }
//    });

    private class KamiGuiEditSign extends GuiEditSign {


        public KamiGuiEditSign(TileEntitySign teSign) {
            super(teSign);
        }

        @Override
        public void initGui() {
            super.initGui();
        }

        @Override
        protected void actionPerformed(GuiButton button) throws IOException {
            if (button.id == 0) {
                this.tileSign.signText[this.editLine] = new TextComponentString(tileSign.signText[this.editLine].getFormattedText().replaceAll("(" + KamiMod.colour + ")(.)", "$1$1$2$2"));
            }
            super.actionPerformed(button);
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            super.keyTyped(typedChar, keyCode);
            String s = ((TextComponentString) tileSign.signText[this.editLine]).getText();
            s = s.replace("&", KamiMod.colour + "");
            tileSign.signText[this.editLine] = new TextComponentString(s);
        }

    }
}
