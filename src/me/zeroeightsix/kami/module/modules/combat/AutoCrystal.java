// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.command.Command;
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
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.misc.Announcer;
import me.zeroeightsix.kami.module.modules.render.HUD;
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
import java.util.stream.Collector;
import java.util.stream.Collectors;
import me.zeroeightsix.kami.util.Friends;
import java.util.Collection;
import java.util.ArrayList;
import me.zeroeightsix.kami.util.InventoryUtils;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
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

@Info(name = "AutoCrystal", category = Category.COMBAT)
public class AutoCrystal extends Module
{
    private Setting<Boolean> place;
    private Setting<Boolean> raytrace;
    private Setting<Boolean> autoSwitch;
    private Setting<Boolean> offhandSwitch;
    private Setting<Boolean> antiStuck;
    private Setting<Boolean> multiPlace;
    private Setting<Boolean> alert;
    private Setting<Boolean> antiSui;
    private Setting<Integer> attackSpeed;
    private Setting<Integer> placeDelay;
    private Setting<Integer> enemyRange;
    private Setting<Integer> minDamage;
    private Setting<Integer> maxDamage;
    private Setting<Integer> facePlace;
    private Setting<Integer> multiPlaceSpeed;
    private Setting<Integer> placeRange;
    private Setting<Integer> breakRange;
    private BlockPos render;
    private Entity renderEnt;
    private long placeSystemTime;
    private long breakSystemTime;
    private long chatSystemTime;
    private long antiStuckSystemTime;
    private long multiPlaceSystemTime;
    private static boolean togglePitch;
    private boolean switchCooldown;
    private int newSlot;
    private int placements;
    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;
    @EventHandler
    private Listener<PacketEvent.Send> packetListener;
    
    public AutoCrystal() {
        this.place = this.register(Settings.b("Place", true));
        this.raytrace = this.register(Settings.b("RayTrace", false));
        this.autoSwitch = this.register(Settings.b("Auto Switch", true));
        this.offhandSwitch = this.register(Settings.b("Offhand Switch", false));
        this.antiStuck = this.register(Settings.b("Anti Stuck", true));
        this.multiPlace = this.register(Settings.b("Multi Place", false));
        this.alert = this.register(Settings.b("Chat Alerts", true));
        this.antiSui = this.register(Settings.b("Anti Suicide", true));
        this.attackSpeed = this.register((Setting<Integer>)Settings.integerBuilder("Attack Speed").withMinimum(0).withMaximum(20).withValue(17).build());
        this.placeDelay = this.register((Setting<Integer>)Settings.integerBuilder("Place Delay").withMinimum(0).withMaximum(50).withValue(0).build());
        this.enemyRange = this.register((Setting<Integer>)Settings.integerBuilder("Enemy Range").withMinimum(1).withMaximum(13).withValue(9).build());
        this.minDamage = this.register((Setting<Integer>)Settings.integerBuilder("Min Damage").withMinimum(0).withMaximum(16).withValue(4).build());
        this.maxDamage = this.register((Setting<Integer>)Settings.integerBuilder("Max Self Damage").withMinimum(0).withMaximum(20).withValue(11).build());
        this.facePlace = this.register((Setting<Integer>)Settings.integerBuilder("Min Health to Face Place").withMinimum(0).withMaximum(16).withValue(7).build());
        this.multiPlaceSpeed = this.register((Setting<Integer>)Settings.integerBuilder("Multi Place Speed").withMinimum(1).withMaximum(10).withValue(2).build());
        this.placeRange = this.register((Setting<Integer>)Settings.integerBuilder("Place Range").withMinimum(1).withMaximum(6).withValue(6).build());
        this.breakRange = this.register((Setting<Integer>)Settings.integerBuilder("Break Range").withMinimum(1).withMaximum(6).withValue(6).build());
        this.placeSystemTime = -1L;
        this.breakSystemTime = -1L;
        this.chatSystemTime = -1L;
        this.antiStuckSystemTime = -1L;
        this.multiPlaceSystemTime = -1L;
        this.switchCooldown = false;
        this.placements = 0;
        final Packet packet;
        this.packetListener = new Listener<PacketEvent.Send>(event -> {
            packet = event.getPacket();
            if (packet instanceof CPacketPlayer && AutoCrystal.isSpoofingAngles) {
                ((CPacketPlayer)packet).field_149476_e = (float)AutoCrystal.yaw;
                ((CPacketPlayer)packet).field_149473_f = (float)AutoCrystal.pitch;
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
    }
    
    @Override
    public void onUpdate() {
        final EntityEnderCrystal crystal = (EntityEnderCrystal)AutoCrystal.mc.field_71441_e.field_72996_f.stream().filter(entity -> entity instanceof EntityEnderCrystal).map(entity -> entity).min(Comparator.comparing(c -> AutoCrystal.mc.field_71439_g.func_70032_d(c))).orElse(null);
        if (crystal != null && AutoCrystal.mc.field_71439_g.func_70032_d((Entity)crystal) <= this.breakRange.getValue()) {
            if (System.nanoTime() / 1000000L - this.breakSystemTime >= 420 - this.attackSpeed.getValue() * 20) {
                this.lookAtPacket(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, (EntityPlayer)AutoCrystal.mc.field_71439_g);
                AutoCrystal.mc.field_71442_b.func_78764_a((EntityPlayer)AutoCrystal.mc.field_71439_g, (Entity)crystal);
                AutoCrystal.mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
                this.breakSystemTime = System.nanoTime() / 1000000L;
            }
            if (this.multiPlace.getValue()) {
                if (System.nanoTime() / 1000000L - this.multiPlaceSystemTime <= this.multiPlaceSpeed.getValue() * 50 && this.multiPlaceSpeed.getValue() < 10) {
                    if (!this.antiStuck.getValue()) {
                        this.placements = 0;
                        return;
                    }
                    if (System.nanoTime() / 1000000L - this.antiStuckSystemTime <= 300 + (400 - this.attackSpeed.getValue() * 20)) {
                        this.multiPlaceSystemTime = System.nanoTime() / 1000000L;
                        return;
                    }
                }
            }
            else {
                if (!this.antiStuck.getValue()) {
                    return;
                }
                if (System.nanoTime() / 1000000L - this.antiStuckSystemTime <= 300 + (400 - this.attackSpeed.getValue() * 20)) {
                    return;
                }
            }
        }
        else {
            resetRotation();
        }
        int crystalSlot = (AutoCrystal.mc.field_71439_g.func_184614_ca().func_77973_b() == Items.field_185158_cP) ? AutoCrystal.mc.field_71439_g.field_71071_by.field_70461_c : -1;
        if (crystalSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (AutoCrystal.mc.field_71439_g.field_71071_by.func_70301_a(l).func_77973_b() == Items.field_185158_cP) {
                    crystalSlot = l;
                    break;
                }
            }
        }
        if (this.offhandSwitch.getValue()) {
            InventoryUtils.OffhandCrystal();
        }
        boolean offhand = false;
        if (AutoCrystal.mc.field_71439_g.func_184592_cb().func_77973_b() == Items.field_185158_cP) {
            offhand = true;
        }
        else if (crystalSlot == -1) {
            return;
        }
        Entity ent = null;
        BlockPos finalPos = null;
        final List<BlockPos> blocks = this.findCrystalBlocks();
        final List<Entity> entities = new ArrayList<Entity>();
        entities.addAll((Collection<? extends Entity>)AutoCrystal.mc.field_71441_e.field_73010_i.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.func_70005_c_())).collect(Collectors.toList()));
        double damage = 0.5;
        double prevSelf = 0.5;
        for (final Entity entity2 : entities) {
            if (entity2 != AutoCrystal.mc.field_71439_g) {
                if (((EntityLivingBase)entity2).func_110143_aJ() <= 0.0f) {
                    continue;
                }
                if (AutoCrystal.mc.field_71439_g.func_70068_e(entity2) > this.enemyRange.getValue() * this.enemyRange.getValue()) {
                    continue;
                }
                for (final BlockPos blockPos : blocks) {
                    if (!canBlockBeSeen(blockPos) && AutoCrystal.mc.field_71439_g.func_174818_b(blockPos) > 25.0 && this.raytrace.getValue()) {
                        continue;
                    }
                    final double b = entity2.func_174818_b(blockPos);
                    if (b > 56.2) {
                        continue;
                    }
                    final double d = calculateDamage(blockPos.field_177962_a + 0.5, blockPos.field_177960_b + 1, blockPos.field_177961_c + 0.5, entity2);
                    if (d < this.minDamage.getValue() && ((EntityLivingBase)entity2).func_110143_aJ() + ((EntityLivingBase)entity2).func_110139_bj() > this.facePlace.getValue()) {
                        continue;
                    }
                    final double self = calculateDamage(blockPos.field_177962_a + 0.5, blockPos.field_177960_b + 1, blockPos.field_177961_c + 0.5, (Entity)AutoCrystal.mc.field_71439_g);
                    if (this.maxDamage.getValue() <= self) {
                        continue;
                    }
                    if (this.antiSui.getValue()) {
                        if (AutoCrystal.mc.field_71439_g.func_110143_aJ() + AutoCrystal.mc.field_71439_g.func_110139_bj() - self <= 7.0) {
                            continue;
                        }
                        if (self > d) {
                            continue;
                        }
                        if (self >= AutoCrystal.mc.field_71439_g.func_110143_aJ() + AutoCrystal.mc.field_71439_g.func_110139_bj()) {
                            continue;
                        }
                    }
                    if (d <= damage && (Math.round(d) != Math.round(damage) || self >= prevSelf)) {
                        continue;
                    }
                    damage = d;
                    finalPos = blockPos;
                    ent = entity2;
                    prevSelf = self;
                }
            }
        }
        if (damage == 0.5) {
            this.render = null;
            this.renderEnt = null;
            resetRotation();
            return;
        }
        this.render = finalPos;
        this.renderEnt = ent;
        if (this.place.getValue()) {
            if (!offhand && AutoCrystal.mc.field_71439_g.field_71071_by.field_70461_c != crystalSlot) {
                if (this.autoSwitch.getValue()) {
                    AutoCrystal.mc.field_71439_g.field_71071_by.field_70461_c = crystalSlot;
                    resetRotation();
                    this.switchCooldown = true;
                }
                return;
            }
            this.lookAtPacket(finalPos.field_177962_a + 0.5, finalPos.field_177960_b - 0.5, finalPos.field_177961_c + 0.5, (EntityPlayer)AutoCrystal.mc.field_71439_g);
            final RayTraceResult result = AutoCrystal.mc.field_71441_e.func_72933_a(new Vec3d(AutoCrystal.mc.field_71439_g.field_70165_t, AutoCrystal.mc.field_71439_g.field_70163_u + AutoCrystal.mc.field_71439_g.func_70047_e(), AutoCrystal.mc.field_71439_g.field_70161_v), new Vec3d(finalPos.field_177962_a + 0.5, finalPos.field_177960_b - 0.5, finalPos.field_177961_c + 0.5));
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
            if (System.nanoTime() / 1000000L - this.placeSystemTime >= this.placeDelay.getValue() * 2) {
                AutoCrystal.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayerTryUseItemOnBlock(finalPos, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                ++this.placements;
                this.antiStuckSystemTime = System.nanoTime() / 1000000L;
                this.placeSystemTime = System.nanoTime() / 1000000L;
            }
        }
        if (AutoCrystal.isSpoofingAngles) {
            if (AutoCrystal.togglePitch) {
                final EntityPlayerSP field_71439_g = AutoCrystal.mc.field_71439_g;
                field_71439_g.field_70125_A += (float)4.0E-4;
                AutoCrystal.togglePitch = false;
            }
            else {
                final EntityPlayerSP field_71439_g2 = AutoCrystal.mc.field_71439_g;
                field_71439_g2.field_70125_A -= (float)4.0E-4;
                AutoCrystal.togglePitch = true;
            }
        }
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        if (this.render != null) {
            KamiTessellator.prepare(7);
            KamiTessellator.drawBox(this.render, HUD.red(), HUD.green(), HUD.blue(), 83, 63);
            KamiTessellator.release();
            KamiTessellator.prepare(7);
            KamiTessellator.drawBoundingBoxBlockPos(this.render, 1.05f, HUD.red(), HUD.green(), HUD.blue(), 244);
            KamiTessellator.release();
        }
        if (this.renderEnt != null && Announcer.crystal() && ModuleManager.isModuleEnabled("Announcer") && System.nanoTime() / 1000000L - this.chatSystemTime >= Announcer.crystalDelay()) {
            Announcer.sendChatMessage("crystalled " + this.renderEnt.func_70005_c_());
            this.chatSystemTime = System.nanoTime() / 1000000L;
        }
    }
    
    private void lookAtPacket(final double px, final double py, final double pz, final EntityPlayer me) {
        final double[] v = EntityUtil.calculateLookAt(px, py, pz, me);
        setYawAndPitch((float)v[0], (float)v[1]);
    }
    
    private boolean canPlaceCrystal(final BlockPos blockPos) {
        final BlockPos boost = blockPos.func_177982_a(0, 1, 0);
        final BlockPos boost2 = blockPos.func_177982_a(0, 2, 0);
        return (AutoCrystal.mc.field_71441_e.func_180495_p(blockPos).func_177230_c() == Blocks.field_150357_h || AutoCrystal.mc.field_71441_e.func_180495_p(blockPos).func_177230_c() == Blocks.field_150343_Z) && AutoCrystal.mc.field_71441_e.func_180495_p(boost).func_177230_c() == Blocks.field_150350_a && AutoCrystal.mc.field_71441_e.func_180495_p(boost2).func_177230_c() == Blocks.field_150350_a && AutoCrystal.mc.field_71441_e.func_72872_a((Class)Entity.class, new AxisAlignedBB(boost)).isEmpty() && AutoCrystal.mc.field_71441_e.func_72872_a((Class)Entity.class, new AxisAlignedBB(boost2)).isEmpty();
    }
    
    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(AutoCrystal.mc.field_71439_g.field_70165_t), Math.floor(AutoCrystal.mc.field_71439_g.field_70163_u), Math.floor(AutoCrystal.mc.field_71439_g.field_70161_v));
    }
    
    private List<BlockPos> findCrystalBlocks() {
        final NonNullList<BlockPos> positions = (NonNullList<BlockPos>)NonNullList.func_191196_a();
        positions.addAll((Collection)this.getSphere(getPlayerPos(), this.placeRange.getValue(), this.placeRange.getValue(), false, true, 0).stream().filter((Predicate<? super Object>)this::canPlaceCrystal).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList()));
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
            finald = getBlastReduction((EntityLivingBase)entity, getDamageMultiplied(damage), new Explosion((World)AutoCrystal.mc.field_71441_e, (Entity)null, posX, posY, posZ, 6.0f, false, true));
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
            return damage;
        }
        damage = CombatRules.func_189427_a(damage, (float)entity.func_70658_aO(), (float)entity.func_110148_a(SharedMonsterAttributes.field_189429_h).func_111126_e());
        return damage;
    }
    
    private static float getDamageMultiplied(final float damage) {
        final int diff = AutoCrystal.mc.field_71441_e.func_175659_aa().func_151525_a();
        return damage * ((diff == 0) ? 0.0f : ((diff == 2) ? 1.0f : ((diff == 1) ? 0.5f : 1.5f)));
    }
    
    public static float calculateDamage(final EntityEnderCrystal crystal, final Entity entity) {
        return calculateDamage(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, entity);
    }
    
    public static boolean canBlockBeSeen(final BlockPos blockPos) {
        return AutoCrystal.mc.field_71441_e.func_147447_a(new Vec3d(AutoCrystal.mc.field_71439_g.field_70165_t, AutoCrystal.mc.field_71439_g.field_70163_u + AutoCrystal.mc.field_71439_g.func_70047_e(), AutoCrystal.mc.field_71439_g.field_70161_v), new Vec3d((double)blockPos.func_177958_n(), (double)blockPos.func_177956_o(), (double)blockPos.func_177952_p()), false, true, false) == null;
    }
    
    private static void setYawAndPitch(final float yaw1, final float pitch1) {
        AutoCrystal.yaw = yaw1;
        AutoCrystal.pitch = pitch1;
        AutoCrystal.isSpoofingAngles = true;
    }
    
    private static void resetRotation() {
        if (AutoCrystal.isSpoofingAngles) {
            AutoCrystal.yaw = AutoCrystal.mc.field_71439_g.field_70177_z;
            AutoCrystal.pitch = AutoCrystal.mc.field_71439_g.field_70125_A;
            AutoCrystal.isSpoofingAngles = false;
        }
    }
    
    @Override
    protected void onEnable() {
        if (this.alert.getValue() && AutoCrystal.mc.field_71441_e != null) {
            Command.sendRawChatMessage("§aAutoCrystal ON");
        }
    }
    
    public void onDisable() {
        if (this.alert.getValue() && AutoCrystal.mc.field_71441_e != null) {
            Command.sendRawChatMessage("§cAutoCrystal OFF");
        }
        if (this.offhandSwitch.getValue()) {
            InventoryUtils.OffhandCrystalReset();
        }
        this.render = null;
        resetRotation();
    }
    
    static {
        AutoCrystal.togglePitch = false;
    }
}
