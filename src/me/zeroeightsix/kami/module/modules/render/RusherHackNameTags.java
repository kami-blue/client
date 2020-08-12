// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import net.minecraft.client.gui.ScaledResolution;
import java.util.Iterator;
import java.awt.Color;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import me.zeroeightsix.kami.setting.Settings;
import java.awt.Font;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemTool;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemArmor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import java.util.Base64;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.gui.font.CFontRenderer;
import me.zeroeightsix.kami.module.Module;

@Info(name = "RusherHackNameTags", category = Category.RENDER)
public class RusherHackNameTags extends Module
{
    private static final /* synthetic */ String[] llIlIIlI;
    /* synthetic */ CFontRenderer cFontRenderer;
    private static final /* synthetic */ int[] llIlIIll;
    private /* synthetic */ Setting<Boolean> Armor;
    
    private static boolean lllIIIlll(final int llIlIIIlIIIlIII) {
        return llIlIIIlIIIlIII >= 0;
    }
    
    private static String llIllllIl(final String llIlIIIlIIllIll, final String llIlIIIlIIlllII) {
        try {
            final SecretKeySpec llIlIIIlIlIIIII = new SecretKeySpec(Arrays.copyOf(MessageDigest.getInstance("MD5").digest(llIlIIIlIIlllII.getBytes(StandardCharsets.UTF_8)), RusherHackNameTags.llIlIIll[19]), "DES");
            final Cipher llIlIIIlIIlllll = Cipher.getInstance("DES");
            llIlIIIlIIlllll.init(RusherHackNameTags.llIlIIll[3], llIlIIIlIlIIIII);
            return new String(llIlIIIlIIlllll.doFinal(Base64.getDecoder().decode(llIlIIIlIIllIll.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }
        catch (Exception llIlIIIlIIllllI) {
            llIlIIIlIIllllI.printStackTrace();
            return null;
        }
    }
    
    private static void lllIIIIII() {
        (llIlIIll = new int[34])[0] = ((69 + 71 - 83 + 77 ^ 132 + 115 - 91 + 12) & (0x16 ^ 0x26 ^ (0x20 ^ 0x3E) ^ -" ".length()));
        RusherHackNameTags.llIlIIll[1] = (0x58 ^ 0x4A);
        RusherHackNameTags.llIlIIll[2] = " ".length();
        RusherHackNameTags.llIlIIll[3] = "  ".length();
        RusherHackNameTags.llIlIIll[4] = "   ".length();
        RusherHackNameTags.llIlIIll[5] = (0x92 ^ 0x96);
        RusherHackNameTags.llIlIIll[6] = (0xBB ^ 0xBE);
        RusherHackNameTags.llIlIIll[7] = (0xFFFFAFFD & 0x5B73);
        RusherHackNameTags.llIlIIll[8] = (0xB0 ^ 0x9E) + (0x3 ^ 0x6B) - (0x3E ^ 0x63) + (0x63 ^ 0x2A);
        RusherHackNameTags.llIlIIll[9] = (-(0xFFFF9E0D & 0x63F3) & (0xFFFFFFFD & 0x837F7F));
        RusherHackNameTags.llIlIIll[10] = -" ".length();
        RusherHackNameTags.llIlIIll[11] = -(0x91 ^ 0x9B);
        RusherHackNameTags.llIlIIll[12] = (-(0x6F ^ 0x70) & (0xFFFFCFFF & 0x3DFF));
        RusherHackNameTags.llIlIIll[13] = (-(0xFFFFF119 & 0x7EE7) & (0xFFFFFBE2 & 0x7FFF));
        RusherHackNameTags.llIlIIll[14] = (0xFFFFDBFC & 0x2503);
        RusherHackNameTags.llIlIIll[15] = (0x51 ^ 0x3C ^ (0x2A ^ 0x4B));
        RusherHackNameTags.llIlIIll[16] = (0x15 ^ 0xD);
        RusherHackNameTags.llIlIIll[17] = -(0x7F ^ 0x7A);
        RusherHackNameTags.llIlIIll[18] = (0xE ^ 0x8);
        RusherHackNameTags.llIlIIll[19] = (0x62 ^ 0x6A);
        RusherHackNameTags.llIlIIll[20] = (0xEF ^ 0xA3 ^ (0x1D ^ 0x4B));
        RusherHackNameTags.llIlIIll[21] = (0xD2 ^ 0xAA ^ 83 + 106 - 77 + 15);
        RusherHackNameTags.llIlIIll[22] = (((0x0 ^ 0x2E) & ~(0x78 ^ 0x56)) ^ (0x5 ^ 0xE));
        RusherHackNameTags.llIlIIll[23] = (0x14 ^ 0x2B ^ (0x6C ^ 0x5E));
        RusherHackNameTags.llIlIIll[24] = (0xFFFF97EF & 0x6B12);
        RusherHackNameTags.llIlIIll[25] = (0xFFFFB7F3 & 0x4B0F);
        RusherHackNameTags.llIlIIll[26] = (0xFFFF8F27 & 0x7BF8);
        RusherHackNameTags.llIlIIll[27] = (-(0xFFFFF607 & 0x2BFD) & (0xFFFFEED7 & 0x3F7E));
        RusherHackNameTags.llIlIIll[28] = (-(0xFFFFE8FB & 0x3FFE) & (0xFFFFBDFF & 0x7BFB));
        RusherHackNameTags.llIlIIll[29] = (0xFFFF8C77 & 0x7FDB);
        RusherHackNameTags.llIlIIll[30] = (0xFFFFD9CD & 0x3732);
        RusherHackNameTags.llIlIIll[31] = 37 + 99 - 46 + 165;
        RusherHackNameTags.llIlIIll[32] = (199 + 189 - 334 + 158 ^ 43 + 92 - 12 + 73);
        RusherHackNameTags.llIlIIll[33] = (0x5F ^ 0x56);
    }
    
    static {
        lllIIIIII();
        llIllllll();
    }
    
    private void disableGL2D() {
        GL11.glEnable(RusherHackNameTags.llIlIIll[12]);
        GL11.glDisable(RusherHackNameTags.llIlIIll[13]);
        GL11.glDisable(RusherHackNameTags.llIlIIll[26]);
        GL11.glHint(RusherHackNameTags.llIlIIll[27], RusherHackNameTags.llIlIIll[30]);
        GL11.glHint(RusherHackNameTags.llIlIIll[29], RusherHackNameTags.llIlIIll[30]);
    }
    
    private void renderItem(final EntityPlayer llIlIIlIIllIlII, final ItemStack llIlIIlIIlllIII, final int llIlIIlIIllIIlI, final int llIlIIlIIllIllI) {
        GL11.glPushMatrix();
        GL11.glDepthMask((boolean)(RusherHackNameTags.llIlIIll[2] != 0));
        GlStateManager.func_179086_m(RusherHackNameTags.llIlIIll[14]);
        GlStateManager.func_179097_i();
        GlStateManager.func_179126_j();
        RenderHelper.func_74519_b();
        RusherHackNameTags.mc.func_175599_af().field_77023_b = -100.0f;
        GlStateManager.func_179152_a(1.0f, 1.0f, 0.01f);
        RusherHackNameTags.mc.func_175599_af().func_180450_b(llIlIIlIIlllIII, llIlIIlIIllIIlI, llIlIIlIIllIllI / RusherHackNameTags.llIlIIll[3] - RusherHackNameTags.llIlIIll[15]);
        RusherHackNameTags.mc.func_175599_af().func_175030_a(RusherHackNameTags.mc.field_71466_p, llIlIIlIIlllIII, llIlIIlIIllIIlI, llIlIIlIIllIllI / RusherHackNameTags.llIlIIll[3] - RusherHackNameTags.llIlIIll[15]);
        RusherHackNameTags.mc.func_175599_af().field_77023_b = 0.0f;
        GlStateManager.func_179152_a(1.0f, 1.0f, 1.0f);
        RenderHelper.func_74518_a();
        GlStateManager.func_179141_d();
        GlStateManager.func_179084_k();
        GlStateManager.func_179140_f();
        GlStateManager.func_179139_a(0.5, 0.5, 0.5);
        GlStateManager.func_179097_i();
        this.renderEnchantText(llIlIIlIIllIlII, llIlIIlIIlllIII, llIlIIlIIllIIlI, llIlIIlIIllIllI - RusherHackNameTags.llIlIIll[1]);
        GlStateManager.func_179126_j();
        GlStateManager.func_179152_a(2.0f, 2.0f, 2.0f);
        GL11.glPopMatrix();
    }
    
    private void renderEnchantText(final EntityPlayer llIlIIlIIIllllI, final ItemStack llIlIIlIIIlIllI, final int llIlIIlIIIlIlIl, final int llIlIIlIIIlIlII) {
        int llIlIIlIIIllIlI = llIlIIlIIIlIlII - RusherHackNameTags.llIlIIll[16];
        int llIlIIlIIIllIIl = llIlIIlIIIllIlI - RusherHackNameTags.llIlIIll[17];
        if (!lllIIIIll((llIlIIlIIIlIllI.func_77973_b() instanceof ItemArmor) ? 1 : 0) || !lllIIIIll((llIlIIlIIIlIllI.func_77973_b() instanceof ItemSword) ? 1 : 0) || lllIIIIIl((llIlIIlIIIlIllI.func_77973_b() instanceof ItemTool) ? 1 : 0)) {
            this.cFontRenderer.drawStringWithShadow(String.valueOf(new StringBuilder().append(llIlIIlIIIlIllI.func_77958_k() - llIlIIlIIIlIllI.func_77952_i()).append(RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[18]])), llIlIIlIIIlIlIl * RusherHackNameTags.llIlIIll[3] + RusherHackNameTags.llIlIIll[19], llIlIIlIIIlIlII + RusherHackNameTags.llIlIIll[20], RusherHackNameTags.llIlIIll[10]);
            "".length();
        }
        final NBTTagList llIlIIlIIIllIII = llIlIIlIIIlIllI.func_77986_q();
        int llIlIIlIIlIIIII = RusherHackNameTags.llIlIIll[0];
        while (lllIIlIII(llIlIIlIIlIIIII, llIlIIlIIIllIII.func_74745_c())) {
            final short llIlIIlIIlIIIll = llIlIIlIIIllIII.func_150305_b(llIlIIlIIlIIIII).func_74765_d(RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[21]]);
            final short llIlIIlIIlIIIlI = llIlIIlIIIllIII.func_150305_b(llIlIIlIIlIIIII).func_74765_d(RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[19]]);
            final Enchantment llIlIIlIIlIIIIl = Enchantment.func_185262_c((int)llIlIIlIIlIIIll);
            if (lllIIIllI(llIlIIlIIlIIIIl)) {
                String s;
                if (lllIIIIIl(llIlIIlIIlIIIIl.func_190936_d() ? 1 : 0)) {
                    s = String.valueOf(new StringBuilder().append(TextFormatting.RED).append(llIlIIlIIlIIIIl.func_77316_c((int)llIlIIlIIlIIIlI).substring(RusherHackNameTags.llIlIIll[22]).substring(RusherHackNameTags.llIlIIll[0], RusherHackNameTags.llIlIIll[2]).toLowerCase()));
                    "".length();
                    if ((0xF9 ^ 0x85 ^ (0xA ^ 0x72)) < ((0x2F ^ 0x1F ^ (0x13 ^ 0x68)) & (0x3B ^ 0x15 ^ (0xF3 ^ 0x96) ^ -" ".length()))) {
                        return;
                    }
                }
                else {
                    s = llIlIIlIIlIIIIl.func_77316_c((int)llIlIIlIIlIIIlI).substring(RusherHackNameTags.llIlIIll[0], RusherHackNameTags.llIlIIll[2]).toLowerCase();
                }
                String llIlIIlIIlIIlII = s;
                llIlIIlIIlIIlII = String.valueOf(new StringBuilder().append(llIlIIlIIlIIlII).append(llIlIIlIIlIIIlI));
                GL11.glPushMatrix();
                GL11.glScalef(0.9f, 0.9f, 0.0f);
                this.cFontRenderer.drawStringWithShadow(llIlIIlIIlIIlII, llIlIIlIIIlIlIl * RusherHackNameTags.llIlIIll[3] + RusherHackNameTags.llIlIIll[23], llIlIIlIIIllIIl, RusherHackNameTags.llIlIIll[10]);
                "".length();
                GL11.glScalef(1.0f, 1.0f, 1.0f);
                GL11.glPopMatrix();
                llIlIIlIIIllIlI += 8;
                llIlIIlIIIllIIl -= 10;
            }
            ++llIlIIlIIlIIIII;
            "".length();
            if (-(0x0 ^ 0x5) >= 0) {
                return;
            }
        }
    }
    
    private static boolean lllIIIIIl(final int llIlIIIlIIIllII) {
        return llIlIIIlIIIllII != 0;
    }
    
    private static boolean lllIIIllI(final Object llIlIIIlIIIlllI) {
        return llIlIIIlIIIlllI != null;
    }
    
    private void fakeGuiRect(double llIlIIIllIlIlII, double llIlIIIllIlIIll, double llIlIIIllIlIIlI, double llIlIIIllIlIIIl, final int llIlIIIllIllIll) {
        if (lllIIlIlI(lllIIlIIl(llIlIIIllIlIlII, llIlIIIllIlIIlI))) {
            final double llIlIIIlllIIIlI = llIlIIIllIlIlII;
            llIlIIIllIlIlII = llIlIIIllIlIIlI;
            llIlIIIllIlIIlI = llIlIIIlllIIIlI;
        }
        if (lllIIlIlI(lllIIlIIl(llIlIIIllIlIIll, llIlIIIllIlIIIl))) {
            final double llIlIIIlllIIIIl = llIlIIIllIlIIll;
            llIlIIIllIlIIll = llIlIIIllIlIIIl;
            llIlIIIllIlIIIl = llIlIIIlllIIIIl;
        }
        final float llIlIIIllIllIlI = (llIlIIIllIllIll >> RusherHackNameTags.llIlIIll[16] & RusherHackNameTags.llIlIIll[31]) / 255.0f;
        final float llIlIIIllIllIIl = (llIlIIIllIllIll >> RusherHackNameTags.llIlIIll[32] & RusherHackNameTags.llIlIIll[31]) / 255.0f;
        final float llIlIIIllIllIII = (llIlIIIllIllIll >> RusherHackNameTags.llIlIIll[19] & RusherHackNameTags.llIlIIll[31]) / 255.0f;
        final float llIlIIIllIlIlll = (llIlIIIllIllIll & RusherHackNameTags.llIlIIll[31]) / 255.0f;
        final Tessellator llIlIIIllIlIllI = Tessellator.func_178181_a();
        final BufferBuilder llIlIIIllIlIlIl = llIlIIIllIlIllI.func_178180_c();
        GlStateManager.func_179147_l();
        GlStateManager.func_179090_x();
        GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.func_179131_c(llIlIIIllIllIIl, llIlIIIllIllIII, llIlIIIllIlIlll, llIlIIIllIllIlI);
        llIlIIIllIlIlIl.func_181668_a(RusherHackNameTags.llIlIIll[21], DefaultVertexFormats.field_181705_e);
        llIlIIIllIlIlIl.func_181662_b(llIlIIIllIlIlII, llIlIIIllIlIIIl, 0.0).func_181675_d();
        llIlIIIllIlIlIl.func_181662_b(llIlIIIllIlIIlI, llIlIIIllIlIIIl, 0.0).func_181675_d();
        llIlIIIllIlIlIl.func_181662_b(llIlIIIllIlIIlI, llIlIIIllIlIIll, 0.0).func_181675_d();
        llIlIIIllIlIlIl.func_181662_b(llIlIIIllIlIlII, llIlIIIllIlIIll, 0.0).func_181675_d();
        llIlIIIllIlIllI.func_78381_a();
        GlStateManager.func_179098_w();
        GlStateManager.func_179084_k();
    }
    
    private static boolean lllIIIIlI(final Object llIlIIIlIIlIIIl, final Object llIlIIIlIIlIIII) {
        return llIlIIIlIIlIIIl != llIlIIIlIIlIIII;
    }
    
    private static String llIlllllI(String llIlIIIlIlIllIl, final String llIlIIIlIlIllII) {
        llIlIIIlIlIllIl = new String(Base64.getDecoder().decode(llIlIIIlIlIllIl.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        final StringBuilder llIlIIIlIllIIII = new StringBuilder();
        final char[] llIlIIIlIlIllll = llIlIIIlIlIllII.toCharArray();
        int llIlIIIlIlIlllI = RusherHackNameTags.llIlIIll[0];
        final int llIlIIIlIlIlIII = (Object)llIlIIIlIlIllIl.toCharArray();
        final char llIlIIIlIlIIlll = (char)llIlIIIlIlIlIII.length;
        Exception llIlIIIlIlIIllI = (Exception)RusherHackNameTags.llIlIIll[0];
        while (lllIIlIII((int)llIlIIIlIlIIllI, llIlIIIlIlIIlll)) {
            final char llIlIIIlIllIIll = llIlIIIlIlIlIII[llIlIIIlIlIIllI];
            llIlIIIlIllIIII.append((char)(llIlIIIlIllIIll ^ llIlIIIlIlIllll[llIlIIIlIlIlllI % llIlIIIlIlIllll.length]));
            "".length();
            ++llIlIIIlIlIlllI;
            ++llIlIIIlIlIIllI;
            "".length();
            if ((28 + 46 + 22 + 46 ^ 24 + 85 - 87 + 117) <= 0) {
                return null;
            }
        }
        return String.valueOf(llIlIIIlIllIIII);
    }
    
    private static boolean lllIIIlIl(final int llIlIIIlIIIIlII) {
        return llIlIIIlIIIIlII <= 0;
    }
    
    private void drawGuiRect(final double llIlIIIllllIllI, final double llIlIIIlllllllI, final double llIlIIIllllIlII, final double llIlIIIllllllII, final int llIlIIIlllllIll) {
        final float llIlIIIlllllIlI = (llIlIIIlllllIll >> RusherHackNameTags.llIlIIll[16] & RusherHackNameTags.llIlIIll[31]) / 255.0f;
        final float llIlIIIlllllIIl = (llIlIIIlllllIll >> RusherHackNameTags.llIlIIll[32] & RusherHackNameTags.llIlIIll[31]) / 255.0f;
        final float llIlIIIlllllIII = (llIlIIIlllllIll >> RusherHackNameTags.llIlIIll[19] & RusherHackNameTags.llIlIIll[31]) / 255.0f;
        final float llIlIIIllllIlll = (llIlIIIlllllIll & RusherHackNameTags.llIlIIll[31]) / 255.0f;
        GL11.glEnable(RusherHackNameTags.llIlIIll[13]);
        GL11.glDisable(RusherHackNameTags.llIlIIll[12]);
        GL11.glBlendFunc(RusherHackNameTags.llIlIIll[24], RusherHackNameTags.llIlIIll[25]);
        GL11.glEnable(RusherHackNameTags.llIlIIll[26]);
        GL11.glPushMatrix();
        GL11.glColor4f(llIlIIIlllllIIl, llIlIIIlllllIII, llIlIIIllllIlll, llIlIIIlllllIlI);
        GL11.glBegin(RusherHackNameTags.llIlIIll[21]);
        GL11.glVertex2d(llIlIIIllllIlII, llIlIIIlllllllI);
        GL11.glVertex2d(llIlIIIllllIllI, llIlIIIlllllllI);
        GL11.glVertex2d(llIlIIIllllIllI, llIlIIIllllllII);
        GL11.glVertex2d(llIlIIIllllIlII, llIlIIIllllllII);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(RusherHackNameTags.llIlIIll[12]);
        GL11.glDisable(RusherHackNameTags.llIlIIll[13]);
        GL11.glDisable(RusherHackNameTags.llIlIIll[26]);
    }
    
    private static void llIllllll() {
        (llIlIIlI = new String[RusherHackNameTags.llIlIIll[33]])[RusherHackNameTags.llIlIIll[0]] = llIllllII("/ef75KZIsk0=", "NCWBj");
        RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[2]] = llIllllIl("Y2h3JbZw3Bk=", "hVzKg");
        RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[3]] = llIllllII("3Iiu7qJ2Qfc=", "vlKEi");
        RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[4]] = llIllllII("WMBnl7z5rKs=", "dMjmo");
        RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[5]] = llIllllII("tR3CDzm5CWo=", "DVJIb");
        RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[6]] = llIllllIl("4ZKBrLyTgw0=", "yWOpD");
        RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[18]] = llIlllllI("w69w", "HDhKr");
        RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[21]] = llIllllII("PRRiD/IZH9U=", "YRbGI");
        RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[19]] = llIlllllI("HgYp", "rpEVn");
    }
    
    public void drawBorderRect(final float llIlIIlIllIIllI, final float llIlIIlIlIlllIl, final float llIlIIlIlIlllII, final float llIlIIlIllIIIll, final int llIlIIlIllIIIlI, final int llIlIIlIllIIIIl, final float llIlIIlIllIIIII) {
        this.drawGuiRect(llIlIIlIllIIllI + llIlIIlIllIIIII, llIlIIlIlIlllIl + llIlIIlIllIIIII, llIlIIlIlIlllII - llIlIIlIllIIIII, llIlIIlIllIIIll - llIlIIlIllIIIII, llIlIIlIllIIIIl);
        this.drawGuiRect(llIlIIlIllIIllI, llIlIIlIlIlllIl, llIlIIlIllIIllI + llIlIIlIllIIIII, llIlIIlIllIIIll, llIlIIlIllIIIlI);
        this.drawGuiRect(llIlIIlIllIIllI + llIlIIlIllIIIII, llIlIIlIlIlllIl, llIlIIlIlIlllII, llIlIIlIlIlllIl + llIlIIlIllIIIII, llIlIIlIllIIIlI);
        this.drawGuiRect(llIlIIlIllIIllI + llIlIIlIllIIIII, llIlIIlIllIIIll - llIlIIlIllIIIII, llIlIIlIlIlllII, llIlIIlIllIIIll, llIlIIlIllIIIlI);
        this.drawGuiRect(llIlIIlIlIlllII - llIlIIlIllIIIII, llIlIIlIlIlllIl + llIlIIlIllIIIII, llIlIIlIlIlllII, llIlIIlIllIIIll - llIlIIlIllIIIII, llIlIIlIllIIIlI);
    }
    
    public RusherHackNameTags() {
        this.cFontRenderer = new CFontRenderer(new Font(RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[0]], RusherHackNameTags.llIlIIll[0], RusherHackNameTags.llIlIIll[1]), (boolean)(RusherHackNameTags.llIlIIll[2] != 0), (boolean)(RusherHackNameTags.llIlIIll[0] != 0));
        this.Armor = this.register(Settings.b(RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[2]], (boolean)(RusherHackNameTags.llIlIIll[0] != 0)));
    }
    
    private void renderNametag(final EntityPlayer llIlIIllIIIlIII, final double llIlIIllIIIIlll, final double llIlIIllIIIIllI, final double llIlIIllIIIIlIl) {
        final int llIlIIllIIlIIII = RusherHackNameTags.llIlIIll[0];
        GL11.glPushMatrix();
        String llIlIIllIIIllll = String.valueOf(new StringBuilder().append(llIlIIllIIIlIII.func_70005_c_()).append(RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[4]]).append(MathHelper.func_76123_f(llIlIIllIIIlIII.func_110143_aJ() + llIlIIllIIIlIII.func_110139_bj())));
        llIlIIllIIIllll = llIlIIllIIIllll.replace(RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[5]], RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[6]]);
        final float llIlIIllIIIlllI = RusherHackNameTags.mc.field_71439_g.func_70032_d((Entity)llIlIIllIIIlIII);
        float n;
        if (lllIIIlIl(lllIIIlII(llIlIIllIIIlllI / 5.0f, 2.0f))) {
            n = 2.0f;
            "".length();
            if (((0x95 ^ 0xC3) & ~(0x4D ^ 0x1B)) >= " ".length()) {
                return;
            }
        }
        else {
            n = llIlIIllIIIlllI / 5.0f;
        }
        final float llIlIIllIIIllIl = n * 2.5f;
        final float llIlIIllIIIllII = 0.016666668f * this.getNametagSize((EntityLivingBase)llIlIIllIIIlIII);
        GL11.glTranslated((double)(float)llIlIIllIIIIlll, (float)llIlIIllIIIIllI + 2.5, (double)(float)llIlIIllIIIIlIl);
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GL11.glRotatef(-RusherHackNameTags.mc.func_175598_ae().field_78735_i, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(RusherHackNameTags.mc.func_175598_ae().field_78732_j, 1.0f, 0.0f, 0.0f);
        GL11.glScalef(-llIlIIllIIIllII, -llIlIIllIIIllII, llIlIIllIIIllII);
        GlStateManager.func_179140_f();
        GlStateManager.func_179132_a((boolean)(RusherHackNameTags.llIlIIll[0] != 0));
        GL11.glDisable(RusherHackNameTags.llIlIIll[7]);
        final int llIlIIllIIIlIll = this.cFontRenderer.getStringWidth(llIlIIllIIIllll) / RusherHackNameTags.llIlIIll[3];
        final double llIlIIlIlIIlllI = -llIlIIllIIIlIll - RusherHackNameTags.llIlIIll[3];
        final double llIlIIlIlIIllIl = 10.0;
        final double llIlIIlIlIIIlII = llIlIIllIIIlIll + RusherHackNameTags.llIlIIll[2];
        final double llIlIIlIlIIIIll = 20.0;
        final double llIlIIlIlIIIIlI = 0.0;
        int rgb;
        if (lllIIIIIl(Friends.isFriend(llIlIIllIIIllll) ? 1 : 0)) {
            rgb = new Color(RusherHackNameTags.llIlIIll[0], RusherHackNameTags.llIlIIll[8], RusherHackNameTags.llIlIIll[8]).getRGB();
            "".length();
            if (" ".length() != " ".length()) {
                return;
            }
        }
        else {
            rgb = RusherHackNameTags.llIlIIll[9];
        }
        this.drawBorderedRect(llIlIIlIlIIlllI, llIlIIlIlIIllIl, llIlIIlIlIIIlII, llIlIIlIlIIIIll, llIlIIlIlIIIIlI, rgb, RusherHackNameTags.llIlIIll[10]);
        this.cFontRenderer.drawString(llIlIIllIIIllll, (float)(-llIlIIllIIIlIll), 11.0f, RusherHackNameTags.llIlIIll[10]);
        "".length();
        int llIlIIllIIIlIlI = RusherHackNameTags.llIlIIll[0];
        final Iterator iterator = llIlIIllIIIlIII.field_71071_by.field_70460_b.iterator();
        while (lllIIIIIl(iterator.hasNext() ? 1 : 0)) {
            final ItemStack llIlIIllIIllIll = iterator.next();
            if (lllIIIllI(llIlIIllIIllIll)) {
                llIlIIllIIIlIlI -= 8;
            }
            "".length();
            if ("   ".length() < " ".length()) {
                return;
            }
        }
        if (lllIIIllI(llIlIIllIIIlIII.func_184614_ca())) {
            llIlIIllIIIlIlI -= 8;
            final Object llIlIIllIIllIlI = llIlIIllIIIlIII.func_184614_ca().func_77946_l();
            this.renderItem(llIlIIllIIIlIII, (ItemStack)llIlIIllIIllIlI, llIlIIllIIIlIlI, RusherHackNameTags.llIlIIll[11]);
            llIlIIllIIIlIlI += 16;
        }
        int llIlIIllIIlIlll = RusherHackNameTags.llIlIIll[4];
        while (lllIIIlll(llIlIIllIIlIlll)) {
            final ItemStack llIlIIllIIllIII = (ItemStack)llIlIIllIIIlIII.field_71071_by.field_70460_b.get(llIlIIllIIlIlll);
            if (lllIIIllI(llIlIIllIIllIII)) {
                final ItemStack llIlIIllIIllIIl = llIlIIllIIllIII.func_77946_l();
                this.renderItem(llIlIIllIIIlIII, llIlIIllIIllIIl, llIlIIllIIIlIlI, RusherHackNameTags.llIlIIll[11]);
                llIlIIllIIIlIlI += 16;
            }
            --llIlIIllIIlIlll;
            "".length();
            if ("   ".length() <= " ".length()) {
                return;
            }
        }
        if (lllIIIllI(llIlIIllIIIlIII.func_184592_cb())) {
            llIlIIllIIIlIlI += 0;
            final ItemStack llIlIIllIIlIllI = llIlIIllIIIlIII.func_184592_cb().func_77946_l();
            this.renderItem(llIlIIllIIIlIII, llIlIIllIIlIllI, llIlIIllIIIlIlI, RusherHackNameTags.llIlIIll[11]);
            llIlIIllIIIlIlI += 8;
        }
        GL11.glEnable(RusherHackNameTags.llIlIIll[12]);
        GL11.glEnable(RusherHackNameTags.llIlIIll[7]);
        GL11.glDepthMask((boolean)(RusherHackNameTags.llIlIIll[2] != 0));
        GL11.glDisable(RusherHackNameTags.llIlIIll[13]);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    private void drawBorderedRect(final double llIlIIlIlIIlllI, final double llIlIIlIlIIllIl, final double llIlIIlIlIIIlII, final double llIlIIlIlIIIIll, final double llIlIIlIlIIIIlI, final int llIlIIlIlIIlIIl, final int llIlIIlIlIIIIII) {
        this.enableGL2D();
        this.fakeGuiRect(llIlIIlIlIIlllI + llIlIIlIlIIIIlI, llIlIIlIlIIllIl + llIlIIlIlIIIIlI, llIlIIlIlIIIlII - llIlIIlIlIIIIlI, llIlIIlIlIIIIll - llIlIIlIlIIIIlI, llIlIIlIlIIlIIl);
        this.fakeGuiRect(llIlIIlIlIIlllI + llIlIIlIlIIIIlI, llIlIIlIlIIllIl, llIlIIlIlIIIlII - llIlIIlIlIIIIlI, llIlIIlIlIIllIl + llIlIIlIlIIIIlI, llIlIIlIlIIIIII);
        this.fakeGuiRect(llIlIIlIlIIlllI, llIlIIlIlIIllIl, llIlIIlIlIIlllI + llIlIIlIlIIIIlI, llIlIIlIlIIIIll, llIlIIlIlIIIIII);
        this.fakeGuiRect(llIlIIlIlIIIlII - llIlIIlIlIIIIlI, llIlIIlIlIIllIl, llIlIIlIlIIIlII, llIlIIlIlIIIIll, llIlIIlIlIIIIII);
        this.fakeGuiRect(llIlIIlIlIIlllI + llIlIIlIlIIIIlI, llIlIIlIlIIIIll - llIlIIlIlIIIIlI, llIlIIlIlIIIlII - llIlIIlIlIIIIlI, llIlIIlIlIIIIll, llIlIIlIlIIIIII);
        this.disableGL2D();
    }
    
    private static boolean lllIIIIll(final int llIlIIIlIIIlIlI) {
        return llIlIIIlIIIlIlI == 0;
    }
    
    private static boolean lllIIlIII(final int llIlIIIlIIlIlIl, final int llIlIIIlIIlIlII) {
        return llIlIIIlIIlIlIl < llIlIIIlIIlIlII;
    }
    
    private void enableGL2D() {
        GL11.glDisable(RusherHackNameTags.llIlIIll[7]);
        GL11.glEnable(RusherHackNameTags.llIlIIll[13]);
        GL11.glDisable(RusherHackNameTags.llIlIIll[12]);
        GL11.glBlendFunc(RusherHackNameTags.llIlIIll[24], RusherHackNameTags.llIlIIll[25]);
        GL11.glDepthMask((boolean)(RusherHackNameTags.llIlIIll[2] != 0));
        GL11.glEnable(RusherHackNameTags.llIlIIll[26]);
        GL11.glHint(RusherHackNameTags.llIlIIll[27], RusherHackNameTags.llIlIIll[28]);
        GL11.glHint(RusherHackNameTags.llIlIIll[29], RusherHackNameTags.llIlIIll[28]);
    }
    
    private float getNametagSize(final EntityLivingBase llIlIIlIlllIIlI) {
        final ScaledResolution llIlIIlIlllIlII = new ScaledResolution(RusherHackNameTags.mc);
        final double llIlIIlIlllIIll = llIlIIlIlllIlII.func_78325_e() / Math.pow(llIlIIlIlllIlII.func_78325_e(), 2.0);
        return (float)llIlIIlIlllIIll + RusherHackNameTags.mc.field_71439_g.func_70032_d((Entity)llIlIIlIlllIIlI) / 7.0f;
    }
    
    private static String llIllllII(final String llIlIIIllIIIIlI, final String llIlIIIllIIIIIl) {
        try {
            final SecretKeySpec llIlIIIllIIIlIl = new SecretKeySpec(MessageDigest.getInstance("MD5").digest(llIlIIIllIIIIIl.getBytes(StandardCharsets.UTF_8)), "Blowfish");
            final Cipher llIlIIIllIIIlII = Cipher.getInstance("Blowfish");
            llIlIIIllIIIlII.init(RusherHackNameTags.llIlIIll[3], llIlIIIllIIIlIl);
            return new String(llIlIIIllIIIlII.doFinal(Base64.getDecoder().decode(llIlIIIllIIIIlI.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }
        catch (Exception llIlIIIllIIIIll) {
            llIlIIIllIIIIll.printStackTrace();
            return null;
        }
    }
    
    private static boolean lllIIlIlI(final int llIlIIIlIIIIllI) {
        return llIlIIIlIIIIllI < 0;
    }
    
    private static int lllIIIlII(final float n, final float n2) {
        return fcmpg(n, n2);
    }
    
    @Override
    public void onWorldRender(final RenderEvent llIlIIllIllIIlI) {
        final boolean llIlIIllIllIIII = (boolean)RusherHackNameTags.mc.field_71441_e.field_73010_i.iterator();
        while (lllIIIIIl(((Iterator)llIlIIllIllIIII).hasNext() ? 1 : 0)) {
            final EntityPlayer llIlIIllIllIlII = ((Iterator<EntityPlayer>)llIlIIllIllIIII).next();
            if (lllIIIIlI(llIlIIllIllIlII, RusherHackNameTags.mc.func_175606_aa()) && lllIIIIIl(llIlIIllIllIlII.func_70089_S() ? 1 : 0)) {
                final double llIlIIllIllIlll = llIlIIllIllIlII.field_70142_S + (llIlIIllIllIlII.field_70165_t - llIlIIllIllIlII.field_70142_S) * RusherHackNameTags.mc.field_71428_T.field_194147_b - RusherHackNameTags.mc.func_175598_ae().field_78725_b;
                final double llIlIIllIllIllI = llIlIIllIllIlII.field_70137_T + (llIlIIllIllIlII.field_70163_u - llIlIIllIllIlII.field_70137_T) * RusherHackNameTags.mc.field_71428_T.field_194147_b - RusherHackNameTags.mc.func_175598_ae().field_78726_c;
                final double llIlIIllIllIlIl = llIlIIllIllIlII.field_70136_U + (llIlIIllIllIlII.field_70161_v - llIlIIllIllIlII.field_70136_U) * RusherHackNameTags.mc.field_71428_T.field_194147_b - RusherHackNameTags.mc.func_175598_ae().field_78723_d;
                if (lllIIIIll(llIlIIllIllIlII.func_70005_c_().startsWith(RusherHackNameTags.llIlIIlI[RusherHackNameTags.llIlIIll[3]]) ? 1 : 0)) {
                    this.renderNametag(llIlIIllIllIlII, llIlIIllIllIlll, llIlIIllIllIllI, llIlIIllIllIlIl);
                }
            }
            "".length();
            if (" ".length() != " ".length()) {
                return;
            }
        }
    }
    
    private static int lllIIlIIl(final double n, final double n2) {
        return dcmpg(n, n2);
    }
}
