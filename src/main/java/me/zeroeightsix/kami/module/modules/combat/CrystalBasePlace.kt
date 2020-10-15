package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.manager.mangers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.BlockUtils
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.combat.CrystalUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.math.VectorUtils
import me.zeroeightsix.kami.util.math.VectorUtils.toVec3d
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.block.Block
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard

@Module.Info(
        name = "CrystalBasePlace",
        description = "Places obby for placing crystal on",
        category = Module.Category.COMBAT,
        modulePriority = 90
)
object CrystalBasePlace : Module() {
    private val manualPlaceBind = register(Settings.custom("BindManualPlace", Bind.none(), BindConverter()))
    private val minDamageInc = register(Settings.floatBuilder("MinDamageInc").withValue(2.0f).withRange(0.0f, 10.0f).withStep(0.25f))
    private val range = register(Settings.floatBuilder("Range").withValue(4.0f).withRange(0.0f, 8.0f).withStep(0.5f))
    private val delay = register(Settings.integerBuilder("Delay").withValue(20).withRange(0, 50).withStep(5))

    private val timer = TimerUtils.TickTimer()
    private val renderer = ESPRenderer().apply { aFilled = 33; aOutline = 233 }
    private var inactiveTicks = 0
    private var rotationTo: Vec3d? = null
    private var placePacket: CPacketPlayerTryUseItemOnBlock? = null

    override fun onDisable() {
        inactiveTicks = 0
        placePacket = null
        PlayerPacketManager.resetHotbar()
    }

    override fun isActive(): Boolean {
        return isEnabled && inactiveTicks <= 3
    }

    init {
        listener<RenderWorldEvent> {
            val clear = inactiveTicks >= 30
            renderer.render(clear)
        }

        listener<InputEvent.KeyInputEvent> {
            if (!CombatManager.isOnTopPriority(this) || CombatSetting.pause) return@listener
            val target = CombatManager.target ?: return@listener

            if (manualPlaceBind.value.isDown(Keyboard.getEventKey())) prePlace(target)
        }

        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@listener
            inactiveTicks++
            if (!CombatManager.isOnTopPriority(this) || CombatSetting.pause) return@listener
            val slot = getObby() ?: return@listener
            val target = CombatManager.target ?: return@listener

            placePacket?.let { packet ->
                if (inactiveTicks > 1) {
                    mc.player.swingArm(EnumHand.MAIN_HAND)
                    mc.connection!!.sendPacket(packet)
                    PlayerPacketManager.resetHotbar()
                    placePacket = null
                }
            }

            if (placePacket == null && CrystalAura.isEnabled && CrystalAura.inactiveTicks > 20) prePlace(target)

            if (isActive()) {
                rotationTo?.let { hitVec ->
                    if (!isHoldingObby(mc.player.heldItemMainhand) && !isHoldingObby(mc.player.inventory.getStackInSlot(PlayerPacketManager.serverSideHotbar))) {
                        PlayerPacketManager.spoofHotbar(slot)
                    }
                    val rotation = RotationUtils.getRotationTo(hitVec, true)
                    PlayerPacketManager.addPacket(this, PlayerPacketManager.PlayerPacket(rotating = true, rotation = Vec2f(rotation)))
                }
            } else {
                rotationTo = null
            }
        }
    }

    private fun isHoldingObby(itemStack: ItemStack) = Block.getBlockFromItem(itemStack.getItem()) == Blocks.OBSIDIAN

    private fun getObby(): Int? {
        val slots = InventoryUtils.getSlotsHotbar(49)
        if (slots == null) { // Obsidian check
            if (isEnabled) {
                MessageSendHelper.sendChatMessage("$chatName No obsidian in hotbar, disabling!")
                disable()
            }
            return null
        }
        return slots[0]
    }

    private fun prePlace(entity: EntityLivingBase) {
        if (rotationTo != null || !timer.tick((delay.value * 50.0f).toLong(), false)) return
        val placeInfo = getPlaceInfo(entity)
        if (placeInfo != null) {
            val offset = BlockUtils.getHitVecOffset(placeInfo.first)
            val hitVec = Vec3d(placeInfo.second).add(offset)
            rotationTo = hitVec
            placePacket = CPacketPlayerTryUseItemOnBlock(placeInfo.second, placeInfo.first, EnumHand.MAIN_HAND, offset.x.toFloat(), offset.y.toFloat(), offset.z.toFloat())
            renderer.clear()
            renderer.add(placeInfo.second.offset(placeInfo.first), ColorHolder(255, 255, 255))
            inactiveTicks = 0
            timer.reset()
        } else {
            timer.reset((delay.value * -25.0f).toLong())
        }
    }

    private fun getPlaceInfo(entity: EntityLivingBase): Pair<EnumFacing, BlockPos>? {
        val prediction = CombatSetting.getPrediction(entity)
        val eyePos = mc.player.getPositionEyes(1.0f)
        val posList = VectorUtils.getBlockPosInSphere(eyePos, range.value).sortedBy { entity.getDistanceSqToCenter(it) }
        val maxCurrentDamage = CombatManager.crystalPlaceList
                .filter { eyePos.distanceTo(it.first.toVec3d()) < range.value }
                .maxBy { it.second }?.second ?: 0.0f
        val minDamage = CrystalAura.minDamage
        val maxSelfDamage = CrystalAura.maxSelfDamage

        for (pos in posList) {
            // Placeable check
            if (!BlockUtils.isPlaceable(pos, false)) continue

            // Checks neighbour blocks
            if (!BlockUtils.hasNeighbour(pos)) continue

            // Checks distance
            val crystalPos = pos.toVec3d().add(0.0, 0.5, 0.0)
            if (prediction.first.distanceTo(crystalPos) > 5.0) continue

            // Set up a fake obsidian here for proper damage calculation
            val prevState = mc.world.getBlockState(pos)
            mc.world.setBlockState(pos, Blocks.OBSIDIAN.defaultState)

            // Checks damage
            val damage = CrystalUtils.calcDamage(pos, entity, prediction.first, prediction.second)
            val selfDamage = CrystalUtils.calcDamage(pos, mc.player)
            val passed = selfDamage < maxSelfDamage && damage > minDamage && (maxCurrentDamage < minDamage || damage - maxCurrentDamage >= minDamageInc.value)

            // Revert the block state
            mc.world.setBlockState(pos, prevState)

            // We need to revert the block state because continue the loop
            if (!passed) continue

            return BlockUtils.getNeighbour(pos, 1) ?: continue
        }
        return null
    }
}