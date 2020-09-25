package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.manager.mangers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BlockUtils
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.math.BlockPos
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.pow

@Module.Info(
        name = "AutoTrap",
        category = Module.Category.COMBAT,
        description = "Traps your enemies in obsidian",
        modulePriority = 60
)
object AutoTrap : Module() {
    private val trapMode = register(Settings.e<TrapMode>("TrapMode", TrapMode.FULL_TRAP))
    private val selfTrap = register(Settings.b("SelfTrap", false))
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

    override fun onUpdate() {
        if (future?.isDone != false && (CombatManager.target != null || selfTrap.value) && getPlacingPos(emptyList()) != null) future = threadPool.submit(placeThread)

        if (future?.isDone == false && future?.isCancelled == false) {
            PlayerPacketManager.addPacket(this, PlayerPacketManager.PlayerPacket(rotating = false))
        } else {
            PlayerPacketManager.resetHotbar()
        }
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
        val slot = getObby()
        if (slot != -1) PlayerPacketManager.spoofHotbar(getObby())
        val placed = ArrayList<BlockPos>()
        var placeCount = 0
        while (isEnabled) {
            val pos = getPlacingPos(placed) ?: break
            val neighbor = BlockUtils.getNeighbour(pos, 2) ?: break
            placeCount++
            placed.add(neighbor.second.offset(neighbor.first))
            println("${neighbor.first} ,${neighbor.second}")
            BlockUtils.doPlace(neighbor.second, neighbor.first, placeSpeed.value)
            if (placeCount >= 4) Thread.sleep(50L)
        }
        disable()
    }

    private fun getPlacingPos(toIgnore: List<BlockPos>): BlockPos? {
        (if (selfTrap.value) mc.player else CombatManager.target)?.positionVector?.toBlockPos()?.let {
            for (offset in trapMode.value.offset) {
                val pos = it.add(offset)
                if (toIgnore.contains(pos)) continue
                if (!BlockUtils.isPlaceable(pos)) continue
                return pos
            }
        }
        return null
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