// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.command.Command;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Collection;
import me.zeroeightsix.kami.util.BlockInteractionHelper;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.init.Blocks;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.RayTraceResult;
import java.util.Iterator;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import me.zeroeightsix.kami.module.modules.chat.AutoGG;
import me.zeroeightsix.kami.module.ModuleManager;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import me.zeroeightsix.kami.util.Friends;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.item.ItemTool;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemStack;
import java.util.Comparator;
import net.minecraft.entity.item.EntityEnderCrystal;
import me.zeroeightsix.kami.util.KamiTessellator;
import java.awt.Color;
import me.zeroeightsix.kami.event.events.RenderEvent;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.CombatRules;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.Explosion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import java.util.function.Predicate;
import net.minecraft.network.play.client.CPacketPlayer;
import me.zeroeightsix.kami.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "CrystalAura", category = Category.COMBAT)
public class CrystalAura extends Module
{
    private static boolean togglePitch;
    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;
    private Setting<Boolean> place;
    private Setting<Boolean> explode;
    private Setting<Boolean> autoSwitch;
    private Setting<Boolean> antiWeakness;
    private Setting<Integer> hitTickDelay;
    private Setting<Double> hitRange;
    private Setting<Double> placeRange;
    private Setting<Double> minDamage;
    private Setting<Boolean> spoofRotations;
    private Setting<Boolean> rayTraceHit;
    private Setting<RenderMode> renderMode;
    private Setting<Integer> red;
    private Setting<Integer> green;
    private Setting<Integer> blue;
    private Setting<Integer> alpha;
    private Setting<Boolean> announceUsage;
    private BlockPos renderBlock;
    private EntityPlayer target;
    private boolean switchCooldown;
    private boolean isAttacking;
    private int oldSlot;
    private int newSlot;
    private int hitDelayCounter;
    @EventHandler
    private Listener<PacketEvent.Send> packetListener;
    
    public CrystalAura() {
        this.place = this.register(Settings.b("Place", true));
        this.explode = this.register(Settings.b("Explode", true));
        this.autoSwitch = this.register(Settings.b("Auto Switch", true));
        this.antiWeakness = this.register(Settings.b("Anti Weakness", true));
        this.hitTickDelay = this.register((Setting<Integer>)Settings.integerBuilder("Hit Delay").withMinimum(0).withValue(4).withMaximum(20).build());
        this.hitRange = this.register((Setting<Double>)Settings.doubleBuilder("Hit Range").withMinimum(0.0).withValue(5.5).build());
        this.placeRange = this.register((Setting<Double>)Settings.doubleBuilder("Place Range").withMinimum(0.0).withValue(3.5).build());
        this.minDamage = this.register((Setting<Double>)Settings.doubleBuilder("Min Damage").withMinimum(0.0).withValue(2.0).withMaximum(20.0).build());
        this.spoofRotations = this.register(Settings.b("Spoof Rotations", false));
        this.rayTraceHit = this.register(Settings.b("RayTraceHit", false));
        this.renderMode = this.register(Settings.e("Render Mode", RenderMode.UP));
        this.red = this.register((Setting<Integer>)Settings.integerBuilder("Red").withMinimum(0).withValue(104).withMaximum(255).build());
        this.green = this.register((Setting<Integer>)Settings.integerBuilder("Green").withMinimum(0).withValue(12).withMaximum(255).build());
        this.blue = this.register((Setting<Integer>)Settings.integerBuilder("Blue").withMinimum(0).withValue(35).withMaximum(255).build());
        this.alpha = this.register((Setting<Integer>)Settings.integerBuilder("Alpha").withMinimum(0).withValue(169).withMaximum(255).build());
        this.announceUsage = this.register(Settings.b("Announce Usage", true));
        this.switchCooldown = false;
        this.isAttacking = false;
        this.oldSlot = -1;
        Packet packet;
        this.packetListener = new Listener<PacketEvent.Send>(event -> {
            if (!(!this.spoofRotations.getValue())) {
                packet = event.getPacket();
                if (packet instanceof CPacketPlayer && CrystalAura.isSpoofingAngles) {
                    ((CPacketPlayer)packet).field_149476_e = (float)CrystalAura.yaw;
                    ((CPacketPlayer)packet).field_149473_f = (float)CrystalAura.pitch;
                }
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
    }
    
    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(CrystalAura.mc.field_71439_g.field_70165_t), Math.floor(CrystalAura.mc.field_71439_g.field_70163_u), Math.floor(CrystalAura.mc.field_71439_g.field_70161_v));
    }
    
    static float calculateDamage(final double posX, final double posY, final double posZ, final Entity entity) {
        final float doubleExplosionSize = 12.0f;
        final double distancedsize = entity.func_70011_f(posX, posY, posZ) / doubleExplosionSize;
        final Vec3d vec3d = new Vec3d(posX, posY, posZ);
        final double blockDensity = entity.field_70170_p.func_72842_a(vec3d, entity.func_174813_aQ());
        final double v = (1.0 - distancedsize) * blockDensity;
        final float damage = (float)(int)((v * v + v) / 2.0 * 7.0 * doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase)entity, getDamageMultiplied(damage), new Explosion((World)CrystalAura.mc.field_71441_e, (Entity)null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float)finald;
    }
    
    private static float getBlastReduction(final EntityLivingBase entity, float damage, final Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            final EntityPlayer ep = (EntityPlayer)entity;
            final DamageSource ds = DamageSource.func_94539_a(explosion);
            damage = CombatRules.func_189427_a(damage, (float)ep.func_70658_aO(), (float)ep.func_110148_a(SharedMonsterAttributes.field_189429_h).func_111126_e());
            final int k = EnchantmentHelper.func_77508_a(ep.func_184193_aE(), ds);
            final float f = MathHelper.func_76131_a((float)k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.func_70644_a(MobEffects.field_76429_m)) {
                damage -= damage / 4.0f;
            }
            return damage;
        }
        damage = CombatRules.func_189427_a(damage, (float)entity.func_70658_aO(), (float)entity.func_110148_a(SharedMonsterAttributes.field_189429_h).func_111126_e());
        return damage;
    }
    
    private static float getDamageMultiplied(final float damage) {
        final int diff = CrystalAura.mc.field_71441_e.func_175659_aa().func_151525_a();
        return damage * ((diff == 0) ? 0.0f : ((diff == 2) ? 1.0f : ((diff == 1) ? 0.5f : 1.5f)));
    }
    
    private static void setYawAndPitch(final float yaw1, final float pitch1) {
        CrystalAura.yaw = yaw1;
        CrystalAura.pitch = pitch1;
        CrystalAura.isSpoofingAngles = true;
    }
    
    private static void resetRotation() {
        if (CrystalAura.isSpoofingAngles) {
            CrystalAura.yaw = CrystalAura.mc.field_71439_g.field_70177_z;
            CrystalAura.pitch = CrystalAura.mc.field_71439_g.field_70125_A;
            CrystalAura.isSpoofingAngles = false;
        }
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        if (this.renderBlock != null && !this.renderMode.getValue().equals(RenderMode.NONE)) {
            this.drawBlock(this.renderBlock, this.red.getValue(), this.green.getValue(), this.blue.getValue());
        }
    }
    
    private void drawBlock(final BlockPos blockPos, final int r, final int g, final int b) {
        final Color color = new Color(r, g, b, this.alpha.getValue());
        KamiTessellator.prepare(7);
        if (this.renderMode.getValue().equals(RenderMode.UP)) {
            KamiTessellator.drawBox(blockPos, color.getRGB(), 2);
        }
        else if (this.renderMode.getValue().equals(RenderMode.BLOCK)) {
            KamiTessellator.drawBox(blockPos, color.getRGB(), 63);
        }
        KamiTessellator.release();
    }
    
    @Override
    public void onUpdate() {
        if (CrystalAura.mc.field_71439_g == null) {
            return;
        }
        final EntityEnderCrystal crystal = (EntityEnderCrystal)CrystalAura.mc.field_71441_e.field_72996_f.stream().filter(entity -> entity instanceof EntityEnderCrystal).map(entity -> entity).min(Comparator.comparing(c -> CrystalAura.mc.field_71439_g.func_70032_d(c))).orElse(null);
        if (this.explode.getValue() && crystal != null && CrystalAura.mc.field_71439_g.func_70032_d((Entity)crystal) <= this.hitRange.getValue() && this.rayTraceHitCheck(crystal)) {
            if (this.hitDelayCounter >= this.hitTickDelay.getValue()) {
                this.hitDelayCounter = 0;
                if (this.antiWeakness.getValue() && CrystalAura.mc.field_71439_g.func_70644_a(MobEffects.field_76437_t)) {
                    if (!this.isAttacking) {
                        this.oldSlot = CrystalAura.mc.field_71439_g.field_71071_by.field_70461_c;
                        this.isAttacking = true;
                    }
                    this.newSlot = -1;
                    for (int i = 0; i < 9; ++i) {
                        final ItemStack stack = CrystalAura.mc.field_71439_g.field_71071_by.func_70301_a(i);
                        if (stack != ItemStack.field_190927_a) {
                            if (stack.func_77973_b() instanceof ItemSword) {
                                this.newSlot = i;
                                break;
                            }
                            if (stack.func_77973_b() instanceof ItemTool) {
                                this.newSlot = i;
                                break;
                            }
                        }
                    }
                    if (this.newSlot != -1) {
                        CrystalAura.mc.field_71439_g.field_71071_by.field_70461_c = this.newSlot;
                        this.switchCooldown = true;
                    }
                }
                this.lookAtPacket(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, (EntityPlayer)CrystalAura.mc.field_71439_g);
                CrystalAura.mc.field_71442_b.func_78764_a((EntityPlayer)CrystalAura.mc.field_71439_g, (Entity)crystal);
                CrystalAura.mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
                return;
            }
            ++this.hitDelayCounter;
        }
        else {
            resetRotation();
            if (this.oldSlot != -1) {
                CrystalAura.mc.field_71439_g.field_71071_by.field_70461_c = this.oldSlot;
                this.oldSlot = -1;
            }
            this.isAttacking = false;
            int crystalSlot = (CrystalAura.mc.field_71439_g.func_184614_ca().func_77973_b() == Items.field_185158_cP) ? CrystalAura.mc.field_71439_g.field_71071_by.field_70461_c : -1;
            if (crystalSlot == -1) {
                for (int l = 0; l < 9; ++l) {
                    if (CrystalAura.mc.field_71439_g.field_71071_by.func_70301_a(l).func_77973_b() == Items.field_185158_cP) {
                        crystalSlot = l;
                        break;
                    }
                }
            }
            boolean offhand = false;
            if (CrystalAura.mc.field_71439_g.func_184592_cb().func_77973_b() == Items.field_185158_cP) {
                offhand = true;
            }
            else if (crystalSlot == -1) {
                return;
            }
            final List<Entity> entities = (List<Entity>)CrystalAura.mc.field_71441_e.field_73010_i.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.func_70005_c_())).sorted((entity1, entity2) -> Float.compare(CrystalAura.mc.field_71439_g.func_70032_d(entity1), CrystalAura.mc.field_71439_g.func_70032_d(entity2))).collect(Collectors.toList());
            final List<BlockPos> blocks = this.findCrystalBlocks();
            BlockPos targetBlock = null;
            double targetBlockDamage = 0.0;
            this.target = null;
            for (final Entity entity3 : entities) {
                if (entity3 == CrystalAura.mc.field_71439_g) {
                    continue;
                }
                if (!(entity3 instanceof EntityPlayer)) {
                    continue;
                }
                final EntityPlayer testTarget = (EntityPlayer)entity3;
                if (testTarget.field_70128_L) {
                    continue;
                }
                if (testTarget.func_110143_aJ() <= 0.0f) {
                    continue;
                }
                for (final BlockPos blockPos : blocks) {
                    if (testTarget.func_174818_b(blockPos) >= 169.0) {
                        continue;
                    }
                    final double targetDamage = calculateDamage(blockPos.field_177962_a + 0.5, blockPos.field_177960_b + 1, blockPos.field_177961_c + 0.5, (Entity)testTarget);
                    final double selfDamage = calculateDamage(blockPos.field_177962_a + 0.5, blockPos.field_177960_b + 1, blockPos.field_177961_c + 0.5, (Entity)CrystalAura.mc.field_71439_g);
                    final float healthTarget = testTarget.func_110143_aJ() + testTarget.func_110139_bj();
                    final float healthSelf = CrystalAura.mc.field_71439_g.func_110143_aJ() + CrystalAura.mc.field_71439_g.func_110139_bj();
                    if (targetDamage < this.minDamage.getValue()) {
                        continue;
                    }
                    if (selfDamage >= healthSelf - 0.5) {
                        continue;
                    }
                    if (selfDamage > targetDamage && targetDamage < healthTarget) {
                        continue;
                    }
                    if (targetDamage <= targetBlockDamage) {
                        continue;
                    }
                    targetBlock = blockPos;
                    targetBlockDamage = targetDamage;
                    this.target = testTarget;
                }
                if (this.target != null) {
                    break;
                }
            }
            if (this.target == null) {
                this.renderBlock = null;
                resetRotation();
                return;
            }
            this.renderBlock = targetBlock;
            if (ModuleManager.getModuleByName("AutoGG").isEnabled()) {
                final AutoGG autoGG = (AutoGG)ModuleManager.getModuleByName("AutoGG");
                autoGG.addTargetedPlayer(this.target.func_70005_c_());
            }
            if (this.place.getValue()) {
                if (!offhand && CrystalAura.mc.field_71439_g.field_71071_by.field_70461_c != crystalSlot) {
                    if (this.autoSwitch.getValue()) {
                        CrystalAura.mc.field_71439_g.field_71071_by.field_70461_c = crystalSlot;
                        resetRotation();
                        this.switchCooldown = true;
                    }
                    return;
                }
                this.lookAtPacket(targetBlock.field_177962_a + 0.5, targetBlock.field_177960_b - 0.5, targetBlock.field_177961_c + 0.5, (EntityPlayer)CrystalAura.mc.field_71439_g);
                final RayTraceResult result = CrystalAura.mc.field_71441_e.func_72933_a(new Vec3d(CrystalAura.mc.field_71439_g.field_70165_t, CrystalAura.mc.field_71439_g.field_70163_u + CrystalAura.mc.field_71439_g.func_70047_e(), CrystalAura.mc.field_71439_g.field_70161_v), new Vec3d(targetBlock.field_177962_a + 0.5, targetBlock.field_177960_b - 0.5, targetBlock.field_177961_c + 0.5));
                EnumFacing f;
                if (result == null || result.field_178784_b == null) {
                    f = EnumFacing.UP;
                }
                else {
                    f = result.field_178784_b;
                }
                if (this.switchCooldown) {
                    this.switchCooldown = false;
                    return;
                }
                CrystalAura.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayerTryUseItemOnBlock(targetBlock, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
            }
            if (this.spoofRotations.getValue() && CrystalAura.isSpoofingAngles) {
                if (CrystalAura.togglePitch) {
                    final EntityPlayerSP field_71439_g = CrystalAura.mc.field_71439_g;
                    field_71439_g.field_70125_A += (float)4.0E-4;
                    CrystalAura.togglePitch = false;
                }
                else {
                    final EntityPlayerSP field_71439_g2 = CrystalAura.mc.field_71439_g;
                    field_71439_g2.field_70125_A -= (float)4.0E-4;
                    CrystalAura.togglePitch = true;
                }
            }
        }
    }
    
    private boolean rayTraceHitCheck(final EntityEnderCrystal crystal) {
        return !this.rayTraceHit.getValue() || CrystalAura.mc.field_71439_g.func_70685_l((Entity)crystal);
    }
    
    private void lookAtPacket(final double px, final double py, final double pz, final EntityPlayer me) {
        final double[] v = EntityUtil.calculateLookAt(px, py, pz, me);
        setYawAndPitch((float)v[0], (float)v[1]);
    }
    
    private boolean canPlaceCrystal(final BlockPos blockPos) {
        final BlockPos boost = blockPos.func_177982_a(0, 1, 0);
        final BlockPos boost2 = blockPos.func_177982_a(0, 2, 0);
        return (CrystalAura.mc.field_71441_e.func_180495_p(blockPos).func_177230_c() == Blocks.field_150357_h || CrystalAura.mc.field_71441_e.func_180495_p(blockPos).func_177230_c() == Blocks.field_150343_Z) && CrystalAura.mc.field_71441_e.func_180495_p(boost).func_177230_c() == Blocks.field_150350_a && CrystalAura.mc.field_71441_e.func_180495_p(boost2).func_177230_c() == Blocks.field_150350_a && CrystalAura.mc.field_71441_e.func_72872_a((Class)Entity.class, new AxisAlignedBB(boost)).isEmpty() && CrystalAura.mc.field_71441_e.func_72872_a((Class)Entity.class, new AxisAlignedBB(boost2)).isEmpty();
    }
    
    private List<BlockPos> findCrystalBlocks() {
        final NonNullList<BlockPos> positions = (NonNullList<BlockPos>)NonNullList.func_191196_a();
        positions.addAll((Collection)BlockInteractionHelper.getSphere(getPlayerPos(), this.placeRange.getValue().floatValue(), this.placeRange.getValue().intValue(), false, true, 0).stream().filter((Predicate<? super Object>)this::canPlaceCrystal).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList()));
        return (List<BlockPos>)positions;
    }
    
    public void onEnable() {
        if (this.announceUsage.getValue()) {
            Command.sendChatMessage("[CrystalAura] " + ChatFormatting.GREEN.toString() + "Enabled!");
        }
        this.hitDelayCounter = 0;
    }
    
    public void onDisable() {
        this.renderBlock = null;
        this.target = null;
        resetRotation();
        if (this.announceUsage.getValue()) {
            Command.sendChatMessage("[CrystalAura] " + ChatFormatting.RED.toString() + "Disabled!");
        }
    }
    
    @Override
    public String getHudInfo() {
        if (this.target == null) {
            return "";
        }
        return this.target.func_70005_c_().toUpperCase();
    }
    
    static {
        CrystalAura.togglePitch = false;
    }
    
    private enum RenderMode
    {
        UP, 
        BLOCK, 
        NONE;
    }
}
