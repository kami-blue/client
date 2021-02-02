package org.kamiblue.client.module.modules.combat

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.manager.managers.CombatManager
import org.kamiblue.client.manager.managers.PlayerPacketManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.setting.settings.impl.primitive.BooleanSetting
import org.kamiblue.client.util.*
import org.kamiblue.client.util.WorldUtils.buildStructure
import org.kamiblue.client.util.WorldUtils.getPlaceInfo
import org.kamiblue.client.util.WorldUtils.isPlaceable
import org.kamiblue.client.util.items.HotbarSlot
import org.kamiblue.client.util.items.firstBlock
import org.kamiblue.client.util.items.hotbarSlots
import org.kamiblue.client.util.math.VectorUtils.toBlockPos
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.client.util.threads.isActiveOrFalse
import org.kamiblue.client.util.threads.safeListener
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard

@CombatManager.CombatModule
internal object AutoTrap : Module(
    name = "AutoTrap",
    category = Category.COMBAT,
    description = "Traps your enemies in obsidian",
    modulePriority = 60
) {
    private val trapMode = setting("TrapMode", TrapMode.FULL_TRAP)
    private val selfTrap = setting("SelfTrap", false)
    private val bindSelfTrap = setting("BindSelfTrap", Bind())
    private val autoDisable = setting("AutoDisable", true)
    private val placeSpeed = setting("PlacesPerTick", 4f, 0.25f..5f, 0.25f)

    private var job: Job? = null

    override fun isActive(): Boolean {
        return isEnabled && job.isActiveOrFalse
    }

    init {
        onDisable {
            PlayerPacketManager.resetHotbar()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (!job.isActiveOrFalse && canRun()) job = runAutoTrap()

            if (job.isActiveOrFalse) {
                getObby()?.let {
                    PlayerPacketManager.spoofHotbar(it.hotbarSlot)
                } ?: return@safeListener
                PlayerPacketManager.addPacket(AutoTrap, PlayerPacketManager.PlayerPacket(rotating = false))
            } else if (CombatManager.isOnTopPriority(AutoTrap)) {
                PlayerPacketManager.resetHotbar()
            }
        }

        listener<InputEvent.KeyInputEvent> {
            if (bindSelfTrap.value.isDown(Keyboard.getEventKey())) {
                selfTrap.value = !selfTrap.value
                MessageSendHelper.sendChatMessage(selfTrap.toggleMsg())
            }
        }
    }

    private fun BooleanSetting.toggleMsg() = "$chatName Turned ${this.name} ${if (this.value) "&aon" else "&coff"}&f!"

    private fun SafeClientEvent.canRun(): Boolean {
        (if (selfTrap.value) player else CombatManager.target)?.positionVector?.toBlockPos()?.let {
            for (offset in trapMode.value.offset) {
                if (!isPlaceable(it.add(offset))) continue
                return true
            }
        }
        return false
    }

    private fun SafeClientEvent.getObby(): HotbarSlot? {
        val slots = player.hotbarSlots.firstBlock(Blocks.OBSIDIAN)

        if (slots == null) { // Obsidian check
            MessageSendHelper.sendChatMessage("$chatName No obsidian in hotbar, disabling!")
            disable()
            return null
        }

        return slots
    }

    private fun SafeClientEvent.runAutoTrap() = defaultScope.launch {
        buildStructure(placeSpeed.value) {
            if (isEnabled && CombatManager.isOnTopPriority(this@AutoTrap)) {
                val center = (if (selfTrap.value) player else CombatManager.target)?.positionVector?.toBlockPos()
                getPlaceInfo(center, trapMode.value.offset, it, 3)
            } else {
                null
            }
        }
        if (autoDisable.value) disable()
    }

    @Suppress("UNUSED")
    private enum class TrapMode(val offset: Array<BlockPos>) {
        FULL_TRAP(arrayOf(
            BlockPos(1, 0, 0),
            BlockPos(-1, 0, 0),
            BlockPos(0, 0, 1),
            BlockPos(0, 0, -1),
            BlockPos(1, 1, 0),
            BlockPos(-1, 1, 0),
            BlockPos(0, 1, 1),
            BlockPos(0, 1, -1),
            BlockPos(0, 2, 0)
        )),
        CRYSTAL_TRAP(arrayOf(
            BlockPos(1, 1, 1),
            BlockPos(1, 1, 0),
            BlockPos(1, 1, -1),
            BlockPos(0, 1, -1),
            BlockPos(-1, 1, -1),
            BlockPos(-1, 1, 0),
            BlockPos(-1, 1, 1),
            BlockPos(0, 1, 1),
            BlockPos(0, 2, 0)
        ))
    }
}