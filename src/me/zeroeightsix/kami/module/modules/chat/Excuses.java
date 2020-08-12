// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.chat;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.security.MessageDigest;
import java.util.function.Predicate;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiGameOver;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Excuses", description = "Cry!", category = Category.CHAT)
public class Excuses extends Module
{
    private /* synthetic */ Setting<Boolean> respawn;
    private static final /* synthetic */ String[] lIIlIIIll;
    private static final /* synthetic */ int[] lIIlIIlII;
    @EventHandler
    public /* synthetic */ Listener<GuiScreenEvent.Displayed> listener;
    
    private static void lIlIIIIlll() {
        (lIIlIIlII = new int[4])[0] = ((0x3 ^ 0x2B ^ (0x1C ^ 0x19)) & (0x9C ^ 0xAE ^ (0x4E ^ 0x51) ^ -" ".length()));
        Excuses.lIIlIIlII[1] = " ".length();
        Excuses.lIIlIIlII[2] = "  ".length();
        Excuses.lIIlIIlII[3] = (7 + 88 - 64 + 106 ^ 35 + 94 - 29 + 29);
    }
    
    private static String lIlIIIIlII(String lIllllllIIIlllI, final String lIllllllIIIllIl) {
        lIllllllIIIlllI = new String(Base64.getDecoder().decode(lIllllllIIIlllI.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        final StringBuilder lIllllllIIlIIIl = new StringBuilder();
        final char[] lIllllllIIlIIII = lIllllllIIIllIl.toCharArray();
        int lIllllllIIIllll = Excuses.lIIlIIlII[0];
        final String lIllllllIIIlIIl = (Object)lIllllllIIIlllI.toCharArray();
        final long lIllllllIIIlIII = lIllllllIIIlIIl.length;
        char lIllllllIIIIlll = (char)Excuses.lIIlIIlII[0];
        while (lIlIIIlIIl(lIllllllIIIIlll, (int)lIllllllIIIlIII)) {
            final char lIllllllIIlIlII = lIllllllIIIlIIl[lIllllllIIIIlll];
            lIllllllIIlIIIl.append((char)(lIllllllIIlIlII ^ lIllllllIIlIIII[lIllllllIIIllll % lIllllllIIlIIII.length]));
            "".length();
            ++lIllllllIIIllll;
            ++lIllllllIIIIlll;
            "".length();
            if ("   ".length() != "   ".length()) {
                return null;
            }
        }
        return String.valueOf(lIllllllIIlIIIl);
    }
    
    private static boolean lIlIIIlIII(final int lIlllllIlllIIll) {
        return lIlllllIlllIIll != 0;
    }
    
    private static boolean lIlIIIlIIl(final int lIlllllIlllIllI, final int lIlllllIlllIlIl) {
        return lIlllllIlllIllI < lIlllllIlllIlIl;
    }
    
    private static void lIlIIIIllI() {
        (lIIlIIIll = new String[Excuses.lIIlIIlII[2]])[Excuses.lIIlIIlII[0]] = lIlIIIIlII("BRYEIRMgHQ==", "WswQr");
        Excuses.lIIlIIIll[Excuses.lIIlIIlII[1]] = lIlIIIIlIl("dUZCvV+VfBb6ljmwioF05iooZQ10fHT6B3xVD8sQ87sbWyfN7qFZtg3n6Q4Ue+Ds", "TUgRs");
    }
    
    static {
        lIlIIIIlll();
        lIlIIIIllI();
    }
    
    public Excuses() {
        this.respawn = this.register(Settings.b(Excuses.lIIlIIIll[Excuses.lIIlIIlII[0]], (boolean)(Excuses.lIIlIIlII[1] != 0)));
        this.listener = new Listener<GuiScreenEvent.Displayed>(lIllllllIlIIIII -> {
            if (lIlIIIlIII((lIllllllIlIIIII.getScreen() instanceof GuiGameOver) ? 1 : 0) && lIlIIIlIII(((boolean)this.respawn.getValue()) ? 1 : 0)) {
                Excuses.mc.field_71439_g.func_71004_bE();
                Excuses.mc.func_147108_a((GuiScreen)null);
                Excuses.mc.field_71439_g.func_71165_d(Excuses.lIIlIIIll[Excuses.lIIlIIlII[1]]);
            }
        }, (Predicate<GuiScreenEvent.Displayed>[])new Predicate[Excuses.lIIlIIlII[0]]);
    }
    
    private static String lIlIIIIlIl(final String lIlllllIlllllII, final String lIlllllIlllllIl) {
        try {
            final SecretKeySpec lIllllllIIIIIIl = new SecretKeySpec(Arrays.copyOf(MessageDigest.getInstance("MD5").digest(lIlllllIlllllIl.getBytes(StandardCharsets.UTF_8)), Excuses.lIIlIIlII[3]), "DES");
            final Cipher lIllllllIIIIIII = Cipher.getInstance("DES");
            lIllllllIIIIIII.init(Excuses.lIIlIIlII[2], lIllllllIIIIIIl);
            return new String(lIllllllIIIIIII.doFinal(Base64.getDecoder().decode(lIlllllIlllllII.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }
        catch (Exception lIlllllIlllllll) {
            lIlllllIlllllll.printStackTrace();
            return null;
        }
    }
}
