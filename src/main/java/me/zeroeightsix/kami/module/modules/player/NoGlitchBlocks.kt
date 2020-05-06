package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

/**
 * @author gbl
 *
 * Used with permission from GPL licensing as proven by
 * https://github.com/gbl/AntiGhost/issues/1
 * and
 * https://github.com/gbl/AntiGhost/issues/6
 *
 * You can view the source code here:
 * https://github.com/gbl/AntiGhost/
 * TODO: fix kicks from this
 */
@Module.Info(
        name = "NoGlitchBlocks",
        description = "Prevents blocks desyncing and creating ghost blocks (e: kicks)",
        category = Module.Category.EXPERIMENTAL
)
class NoGlitchBlocks : Module() {
    private val range = register(Settings.integerBuilder("Range").withRange(1, 5).withValue(4).build())

    private var previous = -1
    private var conn: NetHandlerPlayClient? = null

    public override fun onEnable() {
        conn = mc.connection
        previous = range.value
    }

    override fun onUpdate() {
        if (mc.player == null || mc.isIntegratedServerRunning) return
        if (conn == null) {
            conn = mc.connection
        }
        if (previous != range.value) {
            previous = range.value
            return  // prevents running when changing size
        }
        run()
    }

    private fun run() {
        if (conn == null) {
            return
        }
        val pos = mc.player.position
        for (dx in -range.value..range.value) for (dy in -range.value..range.value) for (dz in -range.value..range.value) {
            val packet = CPacketPlayerDigging(
                    CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,
                    BlockPos(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz),
                    EnumFacing.UP /* with ABORT_DESTROY_BLOCK, this value is unused */
            )
            conn!!.sendPacket(packet)
        }
    }
}