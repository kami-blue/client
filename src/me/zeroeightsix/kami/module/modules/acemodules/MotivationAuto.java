// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.acemodules;

import java.util.List;
import java.util.Random;
import java.util.Arrays;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "MotivationAuto", description = "BeHappy", category = Category.ACEMODULE)
public class MotivationAuto extends Module
{
    private static final /* synthetic */ int[] lIlIIlIIIlIll;
    private /* synthetic */ int delay;
    private /* synthetic */ Setting<Integer> DelayChange;
    public static /* synthetic */ String motivationsAuto;
    
    private static boolean lllIIllIIIlIII(final int lllllllllllllIllIIIlllIIIllIlIIl) {
        return lllllllllllllIllIIIlllIIIllIlIIl != 0;
    }
    
    public MotivationAuto() {
        this.delay = MotivationAuto.lIlIIlIIIlIll[0];
        this.DelayChange = this.register((Setting<Integer>)Settings.integerBuilder("SecondDelay").withRange(MotivationAuto.lIlIIlIIIlIll[1], MotivationAuto.lIlIIlIIIlIll[2]).withValue(MotivationAuto.lIlIIlIIIlIll[3]).build());
    }
    
    private static void lllIIllIIIIllI() {
        (lIlIIlIIIlIll = new int[16])[0] = ((0x37 ^ 0x27 ^ (0x17 ^ 0x1E)) & (0x7A ^ 0x13 ^ (0xFF ^ 0x8F) ^ -" ".length()));
        MotivationAuto.lIlIIlIIIlIll[1] = " ".length();
        MotivationAuto.lIlIIlIIIlIll[2] = (0xCE ^ 0xAA);
        MotivationAuto.lIlIIlIIIlIll[3] = (0x31 ^ 0x3B);
        MotivationAuto.lIlIIlIIIlIll[4] = (0x5F ^ 0x77);
        MotivationAuto.lIlIIlIIIlIll[5] = (0xB3 ^ 0xBE);
        MotivationAuto.lIlIIlIIIlIll[6] = "  ".length();
        MotivationAuto.lIlIIlIIIlIll[7] = "   ".length();
        MotivationAuto.lIlIIlIIIlIll[8] = (0x18 ^ 0x49 ^ (0xED ^ 0xB8));
        MotivationAuto.lIlIIlIIIlIll[9] = (0x17 ^ 0x12);
        MotivationAuto.lIlIIlIIIlIll[10] = (90 + 12 - 78 + 155 ^ 101 + 65 - 1 + 16);
        MotivationAuto.lIlIIlIIIlIll[11] = (102 + 20 + 16 + 14 ^ 99 + 125 - 159 + 94);
        MotivationAuto.lIlIIlIIIlIll[12] = (0xC5 ^ 0xB8 ^ (0x4E ^ 0x3B));
        MotivationAuto.lIlIIlIIIlIll[13] = (0x95 ^ 0x9C);
        MotivationAuto.lIlIIlIIIlIll[14] = (44 + 120 - 120 + 100 ^ 63 + 59 + 10 + 23);
        MotivationAuto.lIlIIlIIIlIll[15] = (0xC ^ 0x0);
    }
    
    @Override
    public void onUpdate() {
        this.delay += MotivationAuto.lIlIIlIIIlIll[1];
        if (lllIIllIIIIlll(this.delay, this.DelayChange.getValue() * MotivationAuto.lIlIIlIIIlIll[4])) {
            if (lllIIllIIIlIII(MotivationAuto.mc.field_71439_g.func_70613_aW() ? 1 : 0)) {
                final String lllllllllllllIllIIIlllIIIllllIlI = MotivationAuto.mc.field_71439_g.func_70005_c_();
                final String[] a = new String[MotivationAuto.lIlIIlIIIlIll[5]];
                a[MotivationAuto.lIlIIlIIIlIll[0]] = String.valueOf(new StringBuilder().append("§6Damn You Lookin Fine Ass Hell Today ").append(lllllllllllllIllIIIlllIIIllllIlI));
                a[MotivationAuto.lIlIIlIIIlIll[1]] = String.valueOf(new StringBuilder().append("§6You My Favorite Pvper ").append(lllllllllllllIllIIIlllIIIllllIlI));
                a[MotivationAuto.lIlIIlIIIlIll[6]] = String.valueOf(new StringBuilder().append("§6").append(lllllllllllllIllIIIlllIIIllllIlI).append(" ON TOP! "));
                a[MotivationAuto.lIlIIlIIIlIll[7]] = String.valueOf(new StringBuilder().append("§6You Dont Even Need Totems ").append(lllllllllllllIllIIIlllIIIllllIlI).append(" Your Too Good"));
                a[MotivationAuto.lIlIIlIIIlIll[8]] = String.valueOf(new StringBuilder().append("§6Remember Ace Hack Loves You ").append(lllllllllllllIllIIIlllIIIllllIlI));
                a[MotivationAuto.lIlIIlIIIlIll[9]] = String.valueOf(new StringBuilder().append("§6Damn ").append(lllllllllllllIllIIIlllIIIllllIlI).append(" You Da Best Simp"));
                a[MotivationAuto.lIlIIlIIIlIll[10]] = String.valueOf(new StringBuilder().append("§6Ace Hack Will Always Be Here For You ").append(lllllllllllllIllIIIlllIIIllllIlI));
                a[MotivationAuto.lIlIIlIIIlIll[11]] = String.valueOf(new StringBuilder().append("§6I Know You Will Do Great Things With Ace Hack ").append(lllllllllllllIllIIIlllIIIllllIlI));
                a[MotivationAuto.lIlIIlIIIlIll[12]] = "§6PvP Takes Time And Practice Yet You Dont Need It. Weird Isnt It?";
                a[MotivationAuto.lIlIIlIIIlIll[13]] = "§6Using Ace Hack Is The Closest Thing To Happy!";
                a[MotivationAuto.lIlIIlIIIlIll[3]] = String.valueOf(new StringBuilder().append("§6").append(lllllllllllllIllIIIlllIIIllllIlI).append(" You Are The MF PIMP"));
                a[MotivationAuto.lIlIIlIIIlIll[14]] = "§6Kobe Knows You Da Best Pvper!";
                a[MotivationAuto.lIlIIlIIIlIll[15]] = "§6Win This Fight For Kobe!";
                final List<String> lllllllllllllIllIIIlllIIIllllIIl = Arrays.asList(a);
                final Random lllllllllllllIllIIIlllIIIllllIII = new Random();
                final int lllllllllllllIllIIIlllIIIlllIlll = lllllllllllllIllIIIlllIIIllllIII.nextInt(lllllllllllllIllIIIlllIIIllllIIl.size());
                final String lllllllllllllIllIIIlllIIIlllIllI = MotivationAuto.motivationsAuto = lllllllllllllIllIIIlllIIIllllIIl.get(lllllllllllllIllIIIlllIIIlllIlll);
            }
            this.delay = MotivationAuto.lIlIIlIIIlIll[0];
        }
    }
    
    static {
        lllIIllIIIIllI();
    }
    
    private static boolean lllIIllIIIIlll(final int lllllllllllllIllIIIlllIIIllIllII, final int lllllllllllllIllIIIlllIIIllIlIll) {
        return lllllllllllllIllIIIlllIIIllIllII > lllllllllllllIllIIIlllIIIllIlIll;
    }
}
