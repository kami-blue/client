package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ESPHelper.drawESPBox
import me.zeroeightsix.kami.util.EntityUtils.EntityPriority
import me.zeroeightsix.kami.util.EntityUtils.calculateLookAt
import me.zeroeightsix.kami.util.EntityUtils.canEntityFeetBeSeen
import me.zeroeightsix.kami.util.EntityUtils.canEntityHitboxBeSeen
import me.zeroeightsix.kami.util.EntityUtils.getPrioritizedTarget
import me.zeroeightsix.kami.util.EntityUtils.getTargetList
import me.zeroeightsix.kami.util.GeometryMasks
import me.zeroeightsix.kami.util.KamiTessellator
import me.zeroeightsix.kami.util.MathsUtils.convertRange
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import java.lang.Math.random
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.*

/**
 * Created by Xiaro on 19/07/20
 */
@Module.Info(
        name = "CrystalAuraRewrite",
        description = "A reborn of the CrystalAura",
        category = Module.Category.COMBAT
)
// TODO: AutoSwap
// TODO: AutoOffhand
// TODO: Fix Aura delay and TPS Sync
// TODO: HoleBreaker

class CrystalAuraRewrite : Module() {
    /* Settings */
    private val page = register(Settings.e<Page>("Page", Page.TARGETING))

    /* Targeting */
    private val targetPriority = register(Settings.enumBuilder(EntityPriority::class.java).withName("TargetPriority").withValue(EntityPriority.HEALTH).withVisibility { page.value == Page.TARGETING }.build())
    private val players = register(Settings.booleanBuilder("Players").withValue(true).withVisibility { page.value == Page.TARGETING }.build())
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { page.value == Page.TARGETING && players.value }.build())
    private val sleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility { page.value == Page.TARGETING && players.value }.build())
    private val mobs = register(Settings.booleanBuilder("Mobs").withValue(false).withVisibility { page.value == Page.TARGETING }.build())
    private val passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility { page.value == Page.TARGETING && mobs.value }.build())
    private val neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(false).withVisibility { page.value == Page.TARGETING && mobs.value }.build())
    private val hostile = register(Settings.booleanBuilder("HostileMobs").withValue(true).withVisibility { page.value == Page.TARGETING && mobs.value }.build())
    private val targetingRange = register(Settings.floatBuilder("TargetingRange").withValue(12.0f).withRange(0.0f, 20.0f).withVisibility { page.value == Page.TARGETING }.build())

    /* Place */
    private val place = register(Settings.booleanBuilder("Place").withValue(true).withVisibility { page.value == Page.PLACE }.build())
    private val fastCalc = register(Settings.booleanBuilder("FastCalculation").withValue(false).withVisibility { page.value == Page.PLACE }.build())
    private val feetOnly = register(Settings.booleanBuilder("FeetOnly").withValue(false).withVisibility { page.value == Page.PLACE && !fastCalc.value }.build())
    private val minDamageP = register(Settings.integerBuilder("MinDamagePlace").withValue(16).withRange(0, 40).withVisibility { page.value == Page.PLACE && !fastCalc.value }.build())
    private val efficiencyP = register(Settings.integerBuilder("MinEfficiencyPlace").withValue(4).withRange(-20, 20).withVisibility { page.value == Page.PLACE && !fastCalc.value }.build())
    private val maxCrystal = register(Settings.integerBuilder("MaxCrystal").withValue(1).withRange(1, 8).withVisibility { page.value == Page.PLACE }.build())
    private val placeSpeed = register(Settings.integerBuilder("Places/s").withValue(20).withRange(1, 30).withVisibility { page.value == Page.PLACE }.build())
    private val placeRange = register(Settings.floatBuilder("PlaceRange").withValue(6.0f).withRange(0.0f, 10.0f).withVisibility { page.value == Page.PLACE }.build())

    /* Explode */
    private val explode = register(Settings.booleanBuilder("Explode").withValue(true).withVisibility { page.value == Page.EXPLODE }.build())
    private val checkImmune = register(Settings.booleanBuilder("CheckImmune").withValue(false).withVisibility { page.value == Page.EXPLODE }.build())
    private val checkDamage = register(Settings.booleanBuilder("CheckDamage").withValue(false).withVisibility { page.value == Page.EXPLODE }.build())
    private val autoForceExplode = register(Settings.booleanBuilder("AutoForceExplode").withValue(true).withVisibility { page.value == Page.EXPLODE && checkDamage.value }.build())
    private val minDamageE = register(Settings.integerBuilder("MinDamageExplode").withValue(60).withRange(0, 80).withVisibility { page.value == Page.EXPLODE && checkDamage.value }.build())
    private val efficiencyE = register(Settings.integerBuilder("MinEfficiencyExplode").withValue(8).withRange(-20, 20).withVisibility { page.value == Page.EXPLODE && checkDamage.value }.build())
    private val hitSpeed = register(Settings.integerBuilder("Hits/s").withValue(20).withRange(1, 30).withVisibility { page.value == Page.EXPLODE }.build())
    private val explodeRange = register(Settings.floatBuilder("ExplodeRange").withValue(4.0f).withRange(0.0f, 10.0f).withVisibility { page.value == Page.EXPLODE }.build())
    private val wallExplodeRange = register(Settings.floatBuilder("WallExplodeRange").withValue(3.0f).withRange(0.0f, 10.0f).withVisibility { page.value == Page.EXPLODE }.build())

    /* Render settings */
    private val damageESP = register(Settings.booleanBuilder("DamageESP").withValue(true).withVisibility { page.value == Page.RENDER }.build())
    private val minAlpha = register(Settings.integerBuilder("MinAlpha").withValue(15).withRange(0, 255).withVisibility { page.value == Page.RENDER }.build())
    private val maxAlpha = register(Settings.integerBuilder("MaxAlpha").withValue(63).withRange(0, 255).withVisibility { page.value == Page.RENDER }.build())
    private val crystalESP = register(Settings.booleanBuilder("CrystalESP").withValue(true).withVisibility { page.value == Page.RENDER }.build())
    private val filled = register(Settings.booleanBuilder("Filled").withValue(true).withVisibility { page.value == Page.RENDER && crystalESP.value }.build())
    private val outline = register(Settings.booleanBuilder("Outline").withValue(true).withVisibility { page.value == Page.RENDER && crystalESP.value }.build())
    private val tracer = register(Settings.booleanBuilder("Tracer").withValue(true).withVisibility { page.value == Page.RENDER && crystalESP.value }.build())
    private val animationScale = register(Settings.floatBuilder("AnimationScale").withValue(1.0f).withRange(0.0f, 5.0f).withVisibility { page.value == Page.RENDER && crystalESP.value })
    private val r = register(Settings.integerBuilder("Red").withValue(155).withRange(0, 255).withVisibility { page.value == Page.RENDER && crystalESP.value }.build())
    private val g = register(Settings.integerBuilder("Green").withValue(144).withRange(0, 255).withVisibility { page.value == Page.RENDER && crystalESP.value }.build())
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDER && crystalESP.value }.build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(47).withRange(0, 255).withVisibility { page.value == Page.RENDER && crystalESP.value && filled.value }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(127).withRange(0, 255).withVisibility { page.value == Page.RENDER && crystalESP.value && outline.value }.build())
    private val aTracer = register(Settings.integerBuilder("TracerAlpha").withValue(200).withRange(0, 255).withVisibility { page.value == Page.RENDER && crystalESP.value && tracer.value }.build())
    private val thickness = register(Settings.floatBuilder("Thickness").withValue(4.0f).withRange(0.0f, 8.0f).withVisibility { page.value == Page.RENDER && crystalESP.value }.build())
    private val espRange = register(Settings.floatBuilder("ESPRange").withValue(16.0f).withRange(0.0f, 32.0f).withVisibility { page.value == Page.RENDER }.build())
    /* End of settings */

    /* Threads */
    private val threads = Array(4) { Thread() }
    private val runnable = arrayOf(Runnable { updateTarget() }, Runnable { updateMap() }, Runnable { place() }, Runnable { explode() })

    /* Variables */
    private var currentTarget: Entity? = null
    private val damageESPMap = ConcurrentHashMap<Float, BlockPos>()
    private var mapClearCount = 0
    private val damagePosMap = ConcurrentSkipListMap<Float, BlockPos>(Comparator.reverseOrder())
    private val damageCrystalMap = ConcurrentSkipListMap<Float, Entity>(Comparator.reverseOrder())
    private val crystalList = ConcurrentHashMap<EntityEnderCrystal, Float>()
    private var forceExplode = false


    private enum class Page {
        TARGETING, PLACE, EXPLODE, RENDER
    }

    override fun getHudInfo(): String {
        return currentTarget?.name ?: ""
    }

    override fun onEnable() {
        if (mc.player == null) {
            this.disable()
            return
        }
        for (i in 0..3) {
            if (threads[i].state == Thread.State.NEW || threads[i].state == Thread.State.TERMINATED) {
                threads[i] = Thread(runnable[i])
                threads[i].start()
            }
        }
    }

    override fun onDisable() {
        currentTarget = null
        damagePosMap.clear()
        crystalList.clear()
    }

    override fun onUpdate() {
        if (spoofing) {
            mc.player.rotationYaw += random().toFloat() * 0.005f - 0.0025f
            mc.player.rotationPitch += random().toFloat() * 0.005f - 0.0025f
        }
    }

    override fun onWorldRender(event: RenderEvent?) {
        /* Damage ESP */
        if (damageESP.value && currentTarget != null) {
            KamiTessellator.prepare(GL11.GL_QUADS)
            for ((damage, pos) in damageESPMap) {
                val rgb = convertRange(damage.toInt(), minDamageP.value, 60, 127, 255)
                val a = convertRange(damage.toInt(), minDamageP.value, 60, minAlpha.value, maxAlpha.value)
                KamiTessellator.drawBox(pos, rgb, rgb, rgb, a, GeometryMasks.Quad.ALL)
            }
            KamiTessellator.release()
        }

        /* Crystal ESP */
        if (crystalESP.value) {
            for ((crystal, alpha) in crystalList) {
                val sine = sin(alpha * 0.5 * PI).toFloat()
                val box = crystal.boundingBox.shrink(1.0 - sine)
                drawESPBox(box, filled.value, outline.value, tracer.value, r.value, g.value, b.value, (aFilled.value * sine).toInt(), (aOutline.value * sine).toInt(), (aTracer.value * sine).toInt(), thickness.value)
            }
        }
    }

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (mc.player == null || !spoofing || event.packet !is SPacketSoundEffect) return@EventHook
        val packet = event.packet as SPacketSoundEffect
        if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
            val crystalList = getCrystalList()
            for (entry in crystalList) {
                if (entry.key.getDistance(packet.x, packet.y, packet.z) > 5) continue
                entry.key.setDead()
            }
        }
    })

    /* Threads */
    private fun updateTarget() {
        while (isEnabled) {
            val player = arrayOf(players.value, friends.value, sleeping.value)
            val mob = arrayOf(mobs.value, passive.value, neutral.value, hostile.value)
            val targetList = getTargetList(player, mob, true, false, targetingRange.value)
            if (targetList.isEmpty()) {
                currentTarget = null
                Thread.sleep(25L) /* Just to find target faster if there is none */
            } else {
                currentTarget = getPrioritizedTarget(targetList, targetPriority.value as EntityPriority)
                Thread.sleep(1000L) /* No need to update target frequently if there is one already */
            }
        }
    }

    private fun updateMap() {
        while (isEnabled) {
            val cacheMap = TreeMap<Float, BlockPos>(Comparator.reverseOrder())
            cacheMap.putAll(getPlacePos(currentTarget, targetingRange.value.toDouble(), fastCalc.value, feetOnly.value))

            if (damageESP.value) {
                mapClearCount = when {
                    cacheMap.isNotEmpty() -> {
                        damageESPMap.clear()
                        damageESPMap.putAll(cacheMap)
                        0
                    }
                    mapClearCount <= 5 -> {  /* To solve blinking issue */
                        mapClearCount + 1
                    }
                    else -> {
                        0
                    }
                }
            }

            if (crystalESP.value) {
                val cacheList = HashMap(getCrystalList())
                for ((crystal, alpha) in crystalList) {
                    if (alpha >= 2.0f) {
                        crystalList.remove(crystal)
                    } else {
                        val scale = 1f / animationScale.value
                        cacheList.computeIfPresent(crystal) { _, _ -> min(alpha + 0.1f * scale, 1f) }
                        cacheList.computeIfAbsent(crystal) { min(alpha + 0.05f * scale, 2f) }
                    }
                }
                crystalList.putAll(cacheList)
            }

            /* For placing */
            damagePosMap.clear()
            if (cacheMap.isNotEmpty()) {
                while (cacheMap.size > maxCrystal.value * 2) {
                    cacheMap.pollLastEntry()
                }
                for ((damage, blockPos) in cacheMap) {
                    if (!canPlaceCollide(blockPos)) continue
                    if (!fastCalc.value) {
                        if (damage < minDamageP.value) continue
                        val selfDamage = calcDamage(blockPos, mc.player, false)
                        if (damage - selfDamage < efficiencyP.value) continue
                    }
                    damagePosMap[damage] = blockPos
                }
                while (damagePosMap.size > maxCrystal.value) {
                    damagePosMap.pollLastEntry()
                }
            }

            /* For exploding */
            forceExplode = (autoForceExplode.value && damagePosMap.isNotEmpty()
                    && damageCrystalMap.isEmpty() && damagePosMap.firstKey() > minDamageE.value)
                    || (mc.gameSettings.keyBindAttack.isKeyDown && mc.player.heldItemMainhand.getItem() != Items.DIAMOND_PICKAXE
                    && mc.player.heldItemMainhand.getItem() != Items.GOLDEN_APPLE)
            damageCrystalMap.clear()
            damageCrystalMap.putAll(getDamageCrystalMap(forceExplode))

            Thread.sleep(15L)
        }
    }

    private fun place() {
        while (isEnabled) {

            when {
                !place.value -> {
                    Thread.sleep(1000L)
                }

                (mc.player.heldItemMainhand.getItem() != Items.END_CRYSTAL
                        && mc.player.heldItemOffhand.getItem() != Items.END_CRYSTAL)
                        || damagePosMap.isEmpty() -> {
                    Thread.sleep(25L)
                }

                else -> {
                    for (blockPos in damagePosMap.values) {
                        if (mc.player.getDistanceSq(blockPos) > placeRange.value * placeRange.value) continue
                        if (!canPlaceCollide(blockPos)) continue
                        val side = getHitSide(blockPos)
                        val hand = if (mc.player.heldItemMainhand.getItem() == Items.END_CRYSTAL) EnumHand.MAIN_HAND else EnumHand.OFF_HAND
                        lookAtPacket(blockPos.x + 0.5, blockPos.y - 0.5, blockPos.z + 0.5)
                        mc.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(blockPos, side, hand, 0F, 0F, 0F))
                        Thread.sleep(1000L / placeSpeed.value)
                        break
                    }
                }
            }
        }
    }

    private fun explode() {
        while (isEnabled) when {
            !explode.value -> {
                Thread.sleep(500L)
            }

            currentTarget == null || (currentTarget!!.isInvulnerable && checkImmune.value) || damageCrystalMap.isEmpty() -> {
                Thread.sleep(25L)
            }

            else -> {
                for (crystal in damageCrystalMap.values) {
                    var lookVec = Vec3d(crystal.posX, crystal.posY, crystal.posZ)
                    val dist = mc.player.getDistance(crystal)
                    if (!forceExplode) {
                        if (dist > explodeRange.value) continue
                        if (!mc.player.canEntityBeSeen(crystal) && !canEntityFeetBeSeen(crystal)) {
                            lookVec = canEntityHitboxBeSeen(crystal) ?: continue
                            if (dist > wallExplodeRange.value) continue
                        }
                    }
                    lookAtPacket(lookVec)
                    mc.playerController.attackEntity(mc.player, crystal)
                    mc.player.swingArm(EnumHand.MAIN_HAND)
                    Thread.sleep(1000L / hitSpeed.value)
                }
            }
        }
    }
    /* End of threads */

    /* Rotation Spoof */
    private var yaw = 0f
    private var pitch = 0f
    private var spoofing = false

    private fun lookAtPacket(vec3d: Vec3d) {
        lookAtPacket(vec3d.x, vec3d.y, vec3d.z)
    }

    private fun lookAtPacket(px: Double, py: Double, pz: Double) {
        val rotation = calculateLookAt(px, py, pz, mc.player)
        yaw = rotation[0].toFloat()
        pitch = rotation[1].toFloat()
        spoofing = true
    }

    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (mc.player == null || !spoofing || event.packet !is CPacketPlayer) return@EventHook
        val packet = event.packet as CPacketPlayer
        packet.yaw = yaw
        packet.pitch = pitch
        spoofing = false
    })
    /* End of Rotation Spoof */

    /* Position finding */
    private fun getDamageCrystalMap(forceExplode: Boolean): Map<Float, Entity> {
        if (currentTarget == null) return emptyMap()
        val damageCrystalMap = HashMap<Float, Entity>()
        val crystalList = getCrystalList()
        for (entityCrystal in crystalList) {
            val damage = calcDamage(entityCrystal.key, currentTarget!!, !checkDamage.value)
            if (checkDamage.value && !forceExplode && damage < minDamageE.value) continue
            val selfDamage = calcDamage(entityCrystal.key, mc.player, false)
            if (checkDamage.value && !forceExplode && damage - selfDamage < efficiencyE.value) continue
            damageCrystalMap[damage] = entityCrystal.key
        }
        return damageCrystalMap
    }

    private fun getCrystalList(): Map<EntityEnderCrystal, Float> {
        val crystalList = HashMap<EntityEnderCrystal, Float>()
        val entityList = ArrayList<Entity>()
        try {
            entityList.addAll(mc.world.loadedEntityList)
            for (entity in entityList) {
                if (entity !is EntityEnderCrystal) continue
                if (entity.isDead) continue
                if (mc.player.getDistance(entity) > espRange.value) continue
                crystalList[entity] = 0.5f
            }
        } catch (ignored: ConcurrentModificationException) {
        }
        return crystalList
    }

    private fun getHitSide(blockPos: BlockPos): EnumFacing {
        val result = mc.world.rayTraceBlocks(Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
                Vec3d(blockPos.x + 0.5, blockPos.y - .5, blockPos.z + 0.5))
        return if (result?.sideHit == null) {
            EnumFacing.UP
        } else {
            result.sideHit
        }
    }

    private fun getPlacePos(target: Entity?, radius: Double, fastCalc: Boolean, feetLevel: Boolean): Map<Float, BlockPos> {
        if (target == null) return emptyMap()
        val feetPosY = target.posY.toInt() - 1
        val yRange = if (!fastCalc && !feetLevel) getIntRange(target.posY, radius) else IntRange(feetPosY, feetPosY)
        val damagePosMap = HashMap<Float, BlockPos>()
        for (x in getIntRange(target.posX, radius)) for (y in yRange) for (z in getIntRange(target.posZ, radius)) {
            /* Valid position check */
            val blockPos = BlockPos(x, y, z)
            if (target.getDistanceSq(blockPos) > radius * radius) continue
            if (!canPlace(blockPos)) continue

            /* Damage calculation */
            val damage = calcDamage(blockPos, target, fastCalc)
            damagePosMap[damage] = blockPos
        }
        return damagePosMap
    }

    /* Checks blocks and target colliding only */
    private fun canPlace(blockPos: BlockPos): Boolean {
        val pos1 = blockPos.up()
        val pos2 = pos1.up()
        val bBox = currentTarget?.boundingBox ?: return false
        val xArray = arrayOf(floor(bBox.minX).toInt(), floor(bBox.maxX).toInt())
        val yArray = arrayOf(floor(bBox.minY).toInt(), floor(bBox.maxY).toInt())
        val zArray = arrayOf(floor(bBox.minZ).toInt(), floor(bBox.maxZ).toInt())
        for (x in xArray) for (y in yArray) for (z in zArray) {
            if (pos1 == BlockPos(x, y, z)
                    || pos2 == BlockPos(x, y, z)) return false
        }
        return (mc.world.getBlockState(blockPos).block == Blocks.BEDROCK || mc.world.getBlockState(blockPos).block == Blocks.OBSIDIAN)
                && mc.world.isAirBlock(pos1) && mc.world.isAirBlock(pos2)
    }

    /* Checks crystal colliding */
    private fun canPlaceCollide(blockPos: BlockPos): Boolean {
        val pos = blockPos.up()
        return try {
            mc.world.checkNoEntityCollision(AxisAlignedBB(pos))
        } catch (ignored: ConcurrentModificationException) {
            false
        }
    }

    private fun getIntRange(d1: Double, d2: Double): IntRange {
        return IntRange(floor(d1 - d2).toInt(), ceil(d1 + d2).toInt())
    }
    /* End of position finding */

    /* Damage calculation */
    private fun calcDamage(blockPos: BlockPos, entity: Entity, fastCalc: Boolean): Float {
        val posX = blockPos.x + 0.5
        val posY = blockPos.y + 1.0
        val posZ = blockPos.z + 0.5
        val vec3d = Vec3d(posX, posY, posZ)
        return calcDamage(vec3d, entity, fastCalc)
    }

    private fun calcDamage(crystal: EntityEnderCrystal, entity: Entity, fastCalc: Boolean): Float {
        val vec3d = crystal.positionVector
        return calcDamage(vec3d, entity, fastCalc)
    }

    private fun calcDamage(pos: Vec3d, entity: Entity, fastCalc: Boolean): Float {
        val distance = entity.getDistance(pos.x, pos.y, pos.z)
        return if (!fastCalc) {
            val v = (1.0 - (distance / 12.0)) * entity.world.getBlockDensity(pos, entity.boundingBox)
            ((v * v + v) / 2.0 * 84.0 + 1.0).toFloat()
        } else {
            1f / distance.toFloat() /* Use the reciprocal number so it can be sorted correctly */
        }
    }
}