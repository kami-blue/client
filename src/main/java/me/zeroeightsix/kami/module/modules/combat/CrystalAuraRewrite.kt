package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.CombatUtils.CrystalUtils
import me.zeroeightsix.kami.util.EntityUtils.EntityPriority
import me.zeroeightsix.kami.util.EntityUtils.canEntityFeetBeSeen
import me.zeroeightsix.kami.util.EntityUtils.canEntityHitboxBeSeen
import me.zeroeightsix.kami.util.math.RotationUtils
import net.minecraft.entity.Entity
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.lang.Math.random
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.collections.HashMap

/**
 * Created by Xiaro on 19/07/20
 */
@Module.Info(
        name = "CrystalAuraRewrite",
        description = "A reborn of the CrystalAura",
        category = Module.Category.COMBAT,
        modulePriority = 80
)
// TODO: AutoSwap
// TODO: AutoOffhand
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
    /* End of settings */

    /* Threads */
    private val threads = Array(4) { Thread() }
    private val runnable = arrayOf(Runnable { updateMap() }, Runnable { place() }, Runnable { explode() })

    /* Variables */
    private val placeMap = ConcurrentSkipListMap<Float, BlockPos>(Comparator.reverseOrder())
    private val explodeMap = ConcurrentSkipListMap<Float, Entity>(Comparator.reverseOrder())
    private var forceExplode = false
    private var placing = false
    private var exploding = false

    private enum class Page {
        TARGETING, PLACE, EXPLODE
    }

    override fun onEnable() {
        if (mc.player == null) {
            this.disable()
            return
        }
        for (i in 0..2) {
            if (threads[i].state == Thread.State.NEW || threads[i].state == Thread.State.TERMINATED) {
                threads[i] = Thread(runnable[i])
                threads[i].start()
            }
        }
    }

    override fun onDisable() {
        placeMap.clear()
        explodeMap.clear()
        forceExplode = false
        placing = false
        exploding = false
    }

    override fun onUpdate() {
        if (spoofing) {
            mc.player.rotationYaw += random().toFloat() * 0.005f - 0.0025f
            mc.player.rotationPitch += random().toFloat() * 0.005f - 0.0025f
        }
    }

    override fun isActive(): Boolean {
        return placing || exploding
    }

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (mc.player == null || !spoofing || event.packet !is SPacketSoundEffect) return@EventHook
        val packet = event.packet as SPacketSoundEffect
        if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
            val crystalList = CrystalUtils.getCrystalList(placeRange.value)
            for (entry in crystalList) {
                if (entry.key.getDistance(packet.x, packet.y, packet.z) > 5) continue
                entry.key.setDead()
            }
        }
    })

    /* Threads */
    private fun updateMap() {
        while (isEnabled) {
            val cacheMap = TreeMap<Float, BlockPos>(Comparator.reverseOrder())
            cacheMap.putAll(CrystalUtils.getPlacePos(CombatManager.currentTarget, targetingRange.value.toDouble(), fastCalc.value, feetOnly.value))

            /* For placing */
            placeMap.clear()
            if (cacheMap.isNotEmpty()) {
                while (cacheMap.size > maxCrystal.value * 2) {
                    cacheMap.pollLastEntry()
                }
                for ((damage, blockPos) in cacheMap) {
                    if (!CrystalUtils.canPlaceCollide(blockPos)) continue
                    if (!fastCalc.value) {
                        if (damage < minDamageP.value) continue
                        val selfDamage = CrystalUtils.calcExplosionDamage(blockPos, mc.player, false)
                        if (damage - selfDamage < efficiencyP.value) continue
                    }
                    placeMap[damage] = blockPos
                }
                while (placeMap.size > maxCrystal.value) {
                    placeMap.pollLastEntry()
                }
            }

            /* For exploding */
            forceExplode = (autoForceExplode.value && placeMap.isNotEmpty()
                    && explodeMap.isEmpty() && placeMap.firstKey() > minDamageE.value)
                    || (mc.gameSettings.keyBindAttack.isKeyDown && mc.player.heldItemMainhand.getItem() != Items.DIAMOND_PICKAXE
                    && mc.player.heldItemMainhand.getItem() != Items.GOLDEN_APPLE)
            explodeMap.clear()
            explodeMap.putAll(getPlaceMap(forceExplode))

            Thread.sleep(15L)
        }
    }

    private fun place() {
        while (isEnabled) {
            when {
                !place.value -> {
                    placing = false
                    Thread.sleep(1000L)
                }

                (mc.player.heldItemMainhand.getItem() != Items.END_CRYSTAL
                        && mc.player.heldItemOffhand.getItem() != Items.END_CRYSTAL)
                        || placeMap.isEmpty() -> {
                    placing = false
                    Thread.sleep(25L)
                }

                else -> {
                    placing = true
                    for (blockPos in placeMap.values) {
                        if (mc.player.getDistanceSq(blockPos) > placeRange.value * placeRange.value) continue
                        if (!CrystalUtils.canPlaceCollide(blockPos)) continue
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
                exploding = false
                Thread.sleep(500L)
            }

            CombatManager.currentTarget == null || (CombatManager.currentTarget!!.isInvulnerable && checkImmune.value) || explodeMap.isEmpty() -> {
                exploding = false
                Thread.sleep(25L)
            }

            else -> {
                exploding = true
                for (crystal in explodeMap.values) {
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
        val rotation = RotationUtils.getRotationTo(Vec3d(px, py, pz), true)
        yaw = rotation.first.toFloat()
        pitch = rotation.second.toFloat()
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
    private fun getPlaceMap(forceExplode: Boolean): Map<Float, Entity> {
        if (CombatManager.currentTarget == null) return emptyMap()
        val damageCrystalMap = HashMap<Float, Entity>()
        val crystalList = CrystalUtils.getCrystalList(explodeRange.value)
        for (entityCrystal in crystalList) {
            val damage = CrystalUtils.calcExplosionDamage(entityCrystal.key, CombatManager.currentTarget!!, !checkDamage.value)
            if (checkDamage.value && !forceExplode && damage < minDamageE.value) continue
            val selfDamage = CrystalUtils.calcExplosionDamage(entityCrystal.key, mc.player, false)
            if (checkDamage.value && !forceExplode && damage - selfDamage < efficiencyE.value) continue
            damageCrystalMap[damage] = entityCrystal.key
        }
        return damageCrystalMap
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
    /* End of position finding */
}