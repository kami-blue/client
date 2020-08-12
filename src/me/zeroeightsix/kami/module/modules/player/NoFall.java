// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.player;

import net.minecraft.client.Minecraft;
import me.zero.alpine.listener.EventHook;
import java.util.function.Predicate;
import me.zeroeightsix.kami.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.setting.Setting;
import kotlin.Metadata;
import me.zeroeightsix.kami.module.Module;

@Info(name = "NoFall", category = Category.PLAYER, description = "Prevents fall damage")
@Metadata(mv = { 1, 1, 15 }, bv = { 1, 0, 3 }, k = 1, d1 = { "\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001:\u0001\u0013B\u0005¢\u0006\u0002\u0010\u0002J\b\u0010\u0011\u001a\u00020\u0012H\u0016R2\u0010\u0003\u001a&\u0012\f\u0012\n \u0006*\u0004\u0018\u00010\u00050\u0005 \u0006*\u0012\u0012\f\u0012\n \u0006*\u0004\u0018\u00010\u00050\u0005\u0018\u00010\u00040\u0004X\u0082\u0004¢\u0006\u0002\n\u0000R2\u0010\u0007\u001a&\u0012\f\u0012\n \u0006*\u0004\u0018\u00010\b0\b \u0006*\u0012\u0012\f\u0012\n \u0006*\u0004\u0018\u00010\b0\b\u0018\u00010\u00040\u0004X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e¢\u0006\u0002\n\u0000R2\u0010\u000b\u001a&\u0012\f\u0012\n \u0006*\u0004\u0018\u00010\f0\f \u0006*\u0012\u0012\f\u0012\n \u0006*\u0004\u0018\u00010\f0\f\u0018\u00010\u00040\u0004X\u0082\u0004¢\u0006\u0002\n\u0000R2\u0010\r\u001a&\u0012\f\u0012\n \u0006*\u0004\u0018\u00010\u00050\u0005 \u0006*\u0012\u0012\f\u0012\n \u0006*\u0004\u0018\u00010\u00050\u0005\u0018\u00010\u00040\u0004X\u0082\u0004¢\u0006\u0002\n\u0000R\u001e\u0010\u000e\u001a\u0010\u0012\f\u0012\n \u0006*\u0004\u0018\u00010\u00100\u00100\u000f8\u0002X\u0083\u0004¢\u0006\u0002\n\u0000¨\u0006\u0014" }, d2 = { "Lme/zeroeightsix/kami/module/modules/player/NoFall;", "Lme/zeroeightsix/kami/module/Module;", "()V", "distance", "Lme/zeroeightsix/kami/setting/Setting;", "", "kotlin.jvm.PlatformType", "fallMode", "Lme/zeroeightsix/kami/module/modules/player/NoFall$FallMode;", "last", "", "pickup", "", "pickupDelay", "sendListener", "Lme/zero/alpine/listener/Listener;", "Lme/zeroeightsix/kami/event/events/PacketEvent$Send;", "onUpdate", "", "FallMode", "kamiblue" })
public final class NoFall extends Module
{
    private final Setting<FallMode> fallMode;
    private final Setting<Boolean> pickup;
    private final Setting<Integer> distance;
    private final Setting<Integer> pickupDelay;
    private long last;
    @EventHandler
    private final Listener<PacketEvent.Send> sendListener;
    
    @Override
    public void onUpdate() {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     1: getfield        me/zeroeightsix/kami/module/modules/player/NoFall.fallMode:Lme/zeroeightsix/kami/setting/Setting;
        //     4: dup            
        //     5: ldc             "fallMode"
        //     7: invokestatic    kotlin/jvm/internal/Intrinsics.checkExpressionValueIsNotNull:(Ljava/lang/Object;Ljava/lang/String;)V
        //    10: invokevirtual   me/zeroeightsix/kami/setting/Setting.getValue:()Ljava/lang/Object;
        //    13: checkcast       Lme/zeroeightsix/kami/module/modules/player/NoFall$FallMode;
        //    16: getstatic       me/zeroeightsix/kami/module/modules/player/NoFall$FallMode.BUCKET:Lme/zeroeightsix/kami/module/modules/player/NoFall$FallMode;
        //    19: if_acmpne       398
        //    22: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //    25: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //    28: getfield        net/minecraft/client/entity/EntityPlayerSP.field_71093_bK:I
        //    31: iconst_m1      
        //    32: if_icmpeq       398
        //    35: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //    38: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //    41: getfield        net/minecraft/client/entity/EntityPlayerSP.field_71075_bZ:Lnet/minecraft/entity/player/PlayerCapabilities;
        //    44: getfield        net/minecraft/entity/player/PlayerCapabilities.field_75098_d:Z
        //    47: ifne            398
        //    50: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //    53: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //    56: getfield        net/minecraft/client/entity/EntityPlayerSP.field_70143_R:F
        //    59: aload_0         /* this */
        //    60: getfield        me/zeroeightsix/kami/module/modules/player/NoFall.distance:Lme/zeroeightsix/kami/setting/Setting;
        //    63: dup            
        //    64: ldc             "distance"
        //    66: invokestatic    kotlin/jvm/internal/Intrinsics.checkExpressionValueIsNotNull:(Ljava/lang/Object;Ljava/lang/String;)V
        //    69: invokevirtual   me/zeroeightsix/kami/setting/Setting.getValue:()Ljava/lang/Object;
        //    72: dup            
        //    73: ldc             "distance.value"
        //    75: invokestatic    kotlin/jvm/internal/Intrinsics.checkExpressionValueIsNotNull:(Ljava/lang/Object;Ljava/lang/String;)V
        //    78: checkcast       Ljava/lang/Number;
        //    81: invokevirtual   java/lang/Number.floatValue:()F
        //    84: invokestatic    java/lang/Float.compare:(FF)I
        //    87: iflt            398
        //    90: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //    93: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //    96: checkcast       Lnet/minecraft/entity/Entity;
        //    99: invokestatic    me/zeroeightsix/kami/util/EntityUtil.isAboveWater:(Lnet/minecraft/entity/Entity;)Z
        //   102: ifne            398
        //   105: invokestatic    java/lang/System.currentTimeMillis:()J
        //   108: aload_0         /* this */
        //   109: getfield        me/zeroeightsix/kami/module/modules/player/NoFall.last:J
        //   112: lsub           
        //   113: bipush          100
        //   115: i2l            
        //   116: lcmp           
        //   117: ifle            398
        //   120: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //   123: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //   126: dup            
        //   127: ldc             "mc.player"
        //   129: invokestatic    kotlin/jvm/internal/Intrinsics.checkExpressionValueIsNotNull:(Ljava/lang/Object;Ljava/lang/String;)V
        //   132: invokevirtual   net/minecraft/client/entity/EntityPlayerSP.func_174791_d:()Lnet/minecraft/util/math/Vec3d;
        //   135: astore_1        /* posVec */
        //   136: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //   139: getfield        net/minecraft/client/Minecraft.field_71441_e:Lnet/minecraft/client/multiplayer/WorldClient;
        //   142: aload_1         /* posVec */
        //   143: aload_1         /* posVec */
        //   144: dconst_0       
        //   145: ldc2_w          -5.33
        //   148: dconst_0       
        //   149: invokevirtual   net/minecraft/util/math/Vec3d.func_72441_c:(DDD)Lnet/minecraft/util/math/Vec3d;
        //   152: iconst_1       
        //   153: iconst_1       
        //   154: iconst_0       
        //   155: invokevirtual   net/minecraft/client/multiplayer/WorldClient.func_147447_a:(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;
        //   158: astore_2        /* result */
        //   159: aload_2         /* result */
        //   160: ifnull          349
        //   163: aload_2         /* result */
        //   164: getfield        net/minecraft/util/math/RayTraceResult.field_72313_a:Lnet/minecraft/util/math/RayTraceResult$Type;
        //   167: getstatic       net/minecraft/util/math/RayTraceResult$Type.BLOCK:Lnet/minecraft/util/math/RayTraceResult$Type;
        //   170: if_acmpne       349
        //   173: getstatic       net/minecraft/util/EnumHand.MAIN_HAND:Lnet/minecraft/util/EnumHand;
        //   176: astore_3        /* hand */
        //   177: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //   180: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //   183: dup            
        //   184: ldc             "mc.player"
        //   186: invokestatic    kotlin/jvm/internal/Intrinsics.checkExpressionValueIsNotNull:(Ljava/lang/Object;Ljava/lang/String;)V
        //   189: invokevirtual   net/minecraft/client/entity/EntityPlayerSP.func_184592_cb:()Lnet/minecraft/item/ItemStack;
        //   192: invokevirtual   net/minecraft/item/ItemStack.func_77973_b:()Lnet/minecraft/item/Item;
        //   195: getstatic       net/minecraft/init/Items.field_151131_as:Lnet/minecraft/item/Item;
        //   198: if_acmpne       208
        //   201: getstatic       net/minecraft/util/EnumHand.OFF_HAND:Lnet/minecraft/util/EnumHand;
        //   204: astore_3        /* hand */
        //   205: goto            309
        //   208: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //   211: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //   214: dup            
        //   215: ldc             "mc.player"
        //   217: invokestatic    kotlin/jvm/internal/Intrinsics.checkExpressionValueIsNotNull:(Ljava/lang/Object;Ljava/lang/String;)V
        //   220: invokevirtual   net/minecraft/client/entity/EntityPlayerSP.func_184614_ca:()Lnet/minecraft/item/ItemStack;
        //   223: invokevirtual   net/minecraft/item/ItemStack.func_77973_b:()Lnet/minecraft/item/Item;
        //   226: getstatic       net/minecraft/init/Items.field_151131_as:Lnet/minecraft/item/Item;
        //   229: if_acmpeq       309
        //   232: iconst_0       
        //   233: istore          4
        //   235: bipush          8
        //   237: istore          5
        //   239: iload           4
        //   241: iload           5
        //   243: if_icmpgt       308
        //   246: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //   249: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //   252: getfield        net/minecraft/client/entity/EntityPlayerSP.field_71071_by:Lnet/minecraft/entity/player/InventoryPlayer;
        //   255: iload           i
        //   257: invokevirtual   net/minecraft/entity/player/InventoryPlayer.func_70301_a:(I)Lnet/minecraft/item/ItemStack;
        //   260: invokevirtual   net/minecraft/item/ItemStack.func_77973_b:()Lnet/minecraft/item/Item;
        //   263: getstatic       net/minecraft/init/Items.field_151131_as:Lnet/minecraft/item/Item;
        //   266: if_acmpne       302
        //   269: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //   272: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //   275: getfield        net/minecraft/client/entity/EntityPlayerSP.field_71071_by:Lnet/minecraft/entity/player/InventoryPlayer;
        //   278: iload           i
        //   280: putfield        net/minecraft/entity/player/InventoryPlayer.field_70461_c:I
        //   283: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //   286: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //   289: ldc             90.0
        //   291: putfield        net/minecraft/client/entity/EntityPlayerSP.field_70125_A:F
        //   294: aload_0         /* this */
        //   295: invokestatic    java/lang/System.currentTimeMillis:()J
        //   298: putfield        me/zeroeightsix/kami/module/modules/player/NoFall.last:J
        //   301: return         
        //   302: iinc            i, 1
        //   305: goto            239
        //   308: return         
        //   309: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //   312: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //   315: ldc             90.0
        //   317: putfield        net/minecraft/client/entity/EntityPlayerSP.field_70125_A:F
        //   320: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //   323: getfield        net/minecraft/client/Minecraft.field_71442_b:Lnet/minecraft/client/multiplayer/PlayerControllerMP;
        //   326: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //   329: getfield        net/minecraft/client/Minecraft.field_71439_g:Lnet/minecraft/client/entity/EntityPlayerSP;
        //   332: checkcast       Lnet/minecraft/entity/player/EntityPlayer;
        //   335: invokestatic    me/zeroeightsix/kami/module/modules/player/NoFall.access$getMc$p$s-1984916852:()Lnet/minecraft/client/Minecraft;
        //   338: getfield        net/minecraft/client/Minecraft.field_71441_e:Lnet/minecraft/client/multiplayer/WorldClient;
        //   341: checkcast       Lnet/minecraft/world/World;
        //   344: aload_3         /* hand */
        //   345: invokevirtual   net/minecraft/client/multiplayer/PlayerControllerMP.func_187101_a:(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;
        //   348: pop            
        //   349: aload_0         /* this */
        //   350: getfield        me/zeroeightsix/kami/module/modules/player/NoFall.pickup:Lme/zeroeightsix/kami/setting/Setting;
        //   353: dup            
        //   354: ldc             "pickup"
        //   356: invokestatic    kotlin/jvm/internal/Intrinsics.checkExpressionValueIsNotNull:(Ljava/lang/Object;Ljava/lang/String;)V
        //   359: invokevirtual   me/zeroeightsix/kami/setting/Setting.getValue:()Ljava/lang/Object;
        //   362: dup            
        //   363: ldc             "pickup.value"
        //   365: invokestatic    kotlin/jvm/internal/Intrinsics.checkExpressionValueIsNotNull:(Ljava/lang/Object;Ljava/lang/String;)V
        //   368: checkcast       Ljava/lang/Boolean;
        //   371: invokevirtual   java/lang/Boolean.booleanValue:()Z
        //   374: ifeq            398
        //   377: new             Ljava/lang/Thread;
        //   380: dup            
        //   381: new             Lme/zeroeightsix/kami/module/modules/player/NoFall$onUpdate$1;
        //   384: dup            
        //   385: aload_0         /* this */
        //   386: invokespecial   me/zeroeightsix/kami/module/modules/player/NoFall$onUpdate$1.<init>:(Lme/zeroeightsix/kami/module/modules/player/NoFall;)V
        //   389: checkcast       Ljava/lang/Runnable;
        //   392: invokespecial   java/lang/Thread.<init>:(Ljava/lang/Runnable;)V
        //   395: invokevirtual   java/lang/Thread.start:()V
        //   398: return         
        //    StackMapTable: 00 07 FE 00 D0 07 00 9E 07 00 AA 07 00 B5 FD 00 1E 01 01 3E 05 F9 00 00 FA 00 27 F9 00 30
        // 
        // The error that occurred was:
        // 
        // java.lang.NullPointerException
        //     at com.strobel.decompiler.ast.AstBuilder.convertLocalVariables(AstBuilder.java:2895)
        //     at com.strobel.decompiler.ast.AstBuilder.performStackAnalysis(AstBuilder.java:2445)
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
    
    public NoFall() {
        this.fallMode = this.register(Settings.e("Mode", FallMode.PACKET));
        this.pickup = this.register(Settings.booleanBuilder("Pickup").withValue(true).withVisibility((Predicate<Boolean>)new NoFall$pickup.NoFall$pickup$1(this)).build());
        this.distance = this.register(Settings.integerBuilder("Distance").withValue(3).withMinimum(1).withMaximum(10).withVisibility((Predicate<Integer>)new NoFall$distance.NoFall$distance$1(this)).build());
        this.pickupDelay = this.register(Settings.integerBuilder("Pickup Delay").withValue(300).withMinimum(100).withMaximum(1000).withVisibility((Predicate<Integer>)new NoFall$pickupDelay.NoFall$pickupDelay$1(this)).build());
        this.sendListener = new Listener<PacketEvent.Send>((EventHook<PacketEvent.Send>)new NoFall$sendListener.NoFall$sendListener$1(this), (Predicate<PacketEvent.Send>[])new Predicate[0]);
    }
    
    @Metadata(mv = { 1, 1, 15 }, bv = { 1, 0, 3 }, k = 1, d1 = { "\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0004\b\u0082\u0001\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004¨\u0006\u0005" }, d2 = { "Lme/zeroeightsix/kami/module/modules/player/NoFall$FallMode;", "", "(Ljava/lang/String;I)V", "BUCKET", "PACKET", "kamiblue" })
    private enum FallMode
    {
        BUCKET, 
        PACKET;
    }
}
