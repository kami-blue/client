package org.kamiblue.client.module.modules.combat

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.manager.managers.CombatManager
import org.kamiblue.client.manager.managers.HotbarManager.resetHotbar
import org.kamiblue.client.manager.managers.HotbarManager.spoofHotbar
import org.kamiblue.client.manager.managers.PlayerPacketManager
import org.kamiblue.client.manager.managers.PlayerPacketManager.sendPlayerPacket
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.module.modules.combat.Surround.setting
import org.kamiblue.client.util.*
import org.kamiblue.client.util.EntityUtils.flooredPosition
import org.kamiblue.client.util.combat.SurroundUtils
import org.kamiblue.client.util.items.HotbarSlot
import org.kamiblue.client.util.items.firstBlock
import org.kamiblue.client.util.items.hotbarSlots
import org.kamiblue.client.util.math.VectorUtils.toBlockPos
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.client.util.threads.isActiveOrFalse
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.client.util.world.buildStructure
import org.kamiblue.client.util.world.isPlaceable
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard

@CombatManager.CombatModule
internal object AutoTrap : Module(
    name = "AutoTrap",
    category = Category.COMBAT,
    description = "Traps your enemies in obsidian",
    modulePriority = 60
) {
    private val trapMode = setting("Trap Mode", TrapMode.FULL_TRAP)
    private val selfTrap = setting("Self Trap", false)
    private val bindSelfTrap = setting("Bind Self Trap", Bind())
    private val autoDisable = setting("Auto Disable", true)
    private val strictDirection by setting("Strict Direction", false)
    private val placeSpeed = setting("Places Per Tick", 4f, 0.25f..5f, 0.25f)

    private var job: Job? = null

    override fun isActive(): Boolean {
        return isEnabled && job.isActiveOrFalse
    }

    init {
        onDisable {
            resetHotbar()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (!job.isActiveOrFalse && canRun()) job = runAutoTrap()

            if (job.isActiveOrFalse) {
                getObby()?.let {
                    spoofHotbar(it)
                } ?: return@safeListener
                sendPlayerPacket {
                    cancelRotate()
                }
            } else if (CombatManager.isOnTopPriority(AutoTrap)) {
                resetHotbar()
            }
        }

        listener<InputEvent.KeyInputEvent> {
            if (bindSelfTrap.value.isDown(Keyboard.getEventKey())) {
                selfTrap.value = !selfTrap.value
            }
        }
    }

    private fun SafeClientEvent.canRun(): Boolean {
        (if (selfTrap.value) player else CombatManager.target)?.positionVector?.toBlockPos()?.let {
            for (offset in trapMode.value.offset) {
                if (!world.isPlaceable(it.add(offset))) continue
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
        val entity = if (selfTrap.value) player else CombatManager.target ?: return@launch

        buildStructure(
            entity.flooredPosition,
            SurroundUtils.surroundOffset,
            placeSpeed.value,
            3,
            4.25f,
            strictDirection
        ) {
            isEnabled && CombatManager.isOnTopPriority(AutoTrap)
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