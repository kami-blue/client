package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.manager.mangers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.BlockUtils
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard
import java.util.concurrent.Executors
import java.util.concurrent.Future

@CombatManager.CombatModule
@Module.Info(
        name = "AutoTrap",
        category = Module.Category.COMBAT,
        description = "Traps your enemies in obsidian",
        modulePriority = 60
)
object AutoTrap : Module() {
    private val trapMode = register(Settings.e<TrapMode>("TrapMode", TrapMode.FULL_TRAP))
    private val selfTrap = register(Settings.b("SelfTrap", false))
    private val bindSelfTrap = register(Settings.custom("BindSelfTrap", Bind.none(), BindConverter()))
    private val autoDisable = register(Settings.b("AutoDisable", true))
    private val placeSpeed = register(Settings.floatBuilder("PlacesPerTick").withValue(4f).withRange(0.25f, 5f).withStep(0.25f))

    private val placeThread = Thread { runAutoTrap() }.apply { name = "AutoTrap" }
    private val threadPool = Executors.newSingleThreadExecutor()
    private var future: Future<*>? = null

    override fun isActive(): Boolean {
        return isEnabled && future?.isDone == false
    }

    override fun onDisable() {
        PlayerPacketManager.resetHotbar()
    }

    init {
        listener<SafeTickEvent> {
            if (future?.isDone != false && isPlaceable()) future = threadPool.submit(placeThread)

            if (future?.isDone == false && future?.isCancelled == false) {
                val slot = getObby()
                if (slot != -1) PlayerPacketManager.spoofHotbar(getObby())
                PlayerPacketManager.addPacket(this, PlayerPacketManager.PlayerPacket(rotating = false))
            } else if (CombatManager.isOnTopPriority(this)) {
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

    private fun Setting<Boolean>.toggleMsg() = "$chatName Turned ${this.name} ${if (this.value) "&aon" else "&coff"}&f!"

    private fun isPlaceable(): Boolean {
        (if (selfTrap.value) mc.player else CombatManager.target)?.positionVector?.toBlockPos()?.let {
            for (offset in trapMode.value.offset) {
                if (!BlockUtils.isPlaceable(it.add(offset))) continue
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

    private fun runAutoTrap() {
        BlockUtils.buildStructure(placeSpeed.value) {
            if (isEnabled && CombatManager.isOnTopPriority(this)) {
                val center = (if (selfTrap.value) mc.player else CombatManager.target)?.positionVector?.toBlockPos()
                BlockUtils.getPlaceInfo(center, trapMode.value.offset, it, 3)
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