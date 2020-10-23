package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.mixin.client.MixinAnvilChunkLoader
import me.zeroeightsix.kami.util.math.MathUtils.round
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.chunk.storage.AnvilChunkLoader
import org.spongepowered.asm.mixin.gen.Invoker
import java.io.*
import java.util.zip.DeflaterOutputStream
import kotlin.math.hypot

object InfoCalculator {
    private val mc = Wrapper.minecraft
    @Invoker

    fun getServerType() = if (mc.isIntegratedServerRunning) "Singleplayer" else mc.currentServerData?.serverIP
            ?: "MainMenu"

    @JvmStatic
    fun ping() = mc.player?.let { mc.connection?.getPlayerInfo(it.uniqueID)?.responseTime ?: 1 } ?: -1

    fun speed(useUnitKmH: Boolean): Double {
        val tps = 1000.0 / mc.timer.tickLength
        val multiply = if (useUnitKmH) 3.6 else 1.0 // convert mps to kmh
        return hypot(mc.player.posX - mc.player.prevPosX, mc.player.posZ - mc.player.prevPosZ) * multiply * tps
    }

    fun heldItemDurability() = with(mc.player.heldItemMainhand) { maxDamage - getItemDamage() }

    fun memory() = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L

    @JvmStatic
    fun tps(places: Int) = round(TpsCalculator.tickRate, places)

    fun dimension() = when (mc.player?.dimension) {
        -1 -> "Nether"
        0 -> "Overworld"
        1 -> "End"
        else -> "No Dimension"
    }

    /**
     * Ported from Forgehax under MIT: https://github.com/fr1kin/ForgeHax/blob/2011740/src/main/java/com/matt/forgehax/mods/ClientChunkSize.java
     * @return current chunk size in bytes
     */
    fun chunkSize(): Int {
        if (mc.world == null) return 0

        val chunk = mc.world.getChunk(mc.player.position)
        if (chunk.isEmpty) return 0

        val root = NBTTagCompound()
        val level = NBTTagCompound()

        root.setTag("Level", level)
        root.setInteger("DataVersion", 6969)

        try {
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            val loader = AnvilChunkLoader(File("kamiblue"), null)
            (loader as MixinAnvilChunkLoader).invokeWriteChunkToNBT(chunk, mc.world, level)
        } catch (ignored: Throwable) {
            return 0 // couldn't save
        }

        val compressed = DataOutputStream(BufferedOutputStream(DeflaterOutputStream(ByteArrayOutputStream(8096))))

        return try {
            CompressedStreamTools.write(root, compressed)
            compressed.size()
        } catch (ignored: IOException) {
            0 // couldn't save
        }
    }
}