package org.kamiblue.client.module.modules.combat

import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.RenderWorldEvent
import org.kamiblue.client.manager.managers.CombatManager
import org.kamiblue.client.manager.managers.HotbarManager.resetHotbar
import org.kamiblue.client.manager.managers.HotbarManager.serverSideItem
import org.kamiblue.client.manager.managers.HotbarManager.spoofHotbar
import org.kamiblue.client.manager.managers.PlayerPacketManager
import org.kamiblue.client.manager.managers.PlayerPacketManager.sendPlayerPacket
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.*
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.combat.CrystalUtils.calcCrystalDamage
import org.kamiblue.client.util.graphics.ESPRenderer
import org.kamiblue.client.util.items.block
import org.kamiblue.client.util.items.firstBlock
import org.kamiblue.client.util.items.hotbarSlots
import org.kamiblue.client.util.math.RotationUtils.getRotationTo
import org.kamiblue.client.util.math.VectorUtils
import org.kamiblue.client.util.math.VectorUtils.distanceTo
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.client.util.world.PlaceInfo
import org.kamiblue.client.util.world.getNeighbour
import org.kamiblue.client.util.world.hasNeighbour
import org.kamiblue.client.util.world.isPlaceable
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard
import java.util.*

@CombatManager.CombatModule
internal object CrystalBasePlace : Module(
    name = "CrystalBasePlace",
    description = "Places obby for placing crystal on",
    category = Category.COMBAT,
    modulePriority = 90
) {
    private val manualPlaceBind = setting("Bind Manual Place", Bind())
    private val minDamageInc = setting("Min Damage Inc", 2.0f, 0.0f..10.0f, 0.25f)
    private val range = setting("Range", 4.0f, 0.0f..8.0f, 0.5f)
    private val delay = setting("Delay", 20, 0..50, 5)

    private val timer = TickTimer()
    private val renderer = ESPRenderer().apply { aFilled = 33; aOutline = 233 }
    private var inactiveTicks = 0
    private var rotationTo: Vec3d? = null
    private var placePacket: CPacketPlayerTryUseItemOnBlock? = null

    override fun isActive(): Boolean {
        return isEnabled && inactiveTicks <= 3
    }

    init {
        onDisable {
            inactiveTicks = 0
            placePacket = null
            resetHotbar()
        }

        listener<RenderWorldEvent> {
            val clear = inactiveTicks >= 30
            renderer.render(clear)
        }

        safeListener<InputEvent.KeyInputEvent> {
            if (!CombatManager.isOnTopPriority(this@CrystalBasePlace) || CombatSetting.pause) return@safeListener
            val target = CombatManager.target ?: return@safeListener

            if (manualPlaceBind.value.isDown(Keyboard.getEventKey())) prePlace(target)
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@safeListener
            inactiveTicks++

            if (!CombatManager.isOnTopPriority(CrystalBasePlace) || CombatSetting.pause) return@safeListener

            val slot = player.hotbarSlots.firstBlock(Blocks.OBSIDIAN) ?: return@safeListener
            val target = CombatManager.target ?: return@safeListener

            placePacket?.let { packet ->
                if (inactiveTicks > 1) {
                    if (!isHoldingObby) spoofHotbar(slot.hotbarSlot)
                    player.swingArm(EnumHand.MAIN_HAND)
                    connection.sendPacket(packet)
                    resetHotbar()
                    placePacket = null
                }
            }

            if (placePacket == null && CrystalAura.isEnabled && CrystalAura.inactiveTicks > 15) prePlace(target)

            if (isActive()) {
                rotationTo?.let { hitVec ->
                    sendPlayerPacket {
                        rotate(getRotationTo(hitVec))
                    }
                }
            } else {
                rotationTo = null
            }
        }
    }

    private val SafeClientEvent.isHoldingObby
        get() = isObby(player.heldItemMainhand)
            || isObby(player.serverSideItem)

    private fun isObby(itemStack: ItemStack) = itemStack.item.block == Blocks.OBSIDIAN

    private fun SafeClientEvent.prePlace(entity: EntityLivingBase) {
        if (rotationTo != null || !timer.tick((delay.value * 50.0f).toLong(), false)) return
        val placeInfo = getPlaceInfo(entity)

        if (placeInfo != null) {
            rotationTo = placeInfo.hitVec
            placePacket = CPacketPlayerTryUseItemOnBlock(placeInfo.pos, placeInfo.side, EnumHand.MAIN_HAND, placeInfo.hitVecOffset.x.toFloat(), placeInfo.hitVecOffset.y.toFloat(), placeInfo.hitVecOffset.z.toFloat())

            renderer.clear()
            renderer.add(placeInfo.placedPos, ColorHolder(255, 255, 255))

            inactiveTicks = 0
            timer.reset()
        } else {
            timer.reset((delay.value * -25.0f).toLong())
        }
    }

    private fun SafeClientEvent.getPlaceInfo(entity: EntityLivingBase): PlaceInfo? {
        val cacheMap = TreeMap<Float, BlockPos>(compareByDescending { it })
        val prediction = CombatSetting.getPrediction(entity)
        val eyePos = player.getPositionEyes(1.0f)
        val posList = VectorUtils.getBlockPosInSphere(eyePos, range.value)
        val maxCurrentDamage = CombatManager.placeMap.entries
            .filter { eyePos.distanceTo(it.key) < range.value }
            .map { it.value.targetDamage }
            .maxOrNull() ?: 0.0f

        for (pos in posList) {
            // Placeable check
            if (!world.isPlaceable(pos)) continue

            // Neighbour blocks check
            if (!hasNeighbour(pos)) continue

            // Damage check
            val damage = calcPlaceDamage(pos, entity, prediction.first, prediction.second)
            if (!checkDamage(damage.first, damage.second, maxCurrentDamage)) continue

            cacheMap[damage.first] = pos
        }

        for (pos in cacheMap.values) {
            return getNeighbour(pos, 1) ?: continue
        }
        return null
    }

    private fun SafeClientEvent.calcPlaceDamage(pos: BlockPos, entity: EntityLivingBase, entityPos: Vec3d, entityBB: AxisAlignedBB): Pair<Float, Float> {
        // Set up a fake obsidian here for proper damage calculation
        val prevState = world.getBlockState(pos)
        world.setBlockState(pos, Blocks.OBSIDIAN.defaultState)

        // Checks damage
        val damage = calcCrystalDamage(pos, entity, entityPos, entityBB)
        val selfDamage = calcCrystalDamage(pos, player)

        // Revert the block state before return
        world.setBlockState(pos, prevState)

        return damage to selfDamage
    }

    private fun checkDamage(damage: Float, selfDamage: Float, maxCurrentDamage: Float) =
        selfDamage < CrystalAura.maxSelfDamage && damage > CrystalAura.minDamage && (maxCurrentDamage < CrystalAura.minDamage || damage - maxCurrentDamage >= minDamageInc.value)
}