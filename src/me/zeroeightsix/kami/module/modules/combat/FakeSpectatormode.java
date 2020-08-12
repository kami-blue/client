// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.combat;

import java.util.function.Predicate;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.world.World;
import net.minecraft.world.GameType;
import java.util.Base64;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import me.zeroeightsix.kami.event.events.PlayerMoveEvent;
import net.minecraft.entity.Entity;
import me.zeroeightsix.kami.event.events.PacketEvent;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import me.zeroeightsix.kami.setting.Setting;
import me.zero.alpine.listener.EventHandler;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;

@Info(name = "FakeSpectatormode", category = Category.COMBAT, description = "Leave your body and trascend into the realm of the gods")
public class FakeSpectatormode extends Module
{
    @EventHandler
    private /* synthetic */ Listener<PlayerSPPushOutOfBlocksEvent> pushListener;
    private /* synthetic */ boolean isRidingEntity;
    private static final /* synthetic */ int[] lIIllIIlllIl;
    private /* synthetic */ double posY;
    private /* synthetic */ Setting<Integer> speed;
    private /* synthetic */ float yaw;
    private /* synthetic */ EntityOtherPlayerMP clonedPlayer;
    @EventHandler
    private /* synthetic */ Listener<PacketEvent.Send> sendListener;
    private /* synthetic */ Entity ridingEntity;
    private /* synthetic */ float pitch;
    private static final /* synthetic */ String[] lIIllIIllIll;
    private /* synthetic */ double posZ;
    @EventHandler
    private /* synthetic */ Listener<PlayerMoveEvent> moveListener;
    private /* synthetic */ double posX;
    
    static {
        llIIlIIlIIIlI();
        llIIlIIIllIIl();
    }
    
    private static void llIIlIIlIIIlI() {
        (lIIllIIlllIl = new int[6])[0] = ((0x67 ^ 0x48 ^ (0x41 ^ 0x47)) & (0x1C ^ 0x6F ^ (0xC2 ^ 0x98) ^ -" ".length()));
        FakeSpectatormode.lIIllIIlllIl[1] = (0xC1 ^ 0xC4);
        FakeSpectatormode.lIIllIIlllIl[2] = " ".length();
        FakeSpectatormode.lIIllIIlllIl[3] = -(0xC3 ^ 0xA6 ^ " ".length());
        FakeSpectatormode.lIIllIIlllIl[4] = (111 + 161 - 113 + 41 ^ 29 + 70 + 42 + 51);
        FakeSpectatormode.lIIllIIlllIl[5] = "  ".length();
    }
    
    private static String llIIlIIIllIII(final String lllllllllllllllIllIllIIlIlIIIIII, final String lllllllllllllllIllIllIIlIIllllIl) {
        try {
            final SecretKeySpec lllllllllllllllIllIllIIlIlIIIIll = new SecretKeySpec(Arrays.copyOf(MessageDigest.getInstance("MD5").digest(lllllllllllllllIllIllIIlIIllllIl.getBytes(StandardCharsets.UTF_8)), FakeSpectatormode.lIIllIIlllIl[4]), "DES");
            final Cipher lllllllllllllllIllIllIIlIlIIIIlI = Cipher.getInstance("DES");
            lllllllllllllllIllIllIIlIlIIIIlI.init(FakeSpectatormode.lIIllIIlllIl[5], lllllllllllllllIllIllIIlIlIIIIll);
            return new String(lllllllllllllllIllIllIIlIlIIIIlI.doFinal(Base64.getDecoder().decode(lllllllllllllllIllIllIIlIlIIIIII.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }
        catch (Exception lllllllllllllllIllIllIIlIlIIIIIl) {
            lllllllllllllllIllIllIIlIlIIIIIl.printStackTrace();
            return null;
        }
    }
    
    @Override
    public void onUpdate() {
        FakeSpectatormode.mc.field_71439_g.field_71075_bZ.field_75100_b = (FakeSpectatormode.lIIllIIlllIl[2] != 0);
        FakeSpectatormode.mc.field_71439_g.field_71075_bZ.func_75092_a(this.speed.getValue() / 100.0f);
        FakeSpectatormode.mc.field_71439_g.field_70145_X = (FakeSpectatormode.lIIllIIlllIl[2] != 0);
        FakeSpectatormode.mc.field_71439_g.field_70122_E = (FakeSpectatormode.lIIllIIlllIl[0] != 0);
        FakeSpectatormode.mc.field_71439_g.field_70143_R = 0.0f;
        "".length();
        FakeSpectatormode.mc.field_71442_b.func_78746_a(GameType.SPECTATOR);
    }
    
    private static boolean llIIlIIlIIlII(final Object lllllllllllllllIllIllIIlIIllIlIl) {
        return lllllllllllllllIllIllIIlIIllIlIl == null;
    }
    
    private static boolean llIIlIIlIIlll(final int lllllllllllllllIllIllIIlIIlIlIlI) {
        return lllllllllllllllIllIllIIlIIlIlIlI == 0;
    }
    
    @Override
    protected void onEnable() {
        if (llIIlIIlIIIll(FakeSpectatormode.mc.field_71439_g)) {
            int isRidingEntity;
            if (llIIlIIlIIIll(FakeSpectatormode.mc.field_71439_g.func_184187_bx())) {
                isRidingEntity = FakeSpectatormode.lIIllIIlllIl[2];
                "".length();
                if (" ".length() > "   ".length()) {
                    return;
                }
            }
            else {
                isRidingEntity = FakeSpectatormode.lIIllIIlllIl[0];
            }
            this.isRidingEntity = (isRidingEntity != 0);
            if (llIIlIIlIIlII(FakeSpectatormode.mc.field_71439_g.func_184187_bx())) {
                this.posX = FakeSpectatormode.mc.field_71439_g.field_70165_t;
                this.posY = FakeSpectatormode.mc.field_71439_g.field_70163_u;
                this.posZ = FakeSpectatormode.mc.field_71439_g.field_70161_v;
                "".length();
                if (null != null) {
                    return;
                }
            }
            else {
                this.ridingEntity = FakeSpectatormode.mc.field_71439_g.func_184187_bx();
                FakeSpectatormode.mc.field_71439_g.func_184210_p();
            }
            this.pitch = FakeSpectatormode.mc.field_71439_g.field_70125_A;
            this.yaw = FakeSpectatormode.mc.field_71439_g.field_70177_z;
            this.clonedPlayer = new EntityOtherPlayerMP((World)FakeSpectatormode.mc.field_71441_e, FakeSpectatormode.mc.func_110432_I().func_148256_e());
            this.clonedPlayer.func_82149_j((Entity)FakeSpectatormode.mc.field_71439_g);
            this.clonedPlayer.field_70759_as = FakeSpectatormode.mc.field_71439_g.field_70759_as;
            FakeSpectatormode.mc.field_71441_e.func_73027_a(FakeSpectatormode.lIIllIIlllIl[3], (Entity)this.clonedPlayer);
            FakeSpectatormode.mc.field_71439_g.field_71075_bZ.field_75100_b = (FakeSpectatormode.lIIllIIlllIl[2] != 0);
            FakeSpectatormode.mc.field_71439_g.field_71075_bZ.func_75092_a(this.speed.getValue() / 100.0f);
            FakeSpectatormode.mc.field_71439_g.field_70145_X = (FakeSpectatormode.lIIllIIlllIl[2] != 0);
            "".length();
            FakeSpectatormode.mc.field_71442_b.func_78746_a(GameType.SPECTATOR);
        }
    }
    
    private static boolean llIIlIIlIIllI(final int lllllllllllllllIllIllIIlIIlIllll) {
        return lllllllllllllllIllIllIIlIIlIllll != 0;
    }
    
    private static boolean llIIlIIlIIIll(final Object lllllllllllllllIllIllIIlIIlllIIl) {
        return lllllllllllllllIllIllIIlIIlllIIl != null;
    }
    
    @Override
    protected void onDisable() {
        final EntityPlayer lllllllllllllllIllIllIIlIllIlIll = (EntityPlayer)FakeSpectatormode.mc.field_71439_g;
        if (llIIlIIlIIIll(lllllllllllllllIllIllIIlIllIlIll)) {
            FakeSpectatormode.mc.field_71439_g.func_70080_a(this.posX, this.posY, this.posZ, this.yaw, this.pitch);
            FakeSpectatormode.mc.field_71441_e.func_73028_b(FakeSpectatormode.lIIllIIlllIl[3]);
            "".length();
            this.clonedPlayer = null;
            final double posX = 0.0;
            this.posZ = posX;
            this.posY = posX;
            this.posX = posX;
            final float n = 0.0f;
            this.yaw = n;
            this.pitch = n;
            FakeSpectatormode.mc.field_71439_g.field_71075_bZ.field_75100_b = (FakeSpectatormode.lIIllIIlllIl[0] != 0);
            FakeSpectatormode.mc.field_71439_g.field_71075_bZ.func_75092_a(0.05f);
            FakeSpectatormode.mc.field_71439_g.field_70145_X = (FakeSpectatormode.lIIllIIlllIl[0] != 0);
            final EntityPlayerSP field_71439_g = FakeSpectatormode.mc.field_71439_g;
            final EntityPlayerSP field_71439_g2 = FakeSpectatormode.mc.field_71439_g;
            final EntityPlayerSP field_71439_g3 = FakeSpectatormode.mc.field_71439_g;
            final double field_70159_w = 0.0;
            field_71439_g3.field_70179_y = field_70159_w;
            field_71439_g2.field_70181_x = field_70159_w;
            field_71439_g.field_70159_w = field_70159_w;
            if (llIIlIIlIIllI(this.isRidingEntity ? 1 : 0)) {
                FakeSpectatormode.mc.field_71439_g.func_184205_a(this.ridingEntity, (boolean)(FakeSpectatormode.lIIllIIlllIl[2] != 0));
                "".length();
            }
        }
        "".length();
        FakeSpectatormode.mc.field_71442_b.func_78746_a(GameType.SURVIVAL);
    }
    
    public FakeSpectatormode() {
        this.speed = this.register(Settings.i(FakeSpectatormode.lIIllIIllIll[FakeSpectatormode.lIIllIIlllIl[0]], FakeSpectatormode.lIIllIIlllIl[1]));
        this.moveListener = new Listener<PlayerMoveEvent>(lllllllllllllllIllIllIIlIlIlIIIl -> FakeSpectatormode.mc.field_71439_g.field_70145_X = (FakeSpectatormode.lIIllIIlllIl[2] != 0), (Predicate<PlayerMoveEvent>[])new Predicate[FakeSpectatormode.lIIllIIlllIl[0]]);
        this.pushListener = new Listener<PlayerSPPushOutOfBlocksEvent>(lllllllllllllllIllIllIIlIlIlIIlI -> lllllllllllllllIllIllIIlIlIlIIlI.setCanceled((boolean)(FakeSpectatormode.lIIllIIlllIl[2] != 0)), (Predicate<PlayerSPPushOutOfBlocksEvent>[])new Predicate[FakeSpectatormode.lIIllIIlllIl[0]]);
        this.sendListener = new Listener<PacketEvent.Send>(lllllllllllllllIllIllIIlIlIlIllI -> {
            if (!llIIlIIlIIlll((lllllllllllllllIllIllIIlIlIlIllI.getPacket() instanceof CPacketPlayer) ? 1 : 0) || llIIlIIlIIllI((lllllllllllllllIllIllIIlIlIlIllI.getPacket() instanceof CPacketInput) ? 1 : 0)) {
                lllllllllllllllIllIllIIlIlIlIllI.cancel();
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[FakeSpectatormode.lIIllIIlllIl[0]]);
    }
    
    private static void llIIlIIIllIIl() {
        (lIIllIIllIll = new String[FakeSpectatormode.lIIllIIlllIl[2]])[FakeSpectatormode.lIIllIIlllIl[0]] = llIIlIIIllIII("txDlLuZh1lo=", "ajGLL");
    }
}
