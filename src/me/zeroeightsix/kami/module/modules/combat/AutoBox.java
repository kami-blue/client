// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.combat;

import java.util.Iterator;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import java.util.List;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.util.math.BlockPos;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import me.zeroeightsix.kami.command.Command;
import net.minecraft.network.Packet;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketEntityAction;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoBox", category = Category.COMBAT, description = "Traps players near you with obby")
public class AutoBox extends Module
{
    private /* synthetic */ String lastTickTargetName;
    private /* synthetic */ boolean firstRun;
    private /* synthetic */ int lastHotbarSlot;
    private /* synthetic */ Setting<Boolean> rotate;
    private /* synthetic */ int offsetStep;
    private /* synthetic */ Setting<Boolean> announceUsage;
    private /* synthetic */ Setting<Integer> blockPerTick;
    private final /* synthetic */ Vec3d[] offsetsDefault;
    private /* synthetic */ int playerHotbarSlot;
    private /* synthetic */ boolean isSneaking;
    private static final /* synthetic */ String[] llIIlllIlIll;
    private static final /* synthetic */ int[] llIIlllIllII;
    private /* synthetic */ Setting<Double> range;
    private /* synthetic */ EntityPlayer closestTarget;
    
    @Override
    protected void onEnable() {
        if (lIIIlIIIIllllI(AutoBox.mc.field_71439_g)) {
            this.disable();
            return;
        }
        this.firstRun = (AutoBox.llIIlllIllII[2] != 0);
        this.playerHotbarSlot = Wrapper.getPlayer().field_71071_by.field_70461_c;
        this.lastHotbarSlot = AutoBox.llIIlllIllII[15];
    }
    
    private static int lIIIlIlIIIllII(final float n, final float n2) {
        return fcmpg(n, n2);
    }
    
    private static boolean lIIIlIIIIlllll(final int lllllllllllllllIIlIIIIlIllIIlIII, final int lllllllllllllllIIlIIIIlIllIIIllI) {
        return lllllllllllllllIIlIIIIlIllIIlIII != lllllllllllllllIIlIIIIlIllIIIllI;
    }
    
    private static int lIIIlIlIIIllIl(final float n, final float n2) {
        return fcmpl(n, n2);
    }
    
    @Override
    protected void onDisable() {
        if (lIIIlIIIIllllI(AutoBox.mc.field_71439_g)) {
            return;
        }
        if (lIIIlIIIIlllll(this.lastHotbarSlot, this.playerHotbarSlot) && lIIIlIIIIlllll(this.playerHotbarSlot, AutoBox.llIIlllIllII[15])) {
            Wrapper.getPlayer().field_71071_by.field_70461_c = this.playerHotbarSlot;
        }
        if (lIIIlIIIlIIIII(this.isSneaking ? 1 : 0)) {
            AutoBox.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)AutoBox.mc.field_71439_g, CPacketEntityAction.Action.STOP_SNEAKING));
            this.isSneaking = (AutoBox.llIIlllIllII[1] != 0);
        }
        this.playerHotbarSlot = AutoBox.llIIlllIllII[15];
        this.lastHotbarSlot = AutoBox.llIIlllIllII[15];
        if (lIIIlIIIlIIIII(((boolean)this.announceUsage.getValue()) ? 1 : 0)) {
            Command.sendChatMessage(AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[5]]);
        }
    }
    
    private static void lIIIlIIIIlllIl() {
        (llIIlllIllII = new int[16])[0] = (136 + 8 - 101 + 102 ^ 83 + 43 - 58 + 91);
        AutoBox.llIIlllIllII[1] = ((0x2A ^ 0x3A) & ~(0xAE ^ 0xBE));
        AutoBox.llIIlllIllII[2] = " ".length();
        AutoBox.llIIlllIllII[3] = "  ".length();
        AutoBox.llIIlllIllII[4] = "   ".length();
        AutoBox.llIIlllIllII[5] = (0x66 ^ 0x2D ^ (0x39 ^ 0x76));
        AutoBox.llIIlllIllII[6] = (0xC4 ^ 0xC1);
        AutoBox.llIIlllIllII[7] = (0x13 ^ 0x15);
        AutoBox.llIIlllIllII[8] = (0xF ^ 0x49 ^ (0x70 ^ 0x31));
        AutoBox.llIIlllIllII[9] = (0x1A ^ 0x1 ^ (0x58 ^ 0x4B));
        AutoBox.llIIlllIllII[10] = (0xA1 ^ 0xA4 ^ (0xCD ^ 0xC1));
        AutoBox.llIIlllIllII[11] = (0x74 ^ 0x7E);
        AutoBox.llIIlllIllII[12] = (0x7A ^ 0x71);
        AutoBox.llIIlllIllII[13] = (0x31 ^ 0x3D);
        AutoBox.llIIlllIllII[14] = (0xCC ^ 0xC1 ^ ((0xB0 ^ 0x91) & ~(0x42 ^ 0x63)));
        AutoBox.llIIlllIllII[15] = -" ".length();
    }
    
    private static void lIIIlIIIIlllII() {
        (llIIlllIlIll = new String[AutoBox.llIIlllIllII[10]])[AutoBox.llIIlllIllII[1]] = lIIIlIIIIllIIl("xif5fdcK9Yk=", "EvtYp");
        AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[2]] = lIIIlIIIIllIlI("AwY4NyUySicxPGE+Pjcl", "AjWTN");
        AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[3]] = lIIIlIIIIllIlI("BAIHLhgz", "VmsOl");
        AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[4]] = lIIIlIIIIllIIl("mNnoJq4NDH3VaeMaSi+A0Q==", "BlBOU");
        AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[5]] = lIIIlIIIIllIlI("HgIgED0HLC05cgEqJgUwKSYxRQ==", "ECUdR");
        AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[6]] = lIIIlIIIIllIIl("4IOSCqXDaws=", "qaluO");
        AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[7]] = lIIIlIIIIllIll("aoegr+Xqy3pYJdrk/TO4r1PBR7eVEq0CWmFE8eaXgkoAvmAjwBzZsw==", "ZvZlX");
        AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[8]] = lIIIlIIIIllIIl("FHYv6qnAxzNNYUlcM55DgXFigdJsvr7RFbxCUo/nWLo=", "eAfhR");
        AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[9]] = lIIIlIIIIllIll("6rsPEUA9gbJIL0WiInXNTI2fAHvAPoN7", "pnnjF");
    }
    
    private static String lIIIlIIIIllIlI(String lllllllllllllllIIlIIIIllIIlIlIIl, final String lllllllllllllllIIlIIIIllIIlIIlll) {
        lllllllllllllllIIlIIIIllIIlIlIIl = new String(Base64.getDecoder().decode(lllllllllllllllIIlIIIIllIIlIlIIl.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        final StringBuilder lllllllllllllllIIlIIIIllIIlIllII = new StringBuilder();
        final char[] lllllllllllllllIIlIIIIllIIlIlIll = lllllllllllllllIIlIIIIllIIlIIlll.toCharArray();
        int lllllllllllllllIIlIIIIllIIlIlIlI = AutoBox.llIIlllIllII[1];
        final int lllllllllllllllIIlIIIIllIIlIIIII = (Object)lllllllllllllllIIlIIIIllIIlIlIIl.toCharArray();
        final Exception lllllllllllllllIIlIIIIllIIIlllll = (Exception)lllllllllllllllIIlIIIIllIIlIIIII.length;
        Exception lllllllllllllllIIlIIIIllIIIllllI = (Exception)AutoBox.llIIlllIllII[1];
        while (lIIIlIlIIIIlII((int)lllllllllllllllIIlIIIIllIIIllllI, (int)lllllllllllllllIIlIIIIllIIIlllll)) {
            final char lllllllllllllllIIlIIIIllIIlIllll = lllllllllllllllIIlIIIIllIIlIIIII[lllllllllllllllIIlIIIIllIIIllllI];
            lllllllllllllllIIlIIIIllIIlIllII.append((char)(lllllllllllllllIIlIIIIllIIlIllll ^ lllllllllllllllIIlIIIIllIIlIlIll[lllllllllllllllIIlIIIIllIIlIlIlI % lllllllllllllllIIlIIIIllIIlIlIll.length]));
            "".length();
            ++lllllllllllllllIIlIIIIllIIlIlIlI;
            ++lllllllllllllllIIlIIIIllIIIllllI;
            "".length();
            if (-" ".length() >= (0x88 ^ 0x9A ^ (0x39 ^ 0x2F))) {
                return null;
            }
        }
        return String.valueOf(lllllllllllllllIIlIIIIllIIlIllII);
    }
    
    private static boolean lIIIlIlIIIllll(final int lllllllllllllllIIlIIIIlIllIlllII) {
        return lllllllllllllllIIlIIIIlIllIlllII >= 0;
    }
    
    private static boolean lIIIlIlIIIlllI(final Object lllllllllllllllIIlIIIIlIlllIlIIl, final Object lllllllllllllllIIlIIIIlIlllIlIII) {
        return lllllllllllllllIIlIIIIlIlllIlIIl == lllllllllllllllIIlIIIIlIlllIlIII;
    }
    
    private static String lIIIlIIIIllIIl(final String lllllllllllllllIIlIIIIllIlIlIlll, final String lllllllllllllllIIlIIIIllIlIlIllI) {
        try {
            final SecretKeySpec lllllllllllllllIIlIIIIllIlIllIlI = new SecretKeySpec(Arrays.copyOf(MessageDigest.getInstance("MD5").digest(lllllllllllllllIIlIIIIllIlIlIllI.getBytes(StandardCharsets.UTF_8)), AutoBox.llIIlllIllII[9]), "DES");
            final Cipher lllllllllllllllIIlIIIIllIlIllIIl = Cipher.getInstance("DES");
            lllllllllllllllIIlIIIIllIlIllIIl.init(AutoBox.llIIlllIllII[3], lllllllllllllllIIlIIIIllIlIllIlI);
            return new String(lllllllllllllllIIlIIIIllIlIllIIl.doFinal(Base64.getDecoder().decode(lllllllllllllllIIlIIIIllIlIlIlll.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }
        catch (Exception lllllllllllllllIIlIIIIllIlIllIII) {
            lllllllllllllllIIlIIIIllIlIllIII.printStackTrace();
            return null;
        }
    }
    
    private boolean placeBlock(final BlockPos lllllllllllllllIIlIIIIlllIlIlIll) {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     3: getfield        net/minecraft/client/Minecraft.field_71441_e:Lnet/minecraft/client/multiplayer/WorldClient;
        //     6: aload_1         /* lllllllllllllllIIlIIIIlllIlIlIII */
        //     7: invokevirtual   net/minecraft/client/multiplayer/WorldClient.func_180495_p:(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
        //    10: invokeinterface net/minecraft/block/state/IBlockState.func_185904_a:()Lnet/minecraft/block/material/Material;
        //    15: invokevirtual   net/minecraft/block/material/Material.func_76222_j:()Z
        //    18: invokestatic    me/zeroeightsix/kami/module/modules/combat/AutoBox.lIIIlIlIIIIIll:(I)Z
        //    21: ifeq            30
        //    24: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.llIIlllIllII:[I
        //    27: iconst_1       
        //    28: iaload         
        //    29: ireturn        
        //    30: aload_1         /* lllllllllllllllIIlIIIIlllIlIlIII */
        //    31: invokestatic    me/zeroeightsix/kami/util/BlockInteractionHelper.checkForNeighbours:(Lnet/minecraft/util/math/BlockPos;)Z
        //    34: invokestatic    me/zeroeightsix/kami/module/modules/combat/AutoBox.lIIIlIlIIIIIll:(I)Z
        //    37: ifeq            46
        //    40: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.llIIlllIllII:[I
        //    43: iconst_1       
        //    44: iaload         
        //    45: ireturn        
        //    46: new             Lnet/minecraft/util/math/Vec3d;
        //    49: dup            
        //    50: invokestatic    me/zeroeightsix/kami/util/Wrapper.getPlayer:()Lnet/minecraft/client/entity/EntityPlayerSP;
        //    53: getfield        net/minecraft/client/entity/EntityPlayerSP.field_70165_t:D
        //    56: invokestatic    me/zeroeightsix/kami/util/Wrapper.getPlayer:()Lnet/minecraft/client/entity/EntityPlayerSP;
        //    59: getfield        net/minecraft/client/entity/EntityPlayerSP.field_70163_u:D
        //    62: invokestatic    me/zeroeightsix/kami/util/Wrapper.getPlayer:()Lnet/minecraft/client/entity/EntityPlayerSP;
        //    65: invokevirtual   net/minecraft/client/entity/EntityPlayerSP.func_70047_e:()F
        //    68: f2d            
        //    69: dadd           
        //    70: invokestatic    me/zeroeightsix/kami/util/Wrapper.getPlayer:()Lnet/minecraft/client/entity/EntityPlayerSP;
        //    73: getfield        net/minecraft/client/entity/EntityPlayerSP.field_70161_v:D
        //    76: invokespecial   net/minecraft/util/math/Vec3d.<init>:(DDD)V
        //    79: astore_2        /* lllllllllllllllIIlIIIIlllIlIIlll */
        //    80: invokestatic    net/minecraft/util/EnumFacing.values:()[Lnet/minecraft/util/EnumFacing;
        //    83: astore_3        /* lllllllllllllllIIlIIIIlllIlIIllI */
        //    84: aload_3         /* lllllllllllllllIIlIIIIlllIlIIllI */
        //    85: arraylength    
        //    86: istore          lllllllllllllllIIlIIIIlllIlIIlII
        //    88: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.llIIlllIllII:[I
        //    91: iconst_1       
        //    92: iaload         
        //    93: istore          lllllllllllllllIIlIIIIlllIlIIIll
        //    95: iload           lllllllllllllllIIlIIIIlllIlIIIll
        //    97: iload           lllllllllllllllIIlIIIIlllIlIIlII
        //    99: invokestatic    me/zeroeightsix/kami/module/modules/combat/AutoBox.lIIIlIlIIIIlII:(II)Z
        //   102: ifeq            504
        //   105: aload_3         /* lllllllllllllllIIlIIIIlllIlIIllI */
        //   106: iload           lllllllllllllllIIlIIIIlllIlIIIll
        //   108: aaload         
        //   109: astore          lllllllllllllllIIlIIIIlllIlIllIl
        //   111: aload_1         /* lllllllllllllllIIlIIIIlllIlIlIII */
        //   112: aload           lllllllllllllllIIlIIIIlllIlIllIl
        //   114: invokevirtual   net/minecraft/util/math/BlockPos.func_177972_a:(Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/util/math/BlockPos;
        //   117: astore          lllllllllllllllIIlIIIIlllIlIllll
        //   119: aload           lllllllllllllllIIlIIIIlllIlIllIl
        //   121: invokevirtual   net/minecraft/util/EnumFacing.func_176734_d:()Lnet/minecraft/util/EnumFacing;
        //   124: astore          lllllllllllllllIIlIIIIlllIlIlllI
        //   126: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.mc:Lnet/minecraft/client/Minecraft;
        //   129: getfield        net/minecraft/client/Minecraft.field_71441_e:Lnet/minecraft/client/multiplayer/WorldClient;
        //   132: aload           lllllllllllllllIIlIIIIlllIlIllll
        //   134: invokevirtual   net/minecraft/client/multiplayer/WorldClient.func_180495_p:(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
        //   137: invokeinterface net/minecraft/block/state/IBlockState.func_177230_c:()Lnet/minecraft/block/Block;
        //   142: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.mc:Lnet/minecraft/client/Minecraft;
        //   145: getfield        net/minecraft/client/Minecraft.field_71441_e:Lnet/minecraft/client/multiplayer/WorldClient;
        //   148: aload           lllllllllllllllIIlIIIIlllIlIllll
        //   150: invokevirtual   net/minecraft/client/multiplayer/WorldClient.func_180495_p:(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
        //   153: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.llIIlllIllII:[I
        //   156: iconst_1       
        //   157: iaload         
        //   158: invokevirtual   net/minecraft/block/Block.func_176209_a:(Lnet/minecraft/block/state/IBlockState;Z)Z
        //   161: invokestatic    me/zeroeightsix/kami/module/modules/combat/AutoBox.lIIIlIIIlIIIII:(I)Z
        //   164: ifeq            464
        //   167: new             Lnet/minecraft/util/math/Vec3d;
        //   170: dup            
        //   171: aload           lllllllllllllllIIlIIIIlllIlIllll
        //   173: invokespecial   net/minecraft/util/math/Vec3d.<init>:(Lnet/minecraft/util/math/Vec3i;)V
        //   176: ldc2_w          0.5
        //   179: ldc2_w          0.5
        //   182: ldc2_w          0.5
        //   185: invokevirtual   net/minecraft/util/math/Vec3d.func_72441_c:(DDD)Lnet/minecraft/util/math/Vec3d;
        //   188: new             Lnet/minecraft/util/math/Vec3d;
        //   191: dup            
        //   192: aload           lllllllllllllllIIlIIIIlllIlIlllI
        //   194: invokevirtual   net/minecraft/util/EnumFacing.func_176730_m:()Lnet/minecraft/util/math/Vec3i;
        //   197: invokespecial   net/minecraft/util/math/Vec3d.<init>:(Lnet/minecraft/util/math/Vec3i;)V
        //   200: ldc2_w          0.5
        //   203: invokevirtual   net/minecraft/util/math/Vec3d.func_186678_a:(D)Lnet/minecraft/util/math/Vec3d;
        //   206: invokevirtual   net/minecraft/util/math/Vec3d.func_178787_e:(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;
        //   209: astore          lllllllllllllllIIlIIIIlllIllIIII
        //   211: aload_2         /* lllllllllllllllIIlIIIIlllIlIlIlI */
        //   212: aload           lllllllllllllllIIlIIIIlllIllIIII
        //   214: invokevirtual   net/minecraft/util/math/Vec3d.func_72438_d:(Lnet/minecraft/util/math/Vec3d;)D
        //   217: aload_0         /* lllllllllllllllIIlIIIIlllIlIlIIl */
        //   218: getfield        me/zeroeightsix/kami/module/modules/combat/AutoBox.range:Lme/zeroeightsix/kami/setting/Setting;
        //   221: invokevirtual   me/zeroeightsix/kami/setting/Setting.getValue:()Ljava/lang/Object;
        //   224: checkcast       Ljava/lang/Double;
        //   227: invokevirtual   java/lang/Double.doubleValue:()D
        //   230: invokestatic    me/zeroeightsix/kami/module/modules/combat/AutoBox.lIIIlIlIIIIlll:(DD)I
        //   233: invokestatic    me/zeroeightsix/kami/module/modules/combat/AutoBox.lIIIlIlIIIlIIl:(I)Z
        //   236: ifeq            464
        //   239: aload_0         /* lllllllllllllllIIlIIIIlllIlIlIIl */
        //   240: invokespecial   me/zeroeightsix/kami/module/modules/combat/AutoBox.findObiInHotbar:()I
        //   243: istore          lllllllllllllllIIlIIIIlllIllIIlI
        //   245: iload           lllllllllllllllIIlIIIIlllIllIIlI
        //   247: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.llIIlllIllII:[I
        //   250: bipush          15
        //   252: iaload         
        //   253: invokestatic    me/zeroeightsix/kami/module/modules/combat/AutoBox.lIIIlIlIIIlIlI:(II)Z
        //   256: ifeq            269
        //   259: aload_0         /* lllllllllllllllIIlIIIIlllIlIlIIl */
        //   260: invokevirtual   me/zeroeightsix/kami/module/modules/combat/AutoBox.disable:()V
        //   263: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.llIIlllIllII:[I
        //   266: iconst_1       
        //   267: iaload         
        //   268: ireturn        
        //   269: aload_0         /* lllllllllllllllIIlIIIIlllIlIlIIl */
        //   270: getfield        me/zeroeightsix/kami/module/modules/combat/AutoBox.lastHotbarSlot:I
        //   273: iload           lllllllllllllllIIlIIIIlllIllIIlI
        //   275: invokestatic    me/zeroeightsix/kami/module/modules/combat/AutoBox.lIIIlIIIIlllll:(II)Z
        //   278: ifeq            298
        //   281: invokestatic    me/zeroeightsix/kami/util/Wrapper.getPlayer:()Lnet/minecraft/client/entity/EntityPlayerSP;
        //   284: getfield        net/minecraft/client/entity/EntityPlayerSP.field_71071_by:Lnet/minecraft/entity/player/InventoryPlayer;
        //   287: iload           lllllllllllllllIIlIIIIlllIllIIlI
        //   289: putfield        net/minecraft/entity/player/InventoryPlayer.field_70461_c:I
        //   292: aload_0         /* lllllllllllllllIIlIIIIlllIlIlIIl */
        //   293: iload           lllllllllllllllIIlIIIIlllIllIIlI
        //   295: putfield        me/zeroeightsix/kami/module/modules/combat/AutoBox.lastHotbarSlot:I
        //   298: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.mc:Lnet/minecraft/client/Minecraft;
        //   301: getfield        net/minecraft/client/Minecraft.field_71441_e:Lnet/minecraft/client/multiplayer/WorldClient;
        //   304: aload           lllllllllllllllIIlIIIIlllIlIllll
        //   306: invokevirtual   net/minecraft/client/multiplayer/WorldClient.func_180495_p:(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
        //   309: invokeinterface net/minecraft/block/state/IBlockState.func_177230_c:()Lnet/minecraft/block/Block;
        //   314: astore          lllllllllllllllIIlIIIIlllIllIIIl
        //   316: getstatic       me/zeroeightsix/kami/util/BlockInteractionHelper.blackList:Ljava/util/List;
        //   319: aload           lllllllllllllllIIlIIIIlllIllIIIl
        //   321: invokeinterface java/util/List.contains:(Ljava/lang/Object;)Z
        //   326: invokestatic    me/zeroeightsix/kami/module/modules/combat/AutoBox.lIIIlIlIIIIIll:(I)Z
        //   329: ifeq            348
        //   332: getstatic       me/zeroeightsix/kami/util/BlockInteractionHelper.shulkerList:Ljava/util/List;
        //   335: aload           lllllllllllllllIIlIIIIlllIllIIIl
        //   337: invokeinterface java/util/List.contains:(Ljava/lang/Object;)Z
        //   342: invokestatic    me/zeroeightsix/kami/module/modules/combat/AutoBox.lIIIlIIIlIIIII:(I)Z
        //   345: ifeq            385
        //   348: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.mc:Lnet/minecraft/client/Minecraft;
        //   351: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //   354: getfield        net/minecraft/client/entity/EntityPlayerSP.field_71174_a:Lnet/minecraft/client/network/NetHandlerPlayClient;
        //   357: new             Lnet/minecraft/network/play/client/CPacketEntityAction;
        //   360: dup            
        //   361: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.mc:Lnet/minecraft/client/Minecraft;
        //   364: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //   367: getstatic       net/minecraft/network/play/client/CPacketEntityAction$Action.START_SNEAKING:Lnet/minecraft/network/play/client/CPacketEntityAction$Action;
        //   370: invokespecial   net/minecraft/network/play/client/CPacketEntityAction.<init>:(Lnet/minecraft/entity/Entity;Lnet/minecraft/network/play/client/CPacketEntityAction$Action;)V
        //   373: invokevirtual   net/minecraft/client/network/NetHandlerPlayClient.func_147297_a:(Lnet/minecraft/network/Packet;)V
        //   376: aload_0         /* lllllllllllllllIIlIIIIlllIlIlIIl */
        //   377: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.llIIlllIllII:[I
        //   380: iconst_2       
        //   381: iaload         
        //   382: putfield        me/zeroeightsix/kami/module/modules/combat/AutoBox.isSneaking:Z
        //   385: aload_0         /* lllllllllllllllIIlIIIIlllIlIlIIl */
        //   386: getfield        me/zeroeightsix/kami/module/modules/combat/AutoBox.rotate:Lme/zeroeightsix/kami/setting/Setting;
        //   389: invokevirtual   me/zeroeightsix/kami/setting/Setting.getValue:()Ljava/lang/Object;
        //   392: checkcast       Ljava/lang/Boolean;
        //   395: invokevirtual   java/lang/Boolean.booleanValue:()Z
        //   398: invokestatic    me/zeroeightsix/kami/module/modules/combat/AutoBox.lIIIlIIIlIIIII:(I)Z
        //   401: ifeq            409
        //   404: aload           lllllllllllllllIIlIIIIlllIllIIII
        //   406: invokestatic    me/zeroeightsix/kami/util/BlockInteractionHelper.faceVectorPacketInstant:(Lnet/minecraft/util/math/Vec3d;)V
        //   409: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.mc:Lnet/minecraft/client/Minecraft;
        //   412: getfield        net/minecraft/client/Minecraft.field_71442_b:Lnet/minecraft/client/multiplayer/PlayerControllerMP;
        //   415: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.mc:Lnet/minecraft/client/Minecraft;
        //   418: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //   421: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.mc:Lnet/minecraft/client/Minecraft;
        //   424: getfield        net/minecraft/client/Minecraft.field_71441_e:Lnet/minecraft/client/multiplayer/WorldClient;
        //   427: aload           lllllllllllllllIIlIIIIlllIlIllll
        //   429: aload           lllllllllllllllIIlIIIIlllIlIlllI
        //   431: aload           lllllllllllllllIIlIIIIlllIllIIII
        //   433: getstatic       net/minecraft/util/EnumHand.MAIN_HAND:Lnet/minecraft/util/EnumHand;
        //   436: invokevirtual   net/minecraft/client/multiplayer/PlayerControllerMP.func_187099_a:(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;
        //   439: ldc_w           ""
        //   442: invokevirtual   java/lang/String.length:()I
        //   445: pop2           
        //   446: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.mc:Lnet/minecraft/client/Minecraft;
        //   449: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //   452: getstatic       net/minecraft/util/EnumHand.MAIN_HAND:Lnet/minecraft/util/EnumHand;
        //   455: invokevirtual   net/minecraft/client/entity/EntityPlayerSP.func_184609_a:(Lnet/minecraft/util/EnumHand;)V
        //   458: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.llIIlllIllII:[I
        //   461: iconst_2       
        //   462: iaload         
        //   463: ireturn        
        //   464: iinc            lllllllllllllllIIlIIIIlllIlIIIll, 1
        //   467: ldc_w           ""
        //   470: invokevirtual   java/lang/String.length:()I
        //   473: pop            
        //   474: ldc             " "
        //   476: invokevirtual   java/lang/String.length:()I
        //   479: ineg           
        //   480: ldc             " "
        //   482: invokevirtual   java/lang/String.length:()I
        //   485: if_icmple       95
        //   488: bipush          34
        //   490: bipush          14
        //   492: ixor           
        //   493: sipush          156
        //   496: sipush          176
        //   499: ixor           
        //   500: iconst_m1      
        //   501: ixor           
        //   502: iand           
        //   503: ireturn        
        //   504: getstatic       me/zeroeightsix/kami/module/modules/combat/AutoBox.llIIlllIllII:[I
        //   507: iconst_1       
        //   508: iaload         
        //   509: ireturn        
        //    StackMapTable: 00 0A 1E 0F FF 00 30 00 06 07 00 02 07 01 8F 07 01 77 07 02 13 01 01 00 00 FF 00 AD 00 0B 07 00 02 07 01 8F 07 01 77 07 02 13 01 01 07 01 89 07 01 8F 07 01 89 07 01 77 01 00 00 1C FC 00 31 07 01 9D 24 17 F8 00 36 F8 00 27
        // 
        // The error that occurred was:
        // 
        // java.lang.NullPointerException
        //     at com.strobel.assembler.ir.StackMappingVisitor.push(StackMappingVisitor.java:290)
        //     at com.strobel.assembler.ir.StackMappingVisitor$InstructionAnalyzer.execute(StackMappingVisitor.java:833)
        //     at com.strobel.assembler.ir.StackMappingVisitor$InstructionAnalyzer.visit(StackMappingVisitor.java:398)
        //     at com.strobel.decompiler.ast.AstBuilder.performStackAnalysis(AstBuilder.java:2030)
        //     at com.strobel.decompiler.ast.AstBuilder.build(AstBuilder.java:108)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:211)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:782)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:675)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:552)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:519)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:161)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:150)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:125)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at com.strobel.decompiler.DecompilerDriver.decompileType(DecompilerDriver.java:330)
        //     at com.strobel.decompiler.DecompilerDriver.decompileJar(DecompilerDriver.java:251)
        //     at com.strobel.decompiler.DecompilerDriver.main(DecompilerDriver.java:126)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
    
    private static boolean lIIIlIlIIIIlIl(final int lllllllllllllllIIlIIIIllIIIIIIll, final int lllllllllllllllIIlIIIIllIIIIIIlI) {
        return lllllllllllllllIIlIIIIllIIIIIIll >= lllllllllllllllIIlIIIIllIIIIIIlI;
    }
    
    static {
        lIIIlIIIIlllIl();
        lIIIlIIIIlllII();
    }
    
    private static int lIIIlIlIIIIlll(final double n, final double n2) {
        return dcmpg(n, n2);
    }
    
    private static boolean lIIIlIIIIllllI(final Object lllllllllllllllIIlIIIIlIlllIIllI) {
        return lllllllllllllllIIlIIIIlIlllIIllI == null;
    }
    
    public AutoBox() {
        final Vec3d[] offsetsDefault = new Vec3d[AutoBox.llIIlllIllII[0]];
        offsetsDefault[AutoBox.llIIlllIllII[1]] = new Vec3d(0.0, 0.0, -1.0);
        offsetsDefault[AutoBox.llIIlllIllII[2]] = new Vec3d(1.0, 0.0, 0.0);
        offsetsDefault[AutoBox.llIIlllIllII[3]] = new Vec3d(0.0, 0.0, 1.0);
        offsetsDefault[AutoBox.llIIlllIllII[4]] = new Vec3d(-1.0, 0.0, 0.0);
        offsetsDefault[AutoBox.llIIlllIllII[5]] = new Vec3d(0.0, 1.0, -1.0);
        offsetsDefault[AutoBox.llIIlllIllII[6]] = new Vec3d(1.0, 1.0, 0.0);
        offsetsDefault[AutoBox.llIIlllIllII[7]] = new Vec3d(0.0, 1.0, 1.0);
        offsetsDefault[AutoBox.llIIlllIllII[8]] = new Vec3d(-1.0, 1.0, 0.0);
        offsetsDefault[AutoBox.llIIlllIllII[9]] = new Vec3d(0.0, 2.0, -1.0);
        offsetsDefault[AutoBox.llIIlllIllII[10]] = new Vec3d(1.0, 2.0, 0.0);
        offsetsDefault[AutoBox.llIIlllIllII[11]] = new Vec3d(0.0, 2.0, 1.0);
        offsetsDefault[AutoBox.llIIlllIllII[12]] = new Vec3d(-1.0, 2.0, 0.0);
        offsetsDefault[AutoBox.llIIlllIllII[13]] = new Vec3d(0.0, 3.0, -1.0);
        offsetsDefault[AutoBox.llIIlllIllII[14]] = new Vec3d(0.0, 3.0, 0.0);
        this.offsetsDefault = offsetsDefault;
        this.range = this.register(Settings.d(AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[1]], 5.5));
        this.blockPerTick = this.register(Settings.i(AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[2]], AutoBox.llIIlllIllII[5]));
        this.rotate = this.register(Settings.b(AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[3]], (boolean)(AutoBox.llIIlllIllII[2] != 0)));
        this.announceUsage = this.register(Settings.b(AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[4]], (boolean)(AutoBox.llIIlllIllII[2] != 0)));
        this.playerHotbarSlot = AutoBox.llIIlllIllII[15];
        this.lastHotbarSlot = AutoBox.llIIlllIllII[15];
        this.isSneaking = (AutoBox.llIIlllIllII[1] != 0);
        this.offsetStep = AutoBox.llIIlllIllII[1];
    }
    
    private static boolean lIIIlIIIlIIIII(final int lllllllllllllllIIlIIIIlIlllIIlII) {
        return lllllllllllllllIIlIIIIlIlllIIlII != 0;
    }
    
    private static String lIIIlIIIIllIll(final String lllllllllllllllIIlIIIIllIlIIlIlI, final String lllllllllllllllIIlIIIIllIlIIIlll) {
        try {
            final SecretKeySpec lllllllllllllllIIlIIIIllIlIIllIl = new SecretKeySpec(MessageDigest.getInstance("MD5").digest(lllllllllllllllIIlIIIIllIlIIIlll.getBytes(StandardCharsets.UTF_8)), "Blowfish");
            final Cipher lllllllllllllllIIlIIIIllIlIIllII = Cipher.getInstance("Blowfish");
            lllllllllllllllIIlIIIIllIlIIllII.init(AutoBox.llIIlllIllII[3], lllllllllllllllIIlIIIIllIlIIllIl);
            return new String(lllllllllllllllIIlIIIIllIlIIllII.doFinal(Base64.getDecoder().decode(lllllllllllllllIIlIIIIllIlIIlIlI.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }
        catch (Exception lllllllllllllllIIlIIIIllIlIIlIll) {
            lllllllllllllllIIlIIIIllIlIIlIll.printStackTrace();
            return null;
        }
    }
    
    private static boolean lIIIlIlIIIlIll(final Object lllllllllllllllIIlIIIIlIllllIlIl, final Object lllllllllllllllIIlIIIIlIllllIIll) {
        return lllllllllllllllIIlIIIIlIllllIlIl != lllllllllllllllIIlIIIIlIllllIIll;
    }
    
    private static boolean lIIIlIlIIIlIlI(final int lllllllllllllllIIlIIIIllIIIIlIII, final int lllllllllllllllIIlIIIIllIIIIIllI) {
        return lllllllllllllllIIlIIIIllIIIIlIII == lllllllllllllllIIlIIIIllIIIIIllI;
    }
    
    private static boolean lIIIlIlIIIIIll(final int lllllllllllllllIIlIIIIlIlllIIIIl) {
        return lllllllllllllllIIlIIIIlIlllIIIIl == 0;
    }
    
    private static boolean lIIIlIlIIIlIIl(final int lllllllllllllllIIlIIIIlIllIlIllI) {
        return lllllllllllllllIIlIIIIlIllIlIllI <= 0;
    }
    
    private int findObiInHotbar() {
        int lllllllllllllllIIlIIIIllIlllIlIl = AutoBox.llIIlllIllII[15];
        int lllllllllllllllIIlIIIIllIllllIIl = AutoBox.llIIlllIllII[1];
        while (lIIIlIlIIIIlII(lllllllllllllllIIlIIIIllIllllIIl, AutoBox.llIIlllIllII[10])) {
            final ItemStack lllllllllllllllIIlIIIIllIllllIll = Wrapper.getPlayer().field_71071_by.func_70301_a(lllllllllllllllIIlIIIIllIllllIIl);
            if (lIIIlIlIIIlIll(lllllllllllllllIIlIIIIllIllllIll, ItemStack.field_190927_a) && lIIIlIIIlIIIII((lllllllllllllllIIlIIIIllIllllIll.func_77973_b() instanceof ItemBlock) ? 1 : 0)) {
                final Block lllllllllllllllIIlIIIIllIlllllIl = ((ItemBlock)lllllllllllllllIIlIIIIllIllllIll.func_77973_b()).func_179223_d();
                if (lIIIlIIIlIIIII((lllllllllllllllIIlIIIIllIlllllIl instanceof BlockObsidian) ? 1 : 0)) {
                    lllllllllllllllIIlIIIIllIlllIlIl = lllllllllllllllIIlIIIIllIllllIIl;
                    "".length();
                    if (null != null) {
                        return "  ".length() & ~"  ".length();
                    }
                    break;
                }
            }
            ++lllllllllllllllIIlIIIIllIllllIIl;
            "".length();
            if (" ".length() > "   ".length()) {
                return (31 + 40 + 40 + 49 ^ 37 + 26 - 58 + 175) & (153 + 139 - 174 + 62 ^ 115 + 99 - 173 + 119 ^ -" ".length());
            }
        }
        return lllllllllllllllIIlIIIIllIlllIlIl;
    }
    
    @Override
    public void onUpdate() {
        if (!lIIIlIIIlIIIIl(AutoBox.mc.field_71439_g) || lIIIlIIIlIIIII(ModuleManager.isModuleEnabled(AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[6]]) ? 1 : 0)) {
            return;
        }
        this.findClosestTarget();
        if (lIIIlIIIIllllI(this.closestTarget)) {
            if (lIIIlIIIlIIIII(this.firstRun ? 1 : 0)) {
                this.firstRun = (AutoBox.llIIlllIllII[1] != 0);
                if (lIIIlIIIlIIIII(((boolean)this.announceUsage.getValue()) ? 1 : 0)) {
                    Command.sendChatMessage(AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[7]]);
                }
            }
            return;
        }
        if (lIIIlIIIlIIIII(this.firstRun ? 1 : 0)) {
            this.firstRun = (AutoBox.llIIlllIllII[1] != 0);
            this.lastTickTargetName = this.closestTarget.func_70005_c_();
            if (lIIIlIIIlIIIII(((boolean)this.announceUsage.getValue()) ? 1 : 0)) {
                Command.sendChatMessage(String.valueOf(new StringBuilder().append(AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[8]]).append(this.lastTickTargetName)));
                "".length();
                if ((0x9E ^ 0x9B) == 0x0) {
                    return;
                }
            }
        }
        else if (lIIIlIlIIIIIll(this.lastTickTargetName.equals(this.closestTarget.func_70005_c_()) ? 1 : 0)) {
            this.lastTickTargetName = this.closestTarget.func_70005_c_();
            this.offsetStep = AutoBox.llIIlllIllII[1];
            if (lIIIlIIIlIIIII(((boolean)this.announceUsage.getValue()) ? 1 : 0)) {
                Command.sendChatMessage(String.valueOf(new StringBuilder().append(AutoBox.llIIlllIlIll[AutoBox.llIIlllIllII[9]]).append(this.lastTickTargetName)));
            }
        }
        final List<Vec3d> lllllllllllllllIIlIIIIllllllIIll = new ArrayList<Vec3d>();
        Collections.addAll(lllllllllllllllIIlIIIIllllllIIll, this.offsetsDefault);
        "".length();
        int lllllllllllllllIIlIIIIllllllIIlI = AutoBox.llIIlllIllII[1];
        while (lIIIlIlIIIIlII(lllllllllllllllIIlIIIIllllllIIlI, this.blockPerTick.getValue())) {
            if (lIIIlIlIIIIlIl(this.offsetStep, lllllllllllllllIIlIIIIllllllIIll.size())) {
                this.offsetStep = AutoBox.llIIlllIllII[1];
                "".length();
                if (null != null) {
                    return;
                }
                break;
            }
            else {
                final BlockPos lllllllllllllllIIlIIIIllllllIlll = new BlockPos((Vec3d)lllllllllllllllIIlIIIIllllllIIll.get(this.offsetStep));
                final BlockPos lllllllllllllllIIlIIIIllllllIllI = new BlockPos(this.closestTarget.func_174791_d()).func_177977_b().func_177982_a(lllllllllllllllIIlIIIIllllllIlll.field_177962_a, lllllllllllllllIIlIIIIllllllIlll.field_177960_b, lllllllllllllllIIlIIIIllllllIlll.field_177961_c);
                boolean lllllllllllllllIIlIIIIllllllIlIl = AutoBox.llIIlllIllII[2] != 0;
                if (lIIIlIlIIIIIll(Wrapper.getWorld().func_180495_p(lllllllllllllllIIlIIIIllllllIllI).func_185904_a().func_76222_j() ? 1 : 0)) {
                    lllllllllllllllIIlIIIIllllllIlIl = (AutoBox.llIIlllIllII[1] != 0);
                }
                final char lllllllllllllllIIlIIIIlllllIIlll = (char)AutoBox.mc.field_71441_e.func_72839_b((Entity)null, new AxisAlignedBB(lllllllllllllllIIlIIIIllllllIllI)).iterator();
                while (lIIIlIIIlIIIII(((Iterator)lllllllllllllllIIlIIIIlllllIIlll).hasNext() ? 1 : 0)) {
                    final Entity lllllllllllllllIIlIIIIlllllllIII = ((Iterator<Entity>)lllllllllllllllIIlIIIIlllllIIlll).next();
                    if (lIIIlIlIIIIIll((lllllllllllllllIIlIIIIlllllllIII instanceof EntityItem) ? 1 : 0) && lIIIlIlIIIIIll((lllllllllllllllIIlIIIIlllllllIII instanceof EntityXPOrb) ? 1 : 0)) {
                        lllllllllllllllIIlIIIIllllllIlIl = (AutoBox.llIIlllIllII[1] != 0);
                        "".length();
                        if (-"   ".length() >= 0) {
                            return;
                        }
                        break;
                    }
                    else {
                        "".length();
                        if (" ".length() == 0) {
                            return;
                        }
                        continue;
                    }
                }
                if (lIIIlIIIlIIIII(lllllllllllllllIIlIIIIllllllIlIl ? 1 : 0) && lIIIlIIIlIIIII(this.placeBlock(lllllllllllllllIIlIIIIllllllIllI) ? 1 : 0)) {
                    ++lllllllllllllllIIlIIIIllllllIIlI;
                }
                this.offsetStep += AutoBox.llIIlllIllII[2];
                "".length();
                if (((0x2F ^ 0xB ^ (0x72 ^ 0xE)) & (31 + 77 - 105 + 198 ^ 120 + 115 - 212 + 122 ^ -" ".length())) >= " ".length()) {
                    return;
                }
                continue;
            }
        }
        if (lIIIlIlIIIIllI(lllllllllllllllIIlIIIIllllllIIlI)) {
            if (lIIIlIIIIlllll(this.lastHotbarSlot, this.playerHotbarSlot) && lIIIlIIIIlllll(this.playerHotbarSlot, AutoBox.llIIlllIllII[15])) {
                Wrapper.getPlayer().field_71071_by.field_70461_c = this.playerHotbarSlot;
                this.lastHotbarSlot = this.playerHotbarSlot;
            }
            if (lIIIlIIIlIIIII(this.isSneaking ? 1 : 0)) {
                AutoBox.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)AutoBox.mc.field_71439_g, CPacketEntityAction.Action.STOP_SNEAKING));
                this.isSneaking = (AutoBox.llIIlllIllII[1] != 0);
            }
        }
    }
    
    private static boolean lIIIlIlIIIIlII(final int lllllllllllllllIIlIIIIlIllllllll, final int lllllllllllllllIIlIIIIlIllllllIl) {
        return lllllllllllllllIIlIIIIlIllllllll < lllllllllllllllIIlIIIIlIllllllIl;
    }
    
    private static boolean lIIIlIIIlIIIIl(final Object lllllllllllllllIIlIIIIlIlllIllIl) {
        return lllllllllllllllIIlIIIIlIlllIllIl != null;
    }
    
    private static boolean lIIIlIlIIIIllI(final int lllllllllllllllIIlIIIIlIllIlIIIl) {
        return lllllllllllllllIIlIIIIlIllIlIIIl > 0;
    }
    
    private void findClosestTarget() {
        final List<EntityPlayer> lllllllllllllllIIlIIIIllIllIIIll = (List<EntityPlayer>)Wrapper.getWorld().field_73010_i;
        this.closestTarget = null;
        final boolean lllllllllllllllIIlIIIIllIllIIIII = (boolean)lllllllllllllllIIlIIIIllIllIIIll.iterator();
        while (lIIIlIIIlIIIII(((Iterator)lllllllllllllllIIlIIIIllIllIIIII).hasNext() ? 1 : 0)) {
            final EntityPlayer lllllllllllllllIIlIIIIllIllIIlIl = ((Iterator<EntityPlayer>)lllllllllllllllIIlIIIIllIllIIIII).next();
            if (lIIIlIlIIIlllI(lllllllllllllllIIlIIIIllIllIIlIl, AutoBox.mc.field_71439_g)) {
                "".length();
                if ("   ".length() <= "  ".length()) {
                    return;
                }
                continue;
            }
            else if (lIIIlIIIlIIIII(Friends.isFriend(lllllllllllllllIIlIIIIllIllIIlIl.func_70005_c_()) ? 1 : 0)) {
                "".length();
                if (" ".length() > "   ".length()) {
                    return;
                }
                continue;
            }
            else if (lIIIlIlIIIIIll(EntityUtil.isLiving((Entity)lllllllllllllllIIlIIIIllIllIIlIl) ? 1 : 0)) {
                "".length();
                if (((0x9 ^ 0x54) & ~(0xA ^ 0x57)) < 0) {
                    return;
                }
                continue;
            }
            else if (lIIIlIlIIIlIIl(lIIIlIlIIIllII(lllllllllllllllIIlIIIIllIllIIlIl.func_110143_aJ(), 0.0f))) {
                "".length();
                if ("  ".length() <= -" ".length()) {
                    return;
                }
                continue;
            }
            else {
                if (lIIIlIIIIllllI(this.closestTarget)) {
                    this.closestTarget = lllllllllllllllIIlIIIIllIllIIlIl;
                    "".length();
                    if ("   ".length() <= 0) {
                        return;
                    }
                }
                else if (lIIIlIlIIIllll(lIIIlIlIIIllIl(Wrapper.getPlayer().func_70032_d((Entity)lllllllllllllllIIlIIIIllIllIIlIl), Wrapper.getPlayer().func_70032_d((Entity)this.closestTarget)))) {
                    "".length();
                    if ((0x23 ^ 0x27) < "  ".length()) {
                        return;
                    }
                    continue;
                }
                else {
                    this.closestTarget = lllllllllllllllIIlIIIIllIllIIlIl;
                }
                "".length();
                if (null != null) {
                    return;
                }
                continue;
            }
        }
    }
}
