package org.kamiblue.client.module.modules.combat

import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.PlayerTravelEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.module.modules.movement.Strafe
import org.kamiblue.client.util.combat.SurroundUtils
import org.kamiblue.client.util.combat.SurroundUtils.checkHole
import org.kamiblue.client.util.math.RotationUtils
import org.kamiblue.client.util.math.VectorUtils
import org.kamiblue.client.util.math.VectorUtils.distanceTo
import org.kamiblue.client.util.math.VectorUtils.toBlockPos
import org.kamiblue.client.util.threads.safeAsyncListener
import org.kamiblue.commons.extension.ceilToInt
import org.kamiblue.commons.extension.toRadian
import org.kamiblue.event.listener.listener
import kotlin.math.*

internal object HoleSnap : Module(
    name = "HoleSnap",
    description = "Move you into the hole nearby",
    category = Category.COMBAT
) {
    private val disableStrafe by setting("Disable Strafe", true)
    private val range by setting("Range", 2.5f, 0.5f..4.0f, 0.25f)

    init {
        listener<PacketEvent.Receive> {
            if (it.packet is SPacketPlayerPosLook) disable()
        }

        safeAsyncListener<PlayerTravelEvent> {
            if (checkHole(player) != SurroundUtils.HoleType.NONE) {
                disable()
                return@safeAsyncListener
            }

            findHole()?.let {
                if (disableStrafe) Strafe.disable()
                if (player.onGround && !isAboveHole(it)) {
                    val playerPos = player.positionVector
                    val targetPos = Vec3d(it.x + 0.5, player.posY, it.z + 0.5)

                    val yawRad = RotationUtils.getRotationTo(playerPos, targetPos).x.toRadian()
                    val dist = playerPos.distanceTo(targetPos)
                    val speed = min(0.26, dist / 2.0)

                    player.motionX = -sin(yawRad) * speed
                    player.motionZ = cos(yawRad) * speed
                }
            }
        }
    }

    private fun SafeClientEvent.findHole(): BlockPos? {
        var closestHole = Pair(69.69, BlockPos.ORIGIN)
        val playerPos = player.positionVector.toBlockPos()
        val ceilRange = range.ceilToInt()
        val posList = VectorUtils.getBlockPositionsInArea(playerPos.add(ceilRange, -1, ceilRange), playerPos.add(-ceilRange, -1, -ceilRange))

        for (posXZ in posList) {
            val dist = player.distanceTo(posXZ)
            if (dist > range || dist > closestHole.first) continue

            for (posY in 0..5) {
                val pos = posXZ.add(0, -posY, 0)
                if (!world.isAirBlock(pos.up())) break
                if (checkHole(pos) == SurroundUtils.HoleType.NONE) continue
                closestHole = dist to pos
            }
        }

        return if (closestHole.second != BlockPos.ORIGIN) closestHole.second else null
    }

    private fun SafeClientEvent.isAboveHole(holePos: BlockPos): Boolean {
        return player.posX in holePos.x + 0.31..holePos.x + 0.69
            && player.posZ in holePos.z + 0.31..holePos.z + 0.69
    }
}