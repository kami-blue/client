// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.crystal;

import net.minecraft.potion.Potion;
import net.minecraft.util.math.MathHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.CombatRules;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.Explosion;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.init.Blocks;
import me.zeroeightsix.kami.module.modules.render.Tracers;
import me.zeroeightsix.kami.util.KamiTessellator;
import me.zeroeightsix.kami.event.events.RenderEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.RayTraceResult;
import java.util.Iterator;
import java.util.List;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.EntityLivingBase;
import me.zeroeightsix.kami.util.EntityUtil;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import me.zeroeightsix.kami.util.Friends;
import java.util.Collection;
import java.util.ArrayList;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemTool;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemStack;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.init.MobEffects;
import java.util.Comparator;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.Packet;
import java.util.function.Predicate;
import net.minecraft.network.play.client.CPacketPlayer;
import me.zeroeightsix.kami.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Kami-Crystal", category = Category.CRYSTAL)
public class KamiCrystal extends Module
{
    private Setting<Boolean> autoSwitch;
    private Setting<Boolean> players;
    private Setting<Boolean> mobs;
    private Setting<Boolean> animals;
    private Setting<Boolean> place;
    private Setting<Boolean> explode;
    private Setting<Double> range;
    private Setting<Boolean> antiWeakness;
    private BlockPos render;
    private Entity renderEnt;
    private long systemTime;
    private static boolean togglePitch;
    private boolean switchCooldown;
    private boolean isAttacking;
    private int oldSlot;
    private int newSlot;
    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;
    @EventHandler
    private Listener<PacketEvent.Send> packetListener;
    
    public KamiCrystal() {
        this.autoSwitch = this.register(Settings.b("Auto Switch"));
        this.players = this.register(Settings.b("Players"));
        this.mobs = this.register(Settings.b("Mobs", false));
        this.animals = this.register(Settings.b("Animals", false));
        this.place = this.register(Settings.b("Place", false));
        this.explode = this.register(Settings.b("Explode", false));
        this.range = this.register(Settings.d("Range", 4.0));
        this.antiWeakness = this.register(Settings.b("Anti Weakness", false));
        this.systemTime = -1L;
        this.switchCooldown = false;
        this.isAttacking = false;
        this.oldSlot = -1;
        final Packet packet;
        this.packetListener = new Listener<PacketEvent.Send>(event -> {
            packet = event.getPacket();
            if (packet instanceof CPacketPlayer && KamiCrystal.isSpoofingAngles) {
                ((CPacketPlayer)packet).field_149476_e = (float)KamiCrystal.yaw;
                ((CPacketPlayer)packet).field_149473_f = (float)KamiCrystal.pitch;
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
    }
    
    @Override
    public void onUpdate() {
        final EntityEnderCrystal crystal = (EntityEnderCrystal)KamiCrystal.mc.field_71441_e.field_72996_f.stream().filter(entity -> entity instanceof EntityEnderCrystal).map(entity -> entity).min(Comparator.comparing(c -> KamiCrystal.mc.field_71439_g.func_70032_d(c))).orElse(null);
        if (this.explode.getValue() && crystal != null && KamiCrystal.mc.field_71439_g.func_70032_d((Entity)crystal) <= this.range.getValue()) {
            if (System.nanoTime() / 1000000L - this.systemTime >= 250L) {
                if (this.antiWeakness.getValue() && KamiCrystal.mc.field_71439_g.func_70644_a(MobEffects.field_76437_t)) {
                    if (!this.isAttacking) {
                        this.oldSlot = Wrapper.getPlayer().field_71071_by.field_70461_c;
                        this.isAttacking = true;
                    }
                    this.newSlot = -1;
                    for (int i = 0; i < 9; ++i) {
                        final ItemStack stack = Wrapper.getPlayer().field_71071_by.func_70301_a(i);
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
                        Wrapper.getPlayer().field_71071_by.field_70461_c = this.newSlot;
                        this.switchCooldown = true;
                    }
                }
                this.lookAtPacket(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, (EntityPlayer)KamiCrystal.mc.field_71439_g);
                KamiCrystal.mc.field_71442_b.func_78764_a((EntityPlayer)KamiCrystal.mc.field_71439_g, (Entity)crystal);
                KamiCrystal.mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
                this.systemTime = System.nanoTime() / 1000000L;
            }
            return;
        }
        resetRotation();
        if (this.oldSlot != -1) {
            Wrapper.getPlayer().field_71071_by.field_70461_c = this.oldSlot;
            this.oldSlot = -1;
        }
        this.isAttacking = false;
        int crystalSlot = (KamiCrystal.mc.field_71439_g.func_184614_ca().func_77973_b() == Items.field_185158_cP) ? KamiCrystal.mc.field_71439_g.field_71071_by.field_70461_c : -1;
        if (crystalSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (KamiCrystal.mc.field_71439_g.field_71071_by.func_70301_a(l).func_77973_b() == Items.field_185158_cP) {
                    crystalSlot = l;
                    break;
                }
            }
        }
        boolean offhand = false;
        if (KamiCrystal.mc.field_71439_g.func_184592_cb().func_77973_b() == Items.field_185158_cP) {
            offhand = true;
        }
        else if (crystalSlot == -1) {
            return;
        }
        final List<BlockPos> blocks = this.findCrystalBlocks();
        final List<Entity> entities = new ArrayList<Entity>();
        if (this.players.getValue()) {
            entities.addAll((Collection<? extends Entity>)KamiCrystal.mc.field_71441_e.field_73010_i.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.func_70005_c_())).collect(Collectors.toList()));
        }
        final boolean b2;
        entities.addAll((Collection<? extends Entity>)KamiCrystal.mc.field_71441_e.field_72996_f.stream().filter(entity -> {
            if (EntityUtil.isLiving(entity)) {
                if (EntityUtil.isPassive(entity) ? this.animals.getValue() : this.mobs.getValue()) {
                    return b2;
                }
            }
            return b2;
        }).collect(Collectors.toList()));
        BlockPos q = null;
        double damage = 0.5;
        for (final Entity entity2 : entities) {
            if (entity2 != KamiCrystal.mc.field_71439_g) {
                if (((EntityLivingBase)entity2).func_110143_aJ() <= 0.0f) {
                    continue;
                }
                for (final BlockPos blockPos : blocks) {
                    final double b = entity2.func_174818_b(blockPos);
                    if (b >= 169.0) {
                        continue;
                    }
                    final double d = calculateDamage(blockPos.field_177962_a + 0.5, blockPos.field_177960_b + 1, blockPos.field_177961_c + 0.5, entity2);
                    if (d <= damage) {
                        continue;
                    }
                    final double self = calculateDamage(blockPos.field_177962_a + 0.5, blockPos.field_177960_b + 1, blockPos.field_177961_c + 0.5, (Entity)KamiCrystal.mc.field_71439_g);
                    if (self > d && d >= ((EntityLivingBase)entity2).func_110143_aJ()) {
                        continue;
                    }
                    if (self - 0.5 > KamiCrystal.mc.field_71439_g.func_110143_aJ()) {
                        continue;
                    }
                    damage = d;
                    q = blockPos;
                    this.renderEnt = entity2;
                }
            }
        }
        if (damage == 0.5) {
            this.render = null;
            this.renderEnt = null;
            resetRotation();
            return;
        }
        this.render = q;
        if (this.place.getValue()) {
            if (!offhand && KamiCrystal.mc.field_71439_g.field_71071_by.field_70461_c != crystalSlot) {
                if (this.autoSwitch.getValue()) {
                    KamiCrystal.mc.field_71439_g.field_71071_by.field_70461_c = crystalSlot;
                    resetRotation();
                    this.switchCooldown = true;
                }
                return;
            }
            this.lookAtPacket(q.field_177962_a + 0.5, q.field_177960_b - 0.5, q.field_177961_c + 0.5, (EntityPlayer)KamiCrystal.mc.field_71439_g);
            final RayTraceResult result = KamiCrystal.mc.field_71441_e.func_72933_a(new Vec3d(KamiCrystal.mc.field_71439_g.field_70165_t, KamiCrystal.mc.field_71439_g.field_70163_u + KamiCrystal.mc.field_71439_g.func_70047_e(), KamiCrystal.mc.field_71439_g.field_70161_v), new Vec3d(q.field_177962_a + 0.5, q.field_177960_b - 0.5, q.field_177961_c + 0.5));
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
            KamiCrystal.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayerTryUseItemOnBlock(q, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
        }
        if (KamiCrystal.isSpoofingAngles) {
            if (KamiCrystal.togglePitch) {
                final EntityPlayerSP field_71439_g = KamiCrystal.mc.field_71439_g;
                field_71439_g.field_70125_A += (float)4.0E-4;
                KamiCrystal.togglePitch = false;
            }
            else {
                final EntityPlayerSP field_71439_g2 = KamiCrystal.mc.field_71439_g;
                field_71439_g2.field_70125_A -= (float)4.0E-4;
                KamiCrystal.togglePitch = true;
            }
        }
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        if (this.render != null) {
            KamiTessellator.prepare(7);
            KamiTessellator.drawBox(this.render, 1157627903, 63);
            KamiTessellator.release();
            if (this.renderEnt != null) {
                final Vec3d p = EntityUtil.getInterpolatedRenderPos(this.renderEnt, KamiCrystal.mc.func_184121_ak());
                Tracers.drawLineFromPosToPos(this.render.field_177962_a - KamiCrystal.mc.func_175598_ae().field_78725_b + 0.5, this.render.field_177960_b - KamiCrystal.mc.func_175598_ae().field_78726_c + 1.0, this.render.field_177961_c - KamiCrystal.mc.func_175598_ae().field_78723_d + 0.5, p.field_72450_a, p.field_72448_b, p.field_72449_c, this.renderEnt.func_70047_e(), 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }
    
    private void lookAtPacket(final double px, final double py, final double pz, final EntityPlayer me) {
        final double[] v = EntityUtil.calculateLookAt(px, py, pz, me);
        setYawAndPitch((float)v[0], (float)v[1]);
    }
    
    private boolean canPlaceCrystal(final BlockPos blockPos) {
        final BlockPos boost = blockPos.func_177982_a(0, 1, 0);
        final BlockPos boost2 = blockPos.func_177982_a(0, 2, 0);
        return (KamiCrystal.mc.field_71441_e.func_180495_p(blockPos).func_177230_c() == Blocks.field_150357_h || KamiCrystal.mc.field_71441_e.func_180495_p(blockPos).func_177230_c() == Blocks.field_150343_Z) && KamiCrystal.mc.field_71441_e.func_180495_p(boost).func_177230_c() == Blocks.field_150350_a && KamiCrystal.mc.field_71441_e.func_180495_p(boost2).func_177230_c() == Blocks.field_150350_a && KamiCrystal.mc.field_71441_e.func_72872_a((Class)Entity.class, new AxisAlignedBB(boost)).isEmpty() && KamiCrystal.mc.field_71441_e.func_72872_a((Class)Entity.class, new AxisAlignedBB(boost2)).isEmpty();
    }
    
    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(KamiCrystal.mc.field_71439_g.field_70165_t), Math.floor(KamiCrystal.mc.field_71439_g.field_70163_u), Math.floor(KamiCrystal.mc.field_71439_g.field_70161_v));
    }
    
    private List<BlockPos> findCrystalBlocks() {
        final NonNullList<BlockPos> positions = (NonNullList<BlockPos>)NonNullList.func_191196_a();
        positions.addAll((Collection)this.getSphere(getPlayerPos(), this.range.getValue().floatValue(), this.range.getValue().intValue(), false, true, 0).stream().filter((Predicate<? super Object>)this::canPlaceCrystal).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList()));
        return (List<BlockPos>)positions;
    }
    
    public List<BlockPos> getSphere(final BlockPos loc, final float r, final int h, final boolean hollow, final boolean sphere, final int plus_y) {
        final List<BlockPos> circleblocks = new ArrayList<BlockPos>();
        final int cx = loc.func_177958_n();
        final int cy = loc.func_177956_o();
        final int cz = loc.func_177952_p();
        for (int x = cx - (int)r; x <= cx + r; ++x) {
            for (int z = cz - (int)r; z <= cz + r; ++z) {
                for (int y = sphere ? (cy - (int)r) : cy; y < (sphere ? (cy + r) : ((float)(cy + h))); ++y) {
                    final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? ((cy - y) * (cy - y)) : 0);
                    if (dist < r * r && (!hollow || dist >= (r - 1.0f) * (r - 1.0f))) {
                        final BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }
    
    public static float calculateDamage(final double posX, final double posY, final double posZ, final Entity entity) {
        final float doubleExplosionSize = 12.0f;
        final double distancedsize = entity.func_70011_f(posX, posY, posZ) / doubleExplosionSize;
        final Vec3d vec3d = new Vec3d(posX, posY, posZ);
        final double blockDensity = entity.field_70170_p.func_72842_a(vec3d, entity.func_174813_aQ());
        final double v = (1.0 - distancedsize) * blockDensity;
        final float damage = (float)(int)((v * v + v) / 2.0 * 7.0 * doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase)entity, getDamageMultiplied(damage), new Explosion((World)KamiCrystal.mc.field_71441_e, (Entity)null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float)finald;
    }
    
    public static float getBlastReduction(final EntityLivingBase entity, float damage, final Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            final EntityPlayer ep = (EntityPlayer)entity;
            final DamageSource ds = DamageSource.func_94539_a(explosion);
            damage = CombatRules.func_189427_a(damage, (float)ep.func_70658_aO(), (float)ep.func_110148_a(SharedMonsterAttributes.field_189429_h).func_111126_e());
            final int k = EnchantmentHelper.func_77508_a(ep.func_184193_aE(), ds);
            final float f = MathHelper.func_76131_a((float)k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.func_70644_a(Potion.func_188412_a(11))) {
                damage -= damage / 4.0f;
            }
            damage = Math.max(damage - ep.func_110139_bj(), 0.0f);
            return damage;
        }
        damage = CombatRules.func_189427_a(damage, (float)entity.func_70658_aO(), (float)entity.func_110148_a(SharedMonsterAttributes.field_189429_h).func_111126_e());
        return damage;
    }
    
    private static float getDamageMultiplied(final float damage) {
        final int diff = KamiCrystal.mc.field_71441_e.func_175659_aa().func_151525_a();
        return damage * ((diff == 0) ? 0.0f : ((diff == 2) ? 1.0f : ((diff == 1) ? 0.5f : 1.5f)));
    }
    
    public static float calculateDamage(final EntityEnderCrystal crystal, final Entity entity) {
        return calculateDamage(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, entity);
    }
    
    private static void setYawAndPitch(final float yaw1, final float pitch1) {
        KamiCrystal.yaw = yaw1;
        KamiCrystal.pitch = pitch1;
        KamiCrystal.isSpoofingAngles = true;
    }
    
    private static void resetRotation() {
        if (KamiCrystal.isSpoofingAngles) {
            KamiCrystal.yaw = KamiCrystal.mc.field_71439_g.field_70177_z;
            KamiCrystal.pitch = KamiCrystal.mc.field_71439_g.field_70125_A;
            KamiCrystal.isSpoofingAngles = false;
        }
    }
    
    public void onDisable() {
        this.render = null;
        this.renderEnt = null;
        resetRotation();
    }
    
    static {
        KamiCrystal.togglePitch = false;
    }
}
