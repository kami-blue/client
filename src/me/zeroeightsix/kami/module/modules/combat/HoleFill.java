// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.combat;

import java.util.Iterator;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.BlockInteractionHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import java.util.Base64;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import me.zeroeightsix.kami.command.Command;
import net.minecraft.block.Block;
import java.util.List;
import me.zeroeightsix.kami.setting.Setting;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import me.zeroeightsix.kami.module.Module;

@Info(name = "HoleFill", category = Category.COMBAT)
public class HoleFill extends Module
{
    private /* synthetic */ ArrayList<BlockPos> holes;
    private /* synthetic */ Setting<Boolean> chat;
    private /* synthetic */ int waitCounter;
    private static final /* synthetic */ int[] lllIIlIlI;
    private /* synthetic */ Setting<Double> yRange;
    private /* synthetic */ List<Block> whiteList;
    private /* synthetic */ Setting<Integer> waitTick;
    private /* synthetic */ Setting<Double> range;
    private static final /* synthetic */ String[] lllIIIIlI;
    
    public void onDisable() {
        if (lllllIIIII(HoleFill.mc.field_71439_g) && llllIlIlIl(((boolean)this.chat.getValue()) ? 1 : 0)) {
            Command.sendChatMessage(HoleFill.lllIIIIlI[HoleFill.lllIIlIlI[7]]);
        }
    }
    
    private static boolean llllIllllI(final int lIlIIlIlIIIllIl, final int lIlIIlIlIIIllII) {
        return lIlIIlIlIIIllIl == lIlIIlIlIIIllII;
    }
    
    private static void llllIlIlII() {
        (lllIIlIlI = new int[10])[0] = " ".length();
        HoleFill.lllIIlIlI[1] = ((57 + 112 - 143 + 197 ^ 104 + 184 - 184 + 86) & (0x3D ^ 0x61 ^ (0x85 ^ 0xB8) ^ -" ".length()));
        HoleFill.lllIIlIlI[2] = "  ".length();
        HoleFill.lllIIlIlI[3] = "   ".length();
        HoleFill.lllIIlIlI[4] = -" ".length();
        HoleFill.lllIIlIlI[5] = (0xF4 ^ 0xAA ^ (0x5F ^ 0x8));
        HoleFill.lllIIlIlI[6] = (27 + 164 - 79 + 66 ^ 126 + 24 - 55 + 87);
        HoleFill.lllIIlIlI[7] = (0xB7 ^ 0xB2);
        HoleFill.lllIIlIlI[8] = (0xBF ^ 0xAB ^ (0x1B ^ 0x9));
        HoleFill.lllIIlIlI[9] = (91 + 148 - 183 + 133 ^ 6 + 97 - 58 + 136);
    }
    
    private static String lllIlIlllI(final String lIlIIlIlIIlIIll, final String lIlIIlIlIIlIlII) {
        try {
            final SecretKeySpec lIlIIlIlIIllIII = new SecretKeySpec(Arrays.copyOf(MessageDigest.getInstance("MD5").digest(lIlIIlIlIIlIlII.getBytes(StandardCharsets.UTF_8)), HoleFill.lllIIlIlI[9]), "DES");
            final Cipher lIlIIlIlIIlIlll = Cipher.getInstance("DES");
            lIlIIlIlIIlIlll.init(HoleFill.lllIIlIlI[2], lIlIIlIlIIllIII);
            return new String(lIlIIlIlIIlIlll.doFinal(Base64.getDecoder().decode(lIlIIlIlIIlIIll.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }
        catch (Exception lIlIIlIlIIlIllI) {
            lIlIIlIlIIlIllI.printStackTrace();
            return null;
        }
    }
    
    private static void lllIllIIII() {
        (lllIIIIlI = new String[HoleFill.lllIIlIlI[8]])[HoleFill.lllIIlIlI[1]] = lllIlIlllI("TPGcWRd/anc=", "ZRDCW");
        HoleFill.lllIIIIlI[HoleFill.lllIIlIlI[0]] = lllIlIllll("klfzmjf7IiM=", "PJBbO");
        HoleFill.lllIIIIlI[HoleFill.lllIIlIlI[2]] = lllIlIlllI("AmF7bL0NFuMWIt8+72/DMg==", "ApTWv");
        HoleFill.lllIIIIlI[HoleFill.lllIIlIlI[3]] = lllIlIllll("9ck8IM+7Y0f9wvS2ap8ICw==", "DoRVm");
        HoleFill.lllIIIIlI[HoleFill.lllIIlIlI[6]] = lllIlIlllI("1p7zTxy3ZMgPzRY/u9R8iRq/Eiin1U6Y", "ECDXe");
        HoleFill.lllIIIIlI[HoleFill.lllIIlIlI[7]] = lllIlIlllI("raC5R+r9Mcp7eqk4jIuCq7rOdHLk3wBg", "TZleV");
    }
    
    private static boolean llllIlIlll(final Object lIlIIlIlIIIIIIl, final Object lIlIIlIlIIIIIII) {
        return lIlIIlIlIIIIIIl == lIlIIlIlIIIIIII;
    }
    
    @Override
    public void onUpdate() {
        this.holes = new ArrayList<BlockPos>();
        final Iterable<BlockPos> lIlIIlIllIIlIIl = (Iterable<BlockPos>)BlockPos.func_177980_a(HoleFill.mc.field_71439_g.func_180425_c().func_177963_a(-this.range.getValue(), -this.yRange.getValue(), -this.range.getValue()), HoleFill.mc.field_71439_g.func_180425_c().func_177963_a((double)this.range.getValue(), (double)this.yRange.getValue(), (double)this.range.getValue()));
        final char lIlIIlIllIIIlII = (char)lIlIIlIllIIlIIl.iterator();
        while (llllIlIlIl(((Iterator)lIlIIlIllIIIlII).hasNext() ? 1 : 0)) {
            final BlockPos lIlIIlIllIIlllI = ((Iterator<BlockPos>)lIlIIlIllIIIlII).next();
            if (llllIlIllI(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI).func_185904_a().func_76230_c() ? 1 : 0) && llllIlIllI(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[0], HoleFill.lllIIlIlI[1])).func_185904_a().func_76230_c() ? 1 : 0)) {
                int n;
                if (llllIlIlll(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[4], HoleFill.lllIIlIlI[1])).func_177230_c(), Blocks.field_150357_h)) {
                    n = HoleFill.lllIIlIlI[0];
                    "".length();
                    if (((0x89 ^ 0x9E) & ~(0x28 ^ 0x3F)) >= (0x26 ^ 0x22)) {
                        return;
                    }
                }
                else {
                    n = HoleFill.lllIIlIlI[1];
                }
                int n2;
                if (llllIlIlll(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[4], HoleFill.lllIIlIlI[1])).func_177230_c(), Blocks.field_150343_Z)) {
                    n2 = HoleFill.lllIIlIlI[0];
                    "".length();
                    if ("   ".length() == 0) {
                        return;
                    }
                }
                else {
                    n2 = HoleFill.lllIIlIlI[1];
                }
                int n11 = 0;
                Label_1167: {
                    if (llllIlIlIl(n | n2)) {
                        int n3;
                        if (llllIlIlll(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[0], HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[1])).func_177230_c(), Blocks.field_150357_h)) {
                            n3 = HoleFill.lllIIlIlI[0];
                            "".length();
                            if (-" ".length() >= 0) {
                                return;
                            }
                        }
                        else {
                            n3 = HoleFill.lllIIlIlI[1];
                        }
                        int n4;
                        if (llllIlIlll(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[0], HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[1])).func_177230_c(), Blocks.field_150343_Z)) {
                            n4 = HoleFill.lllIIlIlI[0];
                            "".length();
                            if ("  ".length() > "  ".length()) {
                                return;
                            }
                        }
                        else {
                            n4 = HoleFill.lllIIlIlI[1];
                        }
                        if (llllIlIlIl(n3 | n4)) {
                            int n5;
                            if (llllIlIlll(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[0])).func_177230_c(), Blocks.field_150357_h)) {
                                n5 = HoleFill.lllIIlIlI[0];
                                "".length();
                                if (" ".length() < 0) {
                                    return;
                                }
                            }
                            else {
                                n5 = HoleFill.lllIIlIlI[1];
                            }
                            int n6;
                            if (llllIlIlll(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[0])).func_177230_c(), Blocks.field_150343_Z)) {
                                n6 = HoleFill.lllIIlIlI[0];
                                "".length();
                                if ("  ".length() > "  ".length()) {
                                    return;
                                }
                            }
                            else {
                                n6 = HoleFill.lllIIlIlI[1];
                            }
                            if (llllIlIlIl(n5 | n6)) {
                                int n7;
                                if (llllIlIlll(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[4], HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[1])).func_177230_c(), Blocks.field_150357_h)) {
                                    n7 = HoleFill.lllIIlIlI[0];
                                    "".length();
                                    if ((0x45 ^ 0x7C ^ (0x69 ^ 0x54)) <= 0) {
                                        return;
                                    }
                                }
                                else {
                                    n7 = HoleFill.lllIIlIlI[1];
                                }
                                int n8;
                                if (llllIlIlll(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[4], HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[1])).func_177230_c(), Blocks.field_150343_Z)) {
                                    n8 = HoleFill.lllIIlIlI[0];
                                    "".length();
                                    if (-" ".length() < -" ".length()) {
                                        return;
                                    }
                                }
                                else {
                                    n8 = HoleFill.lllIIlIlI[1];
                                }
                                if (llllIlIlIl(n7 | n8)) {
                                    int n9;
                                    if (llllIlIlll(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[4])).func_177230_c(), Blocks.field_150357_h)) {
                                        n9 = HoleFill.lllIIlIlI[0];
                                        "".length();
                                        if ((0x41 ^ 0x72 ^ (0x8D ^ 0xBA)) != (0x4C ^ 0x28 ^ (0xCA ^ 0xAA))) {
                                            return;
                                        }
                                    }
                                    else {
                                        n9 = HoleFill.lllIIlIlI[1];
                                    }
                                    int n10;
                                    if (llllIlIlll(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[4])).func_177230_c(), Blocks.field_150343_Z)) {
                                        n10 = HoleFill.lllIIlIlI[0];
                                        "".length();
                                        if (-(31 + 176 - 135 + 111 ^ 86 + 164 - 242 + 171) > 0) {
                                            return;
                                        }
                                    }
                                    else {
                                        n10 = HoleFill.lllIIlIlI[1];
                                    }
                                    if (llllIlIlIl(n9 | n10) && llllIlIlll(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[1])).func_185904_a(), Material.field_151579_a) && llllIlIlll(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[0], HoleFill.lllIIlIlI[1])).func_185904_a(), Material.field_151579_a) && llllIlIlll(HoleFill.mc.field_71441_e.func_180495_p(lIlIIlIllIIlllI.func_177982_a(HoleFill.lllIIlIlI[1], HoleFill.lllIIlIlI[2], HoleFill.lllIIlIlI[1])).func_185904_a(), Material.field_151579_a)) {
                                        n11 = HoleFill.lllIIlIlI[0];
                                        "".length();
                                        if (-"   ".length() > 0) {
                                            return;
                                        }
                                        break Label_1167;
                                    }
                                }
                            }
                        }
                    }
                    n11 = HoleFill.lllIIlIlI[1];
                }
                final boolean lIlIIlIllIIllll = n11 != 0;
                if (llllIlIlIl(lIlIIlIllIIllll ? 1 : 0)) {
                    this.holes.add(lIlIIlIllIIlllI);
                    "".length();
                }
            }
            "".length();
            if (-" ".length() > " ".length()) {
                return;
            }
        }
        int lIlIIlIllIIlIII = HoleFill.lllIIlIlI[4];
        int lIlIIlIllIIlIll = HoleFill.lllIIlIlI[1];
        while (llllIllIlI(lIlIIlIllIIlIll, HoleFill.lllIIlIlI[5])) {
            final ItemStack lIlIIlIllIIllIl = Wrapper.getPlayer().field_71071_by.func_70301_a(lIlIIlIllIIlIll);
            if (llllIllIll(lIlIIlIllIIllIl, ItemStack.field_190927_a)) {
                if (llllIlIllI((lIlIIlIllIIllIl.func_77973_b() instanceof ItemBlock) ? 1 : 0)) {
                    "".length();
                    if (-"   ".length() > 0) {
                        return;
                    }
                }
                else {
                    final Block lIlIIlIllIIllII = ((ItemBlock)lIlIIlIllIIllIl.func_77973_b()).func_179223_d();
                    if (llllIlIllI(this.whiteList.contains(lIlIIlIllIIllII) ? 1 : 0)) {
                        "".length();
                        if ((" ".length() & ~" ".length()) != 0x0) {
                            return;
                        }
                    }
                    else {
                        lIlIIlIllIIlIII = lIlIIlIllIIlIll;
                        "".length();
                        if (((150 + 49 - 183 + 141 ^ 65 + 146 - 130 + 74) & (153 + 33 - 43 + 11 ^ 33 + 104 - 50 + 69 ^ -" ".length())) < 0) {
                            return;
                        }
                        break;
                    }
                }
            }
            ++lIlIIlIllIIlIll;
            "".length();
            if ("   ".length() <= 0) {
                return;
            }
        }
        if (llllIllllI(lIlIIlIllIIlIII, HoleFill.lllIIlIlI[4])) {
            return;
        }
        final int lIlIIlIllIIIlll = Wrapper.getPlayer().field_71071_by.field_70461_c;
        if (llllIlllll(this.waitTick.getValue())) {
            if (llllIllIlI(this.waitCounter, this.waitTick.getValue())) {
                Wrapper.getPlayer().field_71071_by.field_70461_c = lIlIIlIllIIlIII;
                this.holes.forEach(lIlIIlIlIlIlIlI -> this.place(lIlIIlIlIlIlIlI));
                Wrapper.getPlayer().field_71071_by.field_70461_c = lIlIIlIllIIIlll;
                return;
            }
            this.waitCounter = HoleFill.lllIIlIlI[1];
        }
    }
    
    private void place(final BlockPos lIlIIlIlIllIlII) {
        final boolean lIlIIlIlIllIIIl = (boolean)HoleFill.mc.field_71441_e.func_72839_b((Entity)null, new AxisAlignedBB(lIlIIlIlIllIlII)).iterator();
        while (llllIlIlIl(((Iterator)lIlIIlIlIllIIIl).hasNext() ? 1 : 0)) {
            final Entity lIlIIlIlIllIllI = ((Iterator<Entity>)lIlIIlIlIllIIIl).next();
            if (llllIlIlIl((lIlIIlIlIllIllI instanceof EntityLivingBase) ? 1 : 0)) {
                return;
            }
            "".length();
            if (null != null) {
                return;
            }
        }
        if (llllIlIllI(HoleFill.mc.field_71439_g.func_70093_af() ? 1 : 0)) {
            HoleFill.mc.field_71439_g.func_70095_a((boolean)(HoleFill.lllIIlIlI[0] != 0));
        }
        BlockInteractionHelper.placeBlockScaffold(lIlIIlIlIllIlII);
        if (llllIlIlIl(HoleFill.mc.field_71439_g.func_70093_af() ? 1 : 0)) {
            HoleFill.mc.field_71439_g.func_70095_a((boolean)(HoleFill.lllIIlIlI[1] != 0));
        }
        this.waitCounter += HoleFill.lllIIlIlI[0];
    }
    
    private static String lllIlIllll(final String lIlIIlIlIlIIIlI, final String lIlIIlIlIlIIIIl) {
        try {
            final SecretKeySpec lIlIIlIlIlIIlIl = new SecretKeySpec(MessageDigest.getInstance("MD5").digest(lIlIIlIlIlIIIIl.getBytes(StandardCharsets.UTF_8)), "Blowfish");
            final Cipher lIlIIlIlIlIIlII = Cipher.getInstance("Blowfish");
            lIlIIlIlIlIIlII.init(HoleFill.lllIIlIlI[2], lIlIIlIlIlIIlIl);
            return new String(lIlIIlIlIlIIlII.doFinal(Base64.getDecoder().decode(lIlIIlIlIlIIIlI.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }
        catch (Exception lIlIIlIlIlIIIll) {
            lIlIIlIlIlIIIll.printStackTrace();
            return null;
        }
    }
    
    public HoleFill() {
        this.holes = new ArrayList<BlockPos>();
        final Block[] a = new Block[HoleFill.lllIIlIlI[0]];
        a[HoleFill.lllIIlIlI[1]] = Blocks.field_150343_Z;
        this.whiteList = Arrays.asList(a);
        this.range = this.register((Setting<Double>)Settings.doubleBuilder(HoleFill.lllIIIIlI[HoleFill.lllIIlIlI[1]]).withRange(0.0, 10.0).withValue(4.0).build());
        this.yRange = this.register((Setting<Double>)Settings.doubleBuilder(HoleFill.lllIIIIlI[HoleFill.lllIIlIlI[0]]).withRange(0.0, 10.0).withValue(1.0).build());
        this.waitTick = this.register((Setting<Integer>)Settings.integerBuilder(HoleFill.lllIIIIlI[HoleFill.lllIIlIlI[2]]).withMinimum(HoleFill.lllIIlIlI[1]).withValue(HoleFill.lllIIlIlI[3]).build());
        this.chat = this.register(Settings.b(HoleFill.lllIIIIlI[HoleFill.lllIIlIlI[3]], (boolean)(HoleFill.lllIIlIlI[0] != 0)));
    }
    
    static {
        llllIlIlII();
        lllIllIIII();
    }
    
    private static boolean llllIlIllI(final int lIlIIlIIllllIlI) {
        return lIlIIlIIllllIlI == 0;
    }
    
    private static boolean lllllIIIII(final Object lIlIIlIIllllllI) {
        return lIlIIlIIllllllI != null;
    }
    
    private static boolean llllIllIll(final Object lIlIIlIlIIIIlIl, final Object lIlIIlIlIIIIlII) {
        return lIlIIlIlIIIIlIl != lIlIIlIlIIIIlII;
    }
    
    private static boolean llllIlllll(final int lIlIIlIIllllIII) {
        return lIlIIlIIllllIII > 0;
    }
    
    private static boolean llllIllIlI(final int lIlIIlIlIIIlIIl, final int lIlIIlIlIIIlIII) {
        return lIlIIlIlIIIlIIl < lIlIIlIlIIIlIII;
    }
    
    private static boolean llllIlIlIl(final int lIlIIlIIlllllII) {
        return lIlIIlIIlllllII != 0;
    }
    
    public void onEnable() {
        if (lllllIIIII(HoleFill.mc.field_71439_g) && llllIlIlIl(((boolean)this.chat.getValue()) ? 1 : 0)) {
            Command.sendChatMessage(HoleFill.lllIIIIlI[HoleFill.lllIIlIlI[6]]);
        }
    }
}
