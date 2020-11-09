package me.zeroeightsix.kami.gui.hudgui.elements.world

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.mixin.client.MixinAnvilChunkLoader
import me.zeroeightsix.kami.util.math.MathUtils
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.chunk.storage.AnvilChunkLoader
import java.io.*
import java.util.zip.DeflaterOutputStream

@HudElement.Info(
        category = HudElement.Category.WORLD,
        description = "Display size of the chunk you are in"
)
object ChunkSize : LabelHud("ChunkSize") {

    override fun updateText() {
        val chunkSize = MathUtils.round(calcChunkSize() / 1024.0, 2)
        displayText.add(chunkSize.toString(), primaryColor.value)
        displayText.add("KB (Chunk)", secondaryColor.value)
    }

    /**
     * Ported from Forgehax under MIT: https://github.com/fr1kin/ForgeHax/blob/2011740/src/main/java/com/matt/forgehax/mods/ClientChunkSize.java
     * @return current chunk size in bytes
     */
    fun calcChunkSize(): Int {
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