package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.render.Tracers;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.module.modules.gui.InfoOverlay.getItems;
import static me.zeroeightsix.kami.util.ColourConverter.rgbToInt;
import static me.zeroeightsix.kami.util.ColourConverter.toF;
import static me.zeroeightsix.kami.util.EntityUtil.calculateLookAt;

/**
 * Created by 086 on 28/12/2017.
 * Updated 3 December 2019 by hub
 * Updated 8 March 2020 by polymer
 * Updated by qther on 27/03/20
 * Updated by S-B99 on 27/03/20
 */
@Module.Info(name = "CrystalAura", category = Module.Category.COMBAT, description = "Places End Crystals to kill enemies")
public class CrystalAura extends Module {
    private Setting<Boolean> defaultSetting = register(Settings.b("Defaults", false));
    private Setting<Page> p = register(Settings.enumBuilder(Page.class).withName("Page").withValue(Page.ONE).build());
    /* Page One */
    private Setting<ExplodeBehavior> explodeBehavior = register(Settings.enumBuilder(ExplodeBehavior.class).withName("Explode Behavior").withValue(ExplodeBehavior.ALWAYS).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<PlaceBehavior> placeBehavior = register(Settings.enumBuilder(PlaceBehavior.class).withName("Place Behavior").withValue(PlaceBehavior.TRADITIONAL).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> autoSwitch = register(Settings.booleanBuilder("Auto Switch").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> place = register(Settings.booleanBuilder("Place").withValue(false).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> explode = register(Settings.booleanBuilder("Explode").withValue(false).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> checkAbsorption = register(Settings.booleanBuilder("Check Absorption").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    public  Setting<Double> range = register(Settings.doubleBuilder("Range").withMinimum(1.0).withValue(4.0).withMaximum(10.0).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Double> delay = register(Settings.doubleBuilder("Hit Delay").withMinimum(0.0).withValue(5.0).withMaximum(10.0).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Integer> hitAttempts = register(Settings.integerBuilder("Hit Attempts").withValue(-1).withMinimum(-1).withMaximum(20).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Double> minDmg = register(Settings.doubleBuilder("Minimum Damage").withMinimum(0.0).withValue(0.0).withMaximum(32.0).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> sneakEnable = register(Settings.booleanBuilder("Sneak Surround").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> placePriority = register(Settings.booleanBuilder("Prioritize Manual").withValue(false).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    /* Page Two */
    private Setting<Boolean> antiWeakness = register(Settings.booleanBuilder("Anti Weakness").withValue(false).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> noToolExplode = register(Settings.booleanBuilder("No Tool Explode").withValue(true).withVisibility(v -> !antiWeakness.getValue() && p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> players = register(Settings.booleanBuilder("Players").withValue(true).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> mobs = register(Settings.booleanBuilder("Mobs").withValue(false).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> animals = register(Settings.booleanBuilder("Animals").withValue(false).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> tracer = register(Settings.booleanBuilder("Tracer").withValue(true).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> customColours = register(Settings.booleanBuilder("Custom Colours").withValue(true).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Integer> aBlock = register(Settings.integerBuilder("Block Transparency").withMinimum(0).withValue(44).withMaximum(255).withVisibility(v -> p.getValue().equals(Page.TWO) && customColours.getValue()).build());
    private Setting<Integer> aTracer = register(Settings.integerBuilder("Tracer Transparency").withMinimum(0).withValue(200).withMaximum(255).withVisibility(v -> p.getValue().equals(Page.TWO) && customColours.getValue()).build());
    private Setting<Integer> r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).withVisibility(v -> p.getValue().equals(Page.TWO) && customColours.getValue()).build());
    private Setting<Integer> g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).withVisibility(v -> p.getValue().equals(Page.TWO) && customColours.getValue()).build());
    private Setting<Integer> b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).withVisibility(v -> p.getValue().equals(Page.TWO) && customColours.getValue()).build());
    private Setting<Boolean> statusMessages = register(Settings.booleanBuilder("Status Messages").withValue(false).withVisibility(v -> p.getValue().equals(Page.TWO)).build());

    private enum ExplodeBehavior { HOLE_ONLY, PREVENT_SUICIDE, LEFT_CLICK_ONLY, ALWAYS }
    private enum PlaceBehavior { MULTI, TRADITIONAL }
    private enum Page { ONE, TWO }

    private BlockPos render;
    private Entity renderEnt;
    private long systemTime = -1;
    private static boolean togglePitch = false;
    // we need this cooldown to not place from old hotbar slot, before we have switched to crystals
    private boolean switchCoolDown = false;
    private boolean isAttacking = false;
    private int oldSlot = -1;

    private static EntityEnderCrystal lastCrystal;
    private static List<EntityEnderCrystal> ignoredCrystals = new ArrayList<>();
    private static int hitTries = 0;

    public void onUpdate() {
        if (defaultSetting.getValue()) {
            explodeBehavior.setValue(ExplodeBehavior.ALWAYS);
            placeBehavior.setValue(PlaceBehavior.TRADITIONAL);
            autoSwitch.setValue(true);
            place.setValue(false);
            explode.setValue(false);
            checkAbsorption.setValue(true);
            range.setValue(4.0);
            delay.setValue(5.0);
            hitAttempts.setValue(-1);
            minDmg.setValue(0.0);
            sneakEnable.setValue(true);
            placePriority.setValue(false);

            antiWeakness.setValue(false);
            noToolExplode.setValue(true);
            players.setValue(true);
            mobs.setValue(false);
            animals.setValue(false);
            tracer.setValue(true);
            customColours.setValue(true);
            aBlock.setValue(44);
            aTracer.setValue(200);
            r.setValue(155);
            g.setValue(144);
            b.setValue(255);
            statusMessages.setValue(false);

            defaultSetting.setValue(false);
            Command.sendChatMessage(getChatName() + " Set to defaults!");
            Command.sendChatMessage(getChatName() + " Close and reopen the " + getName() + " settings menu to see changes");
        }

        if (mc.player == null) {
            resetHitAttempts();
            return;
        }

        resetHitAttempts(hitAttempts.getValue() == -1);

        int holeBlocks = 0;

        Vec3d[] holeOffset = {
                mc.player.getPositionVector().add(1, 0, 0),
                mc.player.getPositionVector().add(-1, 0, 0),
                mc.player.getPositionVector().add(0, 0, 1),
                mc.player.getPositionVector().add(0, 0, -1),
                mc.player.getPositionVector().add(0, -1, 0)
        };

        EntityEnderCrystal crystal = mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityEnderCrystal)
                .filter(entity -> !ignored(entity))
                .map(entity -> (EntityEnderCrystal) entity)
                .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                .orElse(null);



        if (explode.getValue() && crystal != null && mc.player.getDistance(crystal) <= range.getValue() && passSwordCheck()) {
            // Added delay to stop ncp from flagging "hitting too fast"
            if (((System.nanoTime() / 1000000f) - systemTime) >= 25*delay.getValue()) {
                if (antiWeakness.getValue() && mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                    if (!isAttacking) {
                        // save initial player hand
                        oldSlot = Wrapper.getPlayer().inventory.currentItem;
                        isAttacking = true;
                    }
                    // search for sword and tools in hotbar
                    int newSlot = -1;
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = Wrapper.getPlayer().inventory.getStackInSlot(i);
                        if (stack == ItemStack.EMPTY) {
                            continue;
                        }
                        if ((stack.getItem() instanceof ItemSword)) {
                            newSlot = i;
                            break;
                        }
                        if ((stack.getItem() instanceof ItemTool)) {
                            newSlot = i;
                            break;
                        }
                    }
                    // check if any swords or tools were found
                    if (newSlot != -1) {
                        Wrapper.getPlayer().inventory.currentItem = newSlot;
                        switchCoolDown = true;
                    }
                }
                if (placePriority.getValue()) {
                    boolean wasPlacing = place.getValue();
                    if (mc.gameSettings.keyBindUseItem.isKeyDown() && place.getValue()) {
                        place.setValue(false);
                        explode(crystal);
                    }
                    if (!place.getValue() && wasPlacing) place.setValue(true);
                }
                if (explodeBehavior.getValue() == ExplodeBehavior.ALWAYS) {
                    explode(crystal);
                }
                for (Vec3d vecOffset:holeOffset) { /* for placeholder offset for each BlockPos in the list holeOffset */
                    BlockPos offset = new BlockPos(vecOffset.x, vecOffset.y, vecOffset.z);
                    if (mc.world.getBlockState(offset).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(offset).getBlock() == Blocks.BEDROCK) {
                        holeBlocks++;
                    }
                }
                if (explodeBehavior.getValue() == ExplodeBehavior.HOLE_ONLY) {
                    if (holeBlocks == 5) {
                        explode(crystal);
                    }
                }
                if (explodeBehavior.getValue() == ExplodeBehavior.PREVENT_SUICIDE) {
                    if (mc.player.getPositionVector().distanceTo(crystal.getPositionVector()) <= 0.5 && mc.player.getPosition().getY() == crystal.getPosition().getY()|| mc.player.getPositionVector().distanceTo(crystal.getPositionVector()) >= 2.3 && mc.player.getPosition().getY() == crystal.getPosition().getY()||mc.player.getPositionVector().distanceTo(crystal.getPositionVector()) >= 0.5 && mc.player.getPosition().getY() != crystal.getPosition().getY()) {
                        explode(crystal);
                    }
                }
                if (explodeBehavior.getValue() == ExplodeBehavior.LEFT_CLICK_ONLY && mc.gameSettings.keyBindAttack.isKeyDown()) {
                    explode(crystal);
                }
                if (sneakEnable.getValue() && mc.player.isSneaking() && holeBlocks != 5) {
                    MODULE_MANAGER.getModule(Surround.class).enable();
                }
                return;
            }

        } else {
            resetRotation();
            if (oldSlot != -1) {
                Wrapper.getPlayer().inventory.currentItem = oldSlot;
                oldSlot = -1;
            }
            isAttacking = false;
        }

        int crystalSlot = mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL ? mc.player.inventory.currentItem : -1;
        if (crystalSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStackInSlot(l).getItem() == Items.END_CRYSTAL) {
                    crystalSlot = l;
                    break;
                }
            }
        }

        boolean offhand = false;
        if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            offhand = true;
        } else if (crystalSlot == -1) {
            return;
        }

        List<BlockPos> blocks = findCrystalBlocks();
        List<Entity> entities = new ArrayList<>();
        if (players.getValue()) {
            entities.addAll(mc.world.playerEntities.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        }
        entities.addAll(mc.world.loadedEntityList.stream().filter(entity -> EntityUtil.isLiving(entity) && (EntityUtil.isPassive(entity) ? animals.getValue() : mobs.getValue())).collect(Collectors.toList()));

        BlockPos q = null;
        double damage = .5;
        for (Entity entity : entities) {
            // stop if the entities health is 0 or less then 0 (somehow) or if the entity is you, don't do excess calculation if multiplace is enabled
            if (entity == mc.player || ((EntityLivingBase) entity).getHealth() <= 0 || placeBehavior.getValue() == PlaceBehavior.MULTI) {
                continue;
            }
            for (BlockPos blockPos : blocks) {
                double b = entity.getDistanceSq(blockPos);
                if (b >= 169) {
                    continue; // If this block if further than 13 (3.6^2, less calc) blocks, ignore it. It'll take no or very little damage
                }
                double d = calculateDamage(blockPos.x + .5, blockPos.y + 1, blockPos.z + .5, entity);
                if (d > damage) {
                    double self = calculateDamage(blockPos.x + .5, blockPos.y + 1, blockPos.z + .5, mc.player);
                    // Factor in absorption if wanted
                    float enemyAbsorption = 0.0f;
                    float playerAbsorption = 0.0f;
                    if (checkAbsorption.getValue()) {
                        enemyAbsorption = ((EntityLivingBase) entity).getAbsorptionAmount();
                        playerAbsorption = mc.player.getAbsorptionAmount();
                    }
                    // If this deals more damage to ourselves than it does to our target, continue. This is only ignored if the crystal is sure to kill our target but not us.
                    // Also continue if our crystal is going to hurt us.. alot
                    if ((self > d && !(d < ((EntityLivingBase) entity).getHealth() + enemyAbsorption)) || self - .5 > mc.player.getHealth() + playerAbsorption) {
                        continue;
                    }
                    damage = d;
                    q = blockPos;
                    renderEnt = entity;
                }
            }
        }

        if (place.getValue() && placeBehavior.getValue() == PlaceBehavior.MULTI) {
            for (Entity entity : entities) {

                if (entity == mc.player || ((EntityLivingBase) entity).getHealth() <= 0) {
                    continue;
                }
                for (BlockPos blockPos : blocks) {
                    double b = entity.getDistanceSq(blockPos);
                    if (b > 75) {
                        continue;
                    }
                    double d = calculateDamage(blockPos.x + .5, blockPos.y + 1, blockPos.z + .5, entity);
                    double self = calculateDamage(blockPos.x + .5, blockPos.y + 1, blockPos.z + .5, mc.player);
                    if (self >= mc.player.getHealth()+mc.player.getAbsorptionAmount() || self > d) continue;
                    if (b < 10 && d >= 15 || d >= ((EntityLivingBase) entity).getHealth() + ((EntityLivingBase) entity).getAbsorptionAmount() || 6 >= ((EntityLivingBase) entity).getHealth() + ((EntityLivingBase) entity).getAbsorptionAmount() && b < 4 || b < 9 && d >= minDmg.getValue() && minDmg.getValue() > 0.0) {
                        q = blockPos;
                        damage = d;
                        renderEnt = entity;
                    }
                }
            }
        }
        if (damage == .5) {
            render = null;
            renderEnt = null;
            resetRotation();
            return;
        }
        // this sends a constant packet flow for default packets
        if (place.getValue()) {
            render = q;
            if (!offhand && mc.player.inventory.currentItem != crystalSlot) {
                if (autoSwitch.getValue()) {
                    mc.player.inventory.currentItem = crystalSlot;
                    resetRotation();
                    switchCoolDown = true;
                }
                return;
            }
            lookAtPacket(q.x + .5, q.y - .5, q.z + .5, mc.player);
            RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(q.x + .5, q.y - .5d, q.z + .5));
            EnumFacing f;
            if (result == null || result.sideHit == null) {
                f = EnumFacing.UP;
            } else {
                f = result.sideHit;
            }
            // return after we did an autoswitch
            if (switchCoolDown) {
                switchCoolDown = false;
                return;
            }
            //mc.playerController.processRightClickBlock(mc.player, mc.world, q, f, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(q, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
        }
        if (isSpoofingAngles) {
            if (togglePitch) {
                mc.player.rotationPitch += 0.0004;
                togglePitch = false;
            } else {
                mc.player.rotationPitch -= 0.0004;
                togglePitch = true;
            }
        }
    }

    @Override
    public void onWorldRender(RenderEvent event) {
        if (render != null) {
            KamiTessellator.prepare(GL11.GL_QUADS);
            int colour = 0x44ffffff;
            if (customColours.getValue()) colour = rgbToInt(r.getValue(), g.getValue(), b.getValue(), aBlock.getValue());
            KamiTessellator.drawBox(render, colour, GeometryMasks.Quad.ALL);
            KamiTessellator.release();
            if (renderEnt != null && tracer.getValue()) {
                Vec3d p = EntityUtil.getInterpolatedRenderPos(renderEnt, mc.getRenderPartialTicks());
                float rL = 1;
                float gL = 1;
                float bL = 1;
                float aL = 1;
                if (customColours.getValue()) {
                    rL = toF(r.getValue());
                    gL = toF(g.getValue());
                    bL = toF(b.getValue());
                    aL = toF(aTracer.getValue());
                }
                Tracers.drawLineFromPosToPos(render.x - mc.getRenderManager().renderPosX + .5d, render.y - mc.getRenderManager().renderPosY + 1, render.z - mc.getRenderManager().renderPosZ + .5d, p.x, p.y, p.z, renderEnt.getEyeHeight(), rL, gL, bL, aL);
            }
        }
    }

    private void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
        double[] v = calculateLookAt(px, py, pz, me);
        setYawAndPitch((float) v[0], (float) v[1]+1f);
    }

    private boolean canPlaceCrystal(BlockPos blockPos) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);
        return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK
                || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)
                && mc.world.getBlockState(boost).getBlock() == Blocks.AIR
                && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR
                && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty()
                && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    private List<BlockPos> findCrystalBlocks() {
        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(getSphere(getPlayerPos(), range.getValue().floatValue(), range.getValue().intValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
        return positions;
    }

    public List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        List<BlockPos> circleblocks = new ArrayList<>();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();
        for (int x = cx - (int) r; x <= cx + r; x++) {
            for (int z = cz - (int) r; z <= cz + r; z++) {
                for (int y = (sphere ? cy - (int) r : cy); y < (sphere ? cy + r : cy + h); y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }

    public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 6.0F * 2.0F;
        double distancedSize = entity.getDistance(posX, posY, posZ) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        double v = (1.0D - distancedSize) * blockDensity;
        float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) doubleExplosionSize + 1.0D));
        double finalD = 1;
        /*if (entity instanceof EntityLivingBase)
            finalD = getBlastReduction((EntityLivingBase) entity,getDamageMultiplied(damage));*/
        if (entity instanceof EntityLivingBase) {
            finalD = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage), new Explosion(mc.world, null, posX, posY, posZ, 6F, false, true));
        }
        return (float) finalD;
    }

    public static float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

            int k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            float f = MathHelper.clamp(k, 0.0F, 20.0F);
            damage = damage * (1.0F - f / 25.0F);

            if (entity.isPotionActive(Objects.requireNonNull(Potion.getPotionById(11)))) {
                damage = damage - (damage / 4);
            }

            damage = Math.max(damage - ep.getAbsorptionAmount(), 0.0F);
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    private static float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0 : (diff == 2 ? 1 : (diff == 1 ? 0.5f : 1.5f)));
    }

    public static float calculateDamage(EntityEnderCrystal crystal, Entity entity) {
        return calculateDamage(crystal.posX, crystal.posY, crystal.posZ, entity);
    }


    //Better Rotation Spoofing System:

    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;

    //this modifies packets being sent so no extra ones are made. NCP used to flag with "too many packets"
    private static void setYawAndPitch(float yaw1, float pitch1) {
        yaw = yaw1;
        pitch = pitch1;
        isSpoofingAngles = true;
    }

    private static void resetRotation() {
        if (isSpoofingAngles) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
            isSpoofingAngles = false;
        }
    }


    @EventHandler
    private Listener<PacketEvent.Send> cPacketListener = new Listener<>(event -> {
        Packet packet = event.getPacket();
        if (packet instanceof CPacketPlayer) {
            if (isSpoofingAngles) {
                ((CPacketPlayer) packet).yaw = (float) yaw;
                ((CPacketPlayer) packet).pitch = (float) pitch;
            }
        }
    });

    public void onEnable() { sendMessage("&aENABLED&r"); }

    public void onDisable() {
        sendMessage("&cDISABLED&r");
        render = null;
        renderEnt = null;
        resetRotation();
    }

    public void explode(EntityEnderCrystal crystal) {
        if (crystal == null) return;

        if (lastCrystal == crystal) hitTries++;
        else {
            lastCrystal = crystal;
            hitTries = 0;
        }

        try {
            if (hitAttempts.getValue() != -1 && hitTries > hitAttempts.getValue()) {
                ignoredCrystals.add(crystal);
                hitTries = 0;
            } else {
                lookAtPacket(crystal.posX, crystal.posY, crystal.posZ, mc.player);
                mc.playerController.attackEntity(mc.player, crystal);
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        } catch (Throwable ignored) { }

        systemTime = System.nanoTime() / 1000000L;
    }

    private boolean passSwordCheck() {
        if (!noToolExplode.getValue() || antiWeakness.getValue()) return true;
        else return !noToolExplode.getValue() || (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemTool) && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword));
    }

    @Override
    public String getHudInfo() {
        return String.valueOf(getItems(Items.END_CRYSTAL));
    }

    private boolean ignored(Entity e) {
        if (ignoredCrystals == null) return false;
        for (EntityEnderCrystal c : ignoredCrystals) if (e != null && e == c) return true;
        return false;
    }

    private void resetHitAttempts() {
        sendMessage("&l&9Reset&r&9 hit attempts crystals");
        lastCrystal = null;
        ignoredCrystals = null;
        hitTries = 0;
    }

    private void resetHitAttempts(boolean should) {
        if (should) {
            lastCrystal = null;
            ignoredCrystals = null;
            hitTries = 0;
        } else if (resetTime()) {
            sendMessage("&l&9Reset&r&9 hit attempts crystals");
            lastCrystal = null;
            ignoredCrystals = null;
            hitTries = 0;
        }
    }

    private static long startTime = 0;
    private boolean resetTime() {
        if (startTime == 0) startTime = System.currentTimeMillis();
        if (startTime + 900000 <= System.currentTimeMillis()) { // 15 minutes in milliseconds
            startTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private void sendMessage(String message) {
        if (statusMessages.getValue()) {
            Command.sendChatMessage(getChatName() + message);
        }
    }
}
