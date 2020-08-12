// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import java.awt.Desktop;
import java.net.URI;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Porn", description = "Opens my favorite pornographic website", category = Category.MISC)
public class OpenPorn extends Module
{
    public void onEnable() {
        try {
            final URI llllllllllllIlIlIllIIllIlIIlIlII = new URI("https://www.pornhub.com/view_video.php?viewkey=ph5b590847deea1");
            Desktop.getDesktop().browse(llllllllllllIlIlIllIIllIlIIlIlII);
            "".length();
            if ("  ".length() == ((0x12 ^ 0x53) & ~(0x32 ^ 0x73))) {
                return;
            }
        }
        catch (Exception llllllllllllIlIlIllIIllIlIIlIIll) {
            llllllllllllIlIlIllIIllIlIIlIIll.printStackTrace();
        }
        this.disable();
    }
}
