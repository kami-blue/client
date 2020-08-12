// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.chat;

import java.util.function.Predicate;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketChatMessage;
import java.util.Base64;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "xHamster-chat", category = Category.CHAT, description = "Modifies your chat messages")
public class xHamsterchat extends Module
{
    private /* synthetic */ Setting<Boolean> commands;
    @EventHandler
    public /* synthetic */ Listener<PacketEvent.Send> listener;
    private static final /* synthetic */ String[] llIIlIIlIlI;
    private static final /* synthetic */ int[] llIIllIlIlI;
    
    private static String lIIIIlIlIIIll(final String llllllllllllllllIllIIllllIlllllI, final String llllllllllllllllIllIIllllIllllll) {
        try {
            final SecretKeySpec llllllllllllllllIllIIlllllIIIIll = new SecretKeySpec(Arrays.copyOf(MessageDigest.getInstance("MD5").digest(llllllllllllllllIllIIllllIllllll.getBytes(StandardCharsets.UTF_8)), xHamsterchat.llIIllIlIlI[6]), "DES");
            final Cipher llllllllllllllllIllIIlllllIIIIlI = Cipher.getInstance("DES");
            llllllllllllllllIllIIlllllIIIIlI.init(xHamsterchat.llIIllIlIlI[2], llllllllllllllllIllIIlllllIIIIll);
            return new String(llllllllllllllllIllIIlllllIIIIlI.doFinal(Base64.getDecoder().decode(llllllllllllllllIllIIllllIlllllI.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }
        catch (Exception llllllllllllllllIllIIlllllIIIIIl) {
            llllllllllllllllIllIIlllllIIIIIl.printStackTrace();
            return null;
        }
    }
    
    public xHamsterchat() {
        this.commands = this.register(Settings.b(xHamsterchat.llIIlIIlIlI[xHamsterchat.llIIllIlIlI[0]], (boolean)(xHamsterchat.llIIllIlIlI[0] != 0)));
        String llllllllllllllllIllIIllllllIlIlI;
        String llllllllllllllllIllIIllllllIlIlI2;
        this.listener = new Listener<PacketEvent.Send>(llllllllllllllllIllIIllllllIIlll -> {
            if (lIIIIllIlIlIl((llllllllllllllllIllIIllllllIIlll.getPacket() instanceof CPacketChatMessage) ? 1 : 0)) {
                llllllllllllllllIllIIllllllIlIlI = ((CPacketChatMessage)llllllllllllllllIllIIllllllIIlll.getPacket()).func_149439_c();
                if (!lIIIIllIlIlIl(llllllllllllllllIllIIllllllIlIlI.startsWith(xHamsterchat.llIIlIIlIlI[xHamsterchat.llIIllIlIlI[2]]) ? 1 : 0) || !lIIIIllIlIllI(((boolean)this.commands.getValue()) ? 1 : 0)) {
                    llllllllllllllllIllIIllllllIlIlI2 = String.valueOf(new StringBuilder().append(llllllllllllllllIllIIllllllIlIlI).append(xHamsterchat.llIIlIIlIlI[xHamsterchat.llIIllIlIlI[3]]));
                    if (lIIIIllIlIlll(llllllllllllllllIllIIllllllIlIlI2.length(), xHamsterchat.llIIllIlIlI[4])) {
                        llllllllllllllllIllIIllllllIlIlI2 = llllllllllllllllIllIIllllllIlIlI2.substring(xHamsterchat.llIIllIlIlI[0], xHamsterchat.llIIllIlIlI[4]);
                    }
                    ((CPacketChatMessage)llllllllllllllllIllIIllllllIIlll.getPacket()).field_149440_a = llllllllllllllllIllIIllllllIlIlI2;
                }
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[xHamsterchat.llIIllIlIlI[0]]);
    }
    
    private static void lIIIIlIlIIlII() {
        (llIIlIIlIlI = new String[xHamsterchat.llIIllIlIlI[5]])[xHamsterchat.llIIllIlIlI[0]] = lIIIIlIlIIIlI("LCs4ByABICY=", "oDUjA");
        xHamsterchat.llIIlIIlIlI[xHamsterchat.llIIllIlIlI[1]] = lIIIIlIlIIIll("Z+sJwG4ludvcnAmLo7Aurw==", "hoJSF");
        xHamsterchat.llIIlIIlIlI[xHamsterchat.llIIllIlIlI[2]] = lIIIIlIlIIIlI("aw==", "DXDsw");
        xHamsterchat.llIIlIIlIlI[xHamsterchat.llIIllIlIlI[3]] = lIIIIlIlIIIll("rbVmfRtbwnjDQOcKMJ1IJA==", "GBnXj");
    }
    
    private static boolean lIIIIllIllIII(final int llllllllllllllllIllIIllllIllIIlI, final int llllllllllllllllIllIIllllIllIIII) {
        return llllllllllllllllIllIIllllIllIIlI < llllllllllllllllIllIIllllIllIIII;
    }
    
    private static boolean lIIIIllIlIlIl(final int llllllllllllllllIllIIllllIlIllII) {
        return llllllllllllllllIllIIllllIlIllII != 0;
    }
    
    private static boolean lIIIIllIlIlll(final int llllllllllllllllIllIIllllIllIlll, final int llllllllllllllllIllIIllllIllIllI) {
        return llllllllllllllllIllIIllllIllIlll >= llllllllllllllllIllIIllllIllIllI;
    }
    
    static {
        lIIIIllIlIlII();
        lIIIIlIlIIlII();
    }
    
    private static void lIIIIllIlIlII() {
        (llIIllIlIlI = new int[7])[0] = ((101 + 82 - 148 + 100 ^ 80 + 127 - 50 + 5) & (0x45 ^ 0xC ^ (0xDD ^ 0xB1) ^ -" ".length()));
        xHamsterchat.llIIllIlIlI[1] = " ".length();
        xHamsterchat.llIIllIlIlI[2] = "  ".length();
        xHamsterchat.llIIllIlIlI[3] = "   ".length();
        xHamsterchat.llIIllIlIlI[4] = (-(0xFFFFFBDF & 0x3E67) & (0xFFFFBF67 & 0x7BDE));
        xHamsterchat.llIIllIlIlI[5] = (0x77 ^ 0x73);
        xHamsterchat.llIIllIlIlI[6] = (0x5B ^ 0x53);
    }
    
    private static String lIIIIlIlIIIlI(String llllllllllllllllIllIIlllllIlIIII, final String llllllllllllllllIllIIlllllIIllll) {
        llllllllllllllllIllIIlllllIlIIII = new String(Base64.getDecoder().decode(llllllllllllllllIllIIlllllIlIIII.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        final StringBuilder llllllllllllllllIllIIlllllIlIIll = new StringBuilder();
        final char[] llllllllllllllllIllIIlllllIlIIlI = llllllllllllllllIllIIlllllIIllll.toCharArray();
        int llllllllllllllllIllIIlllllIlIIIl = xHamsterchat.llIIllIlIlI[0];
        final short llllllllllllllllIllIIlllllIIlIll = (Object)llllllllllllllllIllIIlllllIlIIII.toCharArray();
        final float llllllllllllllllIllIIlllllIIlIlI = llllllllllllllllIllIIlllllIIlIll.length;
        float llllllllllllllllIllIIlllllIIlIIl = xHamsterchat.llIIllIlIlI[0];
        while (lIIIIllIllIII((int)llllllllllllllllIllIIlllllIIlIIl, (int)llllllllllllllllIllIIlllllIIlIlI)) {
            final char llllllllllllllllIllIIlllllIlIllI = llllllllllllllllIllIIlllllIIlIll[llllllllllllllllIllIIlllllIIlIIl];
            llllllllllllllllIllIIlllllIlIIll.append((char)(llllllllllllllllIllIIlllllIlIllI ^ llllllllllllllllIllIIlllllIlIIlI[llllllllllllllllIllIIlllllIlIIIl % llllllllllllllllIllIIlllllIlIIlI.length]));
            "".length();
            ++llllllllllllllllIllIIlllllIlIIIl;
            ++llllllllllllllllIllIIlllllIIlIIl;
            "".length();
            if (null != null) {
                return null;
            }
        }
        return String.valueOf(llllllllllllllllIllIIlllllIlIIll);
    }
    
    private static boolean lIIIIllIlIllI(final int llllllllllllllllIllIIllllIlIIlll) {
        return llllllllllllllllIllIIllllIlIIlll == 0;
    }
}
