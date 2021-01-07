package me.zeroeightsix.kami.module.modules.combat

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.setting.settings.impl.primitive.BooleanSetting
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.WorldUtils
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.isActiveOrFalse
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard

@CombatManager.CombatModule
object AutoTrap : Module(
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
            if (!job.isActiveOrFalse && isPlaceable()) job = runAutoTrap()

            if (job.isActiveOrFalse) {
                val slot = getObby()
                if (slot != -1) PlayerPacketManager.spoofHotbar(getObby())
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

    private fun isPlaceable(): Boolean {
        (if (selfTrap.value) mc.player else CombatManager.target)?.positionVector?.toBlockPos()?.let {
            for (offset in trapMode.value.offset) {
                if (!WorldUtils.isPlaceable(it.add(offset))) continue
                return true
            }
        }
        return false
    }

    private fun getObby(): Int {
        val slots = InventoryUtils.getSlotsHotbar(49)
        if (slots == null) { // Obsidian check
            MessageSendHelper.sendChatMessage("$chatName No obsidian in hotbar, disabling!")
            disable()
            return -1
        }
        return slots[0]
    }

    private fun runAutoTrap() = defaultScope.launch {
        WorldUtils.buildStructure(placeSpeed.value) {
            if (isEnabled && CombatManager.isOnTopPriority(this@AutoTrap)) {
                val center = (if (selfTrap.value) mc.player else CombatManager.target)?.positionVector?.toBlockPos()
                WorldUtils.getPlaceInfo(center, trapMode.value.offset, it, 3)
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